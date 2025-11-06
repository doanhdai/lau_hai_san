import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])

  function add(notification) {
    const id = Date.now()
    notifications.value.push({
      id,
      type: notification.type || 'info',
      message: notification.message,
      duration: notification.duration || 3000
    })

    if (notification.duration !== 0) {
      setTimeout(() => {
        remove(id)
      }, notification.duration || 3000)
    }
  }

  function remove(id) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  function success(message, duration) {
    add({ type: 'success', message, duration })
  }

  function error(message, duration) {
    add({ type: 'error', message, duration })
  }

  function warning(message, duration) {
    add({ type: 'warning', message, duration })
  }

  function info(message, duration) {
    add({ type: 'info', message, duration })
  }

  return {
    notifications,
    add,
    remove,
    success,
    error,
    warning,
    info
  }
})
