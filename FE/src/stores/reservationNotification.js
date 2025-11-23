import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { reservationService } from '@/services/reservationService'
import { useAuthStore } from '@/stores/auth'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export const useReservationNotificationStore = defineStore('reservationNotification', () => {
  const notifications = ref([])
  const lastCheckedTime = ref(null)
  const isPolling = ref(false)
  let pollingInterval = null
  let stompClient = null
  let reconnectAttempts = 0
  const maxReconnectAttempts = 5
  const reconnectDelay = 3000 // 3 seconds
  let subscription = null // Track subscription to avoid duplicate subscriptions

  const unreadCount = computed(() => {
    return notifications.value.filter(n => !n.read).length
  })

  function formatTime(dateString) {
    if (!dateString) return ''
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = date - now
    const diffMins = Math.floor(diffMs / 60000)
    
    if (diffMins < 0) {
      return 'Đã qua'
    } else if (diffMins < 60) {
      return `Còn ${diffMins} phút`
    } else {
      const hours = Math.floor(diffMins / 60)
      const mins = diffMins % 60
      return `Còn ${hours}h ${mins}p`
    }
  }

  function checkReservations() {
    if (isPolling.value) return
    isPolling.value = true

    reservationService.getAll()
      .then(response => {
        if (response.success && response.data) {
          const reservations = Array.isArray(response.data) ? response.data : []
          const now = new Date()
          const thirtyMinutesFromNow = new Date(now.getTime() + 30 * 60 * 1000)

          // Filter reservations that need notification
          const newNotifications = []

          reservations.forEach(reservation => {
            // Skip cancelled and completed reservations
            if (reservation.status === 'CANCELLED' || reservation.status === 'COMPLETED') {
              return
            }

            const reservationTime = new Date(reservation.reservationTime || reservation.reservationDateTime)
            if (isNaN(reservationTime.getTime())) return

            // Check if reservation is new (created in last 5 minutes)
            const createdAt = new Date(reservation.createdAt)
            const fiveMinutesAgo = new Date(now.getTime() - 5 * 60 * 1000)
            const isNew = createdAt > fiveMinutesAgo && (!lastCheckedTime.value || createdAt > new Date(lastCheckedTime.value))

            // Check if reservation is coming soon (within 30 minutes)
            const isComingSoon = reservationTime >= now && reservationTime <= thirtyMinutesFromNow

            if (isNew || isComingSoon) {
              // Check if notification already exists
              const existingNotification = notifications.value.find(n => n.reservationId === reservation.id)
              
              if (!existingNotification) {
                let message = ''
                let type = 'info'
                
                if (isNew && reservation.status === 'PENDING') {
                  message = `Đặt bàn mới: ${reservation.customerName || 'Khách hàng'} - ${reservation.tableNumber || 'Chưa gán bàn'}`
                  type = 'info'
                } else if (isComingSoon) {
                  const timeUntil = formatTime(reservationTime)
                  message = `Khách sắp đến: ${reservation.customerName || 'Khách hàng'} - ${timeUntil} - ${reservation.tableNumber || 'Chưa gán bàn'}`
                  type = 'warning'
                }

                if (message) {
                  newNotifications.push({
                    id: `reservation-${reservation.id}-${Date.now()}`,
                    reservationId: reservation.id,
                    type,
                    message,
                    reservationTime: reservationTime.toISOString(),
                    customerName: reservation.customerName,
                    tableNumber: reservation.tableNumber,
                    read: false,
                    createdAt: new Date().toISOString()
                  })
                }
              } else if (isComingSoon && !existingNotification.read) {
                // Update existing notification if it's coming soon
                const timeUntil = formatTime(reservationTime)
                existingNotification.message = `Khách sắp đến: ${reservation.customerName || 'Khách hàng'} - ${timeUntil} - ${reservation.tableNumber || 'Chưa gán bàn'}`
                existingNotification.type = 'warning'
              }
            }
          })

          // Add new notifications
          if (newNotifications.length > 0) {
            notifications.value.unshift(...newNotifications)
            // Keep only last 50 notifications
            if (notifications.value.length > 50) {
              notifications.value = notifications.value.slice(0, 50)
            }
          }

          // Remove old notifications (older than 1 hour)
          const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000)
          notifications.value = notifications.value.filter(n => {
            const notifTime = new Date(n.createdAt)
            return notifTime > oneHourAgo
          })

          lastCheckedTime.value = now.toISOString()
        }
      })
      .catch(error => {
        console.error('Error checking reservations:', error)
      })
      .finally(() => {
        isPolling.value = false
      })
  }

  function connectWebSocket() {
    if (stompClient && stompClient.connected) {
      return // Already connected
    }

    try {
      // Get token from auth store
      const authStore = useAuthStore()
      const token = authStore.token
      
      if (!token) {
        console.warn('No token available for WebSocket connection. Falling back to polling.')
        startPolling()
        return
      }

      const host = import.meta.env.VITE_API_URL?.replace(/^https?:\/\//, '') || 'localhost:8080'
      // Add token as query parameter for authentication
      const wsUrl = `http://${host}/ws?token=${encodeURIComponent(token)}`
      
      stompClient = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        reconnectDelay: reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        connectHeaders: {
          'Authorization': `Bearer ${token}`
        },
        onConnect: (frame) => {
          console.log('WebSocket connected:', frame)
          reconnectAttempts = 0
          
          // Stop polling when WebSocket is connected (to avoid duplicate notifications)
          stopPolling()
          
          // Unsubscribe previous subscription if exists
          if (subscription) {
            try {
              subscription.unsubscribe()
            } catch (e) {
              // Ignore errors when unsubscribing
            }
            subscription = null
          }
          
          // Subscribe to reservation notifications (only once)
          subscription = stompClient.subscribe('/topic/reservations', (message) => {
            try {
              const data = JSON.parse(message.body)
              handleWebSocketMessage(data)
            } catch (error) {
              console.error('Error parsing WebSocket message:', error)
            }
          })
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame)
        },
        onWebSocketClose: () => {
          console.log('WebSocket disconnected')
          
          // Attempt to reconnect
          if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++
            setTimeout(() => {
              console.log(`Attempting to reconnect WebSocket (${reconnectAttempts}/${maxReconnectAttempts})...`)
              connectWebSocket()
            }, reconnectDelay)
          } else {
            console.error('Max WebSocket reconnection attempts reached. Falling back to polling.')
            // Fallback to polling if WebSocket fails
            startPolling()
          }
        },
        onDisconnect: () => {
          console.log('STOMP disconnected')
          // Start polling when WebSocket disconnects
          if (!pollingInterval) {
            startPolling()
          }
        }
      })
      
      stompClient.activate()
    } catch (error) {
      console.error('Error connecting WebSocket:', error)
      // Fallback to polling
      startPolling()
    }
  }

  function handleWebSocketMessage(data) {
    if (data.type === 'NEW' || data.type === 'COMING_SOON') {
      // Check if notification already exists (check by reservationId and message type to avoid duplicates)
      const notificationType = data.type === 'NEW' ? 'info' : 'warning'
      const existingNotification = notifications.value.find(
        n => n.reservationId === data.reservationId && 
             ((data.type === 'NEW' && n.type === 'info' && n.message.includes('Đặt bàn mới')) ||
              (data.type === 'COMING_SOON' && n.type === 'warning' && n.message.includes('Khách sắp đến')))
      )
      
      if (!existingNotification) {
        const notification = {
          id: `ws-${data.reservationId}-${data.type}-${Date.now()}`,
          reservationId: data.reservationId,
          type: notificationType,
          message: data.message,
          reservationTime: data.reservationTime,
          customerName: data.customerName,
          tableNumber: data.tableNumber,
          read: false,
          createdAt: new Date().toISOString()
        }
        
        notifications.value.unshift(notification)
        
        // Keep only last 50 notifications
        if (notifications.value.length > 50) {
          notifications.value = notifications.value.slice(0, 50)
        }
        
        // Play notification sound (optional)
        if ('Notification' in window && Notification.permission === 'granted') {
          new Notification('Đặt bàn mới', {
            body: data.message,
            icon: '/favicon.ico'
          })
        }
      } else {
        // Update existing notification if it's a "coming soon" update
        if (data.type === 'COMING_SOON' && existingNotification.type === 'warning') {
          existingNotification.message = data.message
          existingNotification.reservationTime = data.reservationTime
          existingNotification.read = false // Mark as unread if updated
        }
      }
    }
  }

  function disconnectWebSocket() {
    // Unsubscribe before disconnecting
    if (subscription) {
      try {
        subscription.unsubscribe()
      } catch (e) {
        // Ignore errors when unsubscribing
      }
      subscription = null
    }
    
    if (stompClient) {
      if (stompClient.connected) {
        stompClient.deactivate()
      }
      stompClient = null
    }
    reconnectAttempts = 0
  }

  function startPolling() {
    if (pollingInterval) return
    
    // Check immediately
    checkReservations()
    
    // Then check every 1 minute for "coming soon" notifications
    pollingInterval = setInterval(() => {
      checkComingSoonReservations()
    }, 60000) // 60 seconds (1 minute)
  }

  function checkComingSoonReservations() {
    if (isPolling.value) return
    isPolling.value = true

    reservationService.getAll()
      .then(response => {
        if (response.success && response.data) {
          const reservations = Array.isArray(response.data) ? response.data : []
          const now = new Date()
          const thirtyMinutesFromNow = new Date(now.getTime() + 30 * 60 * 1000)

          reservations.forEach(reservation => {
            if (reservation.status === 'CANCELLED' || reservation.status === 'COMPLETED') {
              return
            }

            const reservationTime = new Date(reservation.reservationTime || reservation.reservationDateTime)
            if (isNaN(reservationTime.getTime())) return

            // Check if reservation is coming soon (within 30 minutes)
            const isComingSoon = reservationTime >= now && reservationTime <= thirtyMinutesFromNow

            if (isComingSoon) {
              const existingNotification = notifications.value.find(
                n => n.reservationId === reservation.id && n.type === 'warning'
              )
              
              if (!existingNotification) {
                const timeUntil = formatTime(reservationTime)
                const notification = {
                  id: `reservation-${reservation.id}-coming-soon-${Date.now()}`,
                  reservationId: reservation.id,
                  type: 'warning',
                  message: `Khách sắp đến: ${reservation.customerName || 'Khách hàng'} - ${timeUntil} - ${reservation.tableNumber || 'Chưa gán bàn'}`,
                  reservationTime: reservationTime.toISOString(),
                  customerName: reservation.customerName,
                  tableNumber: reservation.tableNumber,
                  read: false,
                  createdAt: new Date().toISOString()
                }
                
                notifications.value.unshift(notification)
                
                if (notifications.value.length > 50) {
                  notifications.value = notifications.value.slice(0, 50)
                }
              }
            }
          })

          // Remove old notifications (older than 1 hour)
          const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000)
          notifications.value = notifications.value.filter(n => {
            const notifTime = new Date(n.createdAt)
            return notifTime > oneHourAgo
          })
        }
      })
      .catch(error => {
        console.error('Error checking coming soon reservations:', error)
      })
      .finally(() => {
        isPolling.value = false
      })
  }

  function stopPolling() {
    if (pollingInterval) {
      clearInterval(pollingInterval)
      pollingInterval = null
    }
  }

  function markAsRead(notificationId) {
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.read = true
    }
  }

  function markAllAsRead() {
    notifications.value.forEach(n => {
      n.read = true
    })
  }

  function remove(notificationId) {
    const index = notifications.value.findIndex(n => n.id === notificationId)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  function clear() {
    notifications.value = []
    lastCheckedTime.value = null
  }

  function start() {
    // Request notification permission
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission()
    }
    
    // Connect WebSocket for real-time notifications
    // Polling will be started only if WebSocket fails to connect
    connectWebSocket()
  }

  function stop() {
    disconnectWebSocket()
    stopPolling()
  }

  return {
    notifications,
    unreadCount,
    isPolling,
    checkReservations,
    checkComingSoonReservations,
    connectWebSocket,
    disconnectWebSocket,
    startPolling,
    stopPolling,
    start,
    stop,
    markAsRead,
    markAllAsRead,
    remove,
    clear
  }
})

