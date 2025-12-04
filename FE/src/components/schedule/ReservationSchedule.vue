<template>
  <div class="bg-white border border-gray-200 rounded-lg p-4">
    <div class="mb-4 flex items-center justify-between">
      <h2 class="text-lg font-bold text-slate-900">Lịch đặt bàn hôm nay</h2>
      <div class="flex items-center gap-2">
        <button
          @click="refreshSchedule"
          :disabled="loading"
          class="px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-slate-700 rounded-lg text-sm font-medium transition-colors disabled:opacity-50"
        >
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }"></i>
        </button>
        <input
          v-model="selectedDate"
          type="date"
          @change="loadSchedule"
          class="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-slate-500 focus:border-transparent"
        />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Schedule Grid -->
    <div v-else class="overflow-x-auto">
      <div class="inline-block min-w-full">
        <!-- Time Header -->
        <div class="flex border-b border-gray-300">
          <div class="w-32 flex-shrink-0 border-r border-gray-300 bg-slate-50 px-3 py-2">
            <span class="text-xs font-semibold text-slate-700">Bàn / Giờ</span>
          </div>
          <div class="flex">
            <div
              v-for="timeSlot in timeSlots"
              :key="timeSlot"
              class="w-16 flex-shrink-0 border-r border-gray-300 bg-slate-50 px-2 py-2 text-center"
            >
              <span class="text-xs font-medium text-slate-700">{{ timeSlot }}</span>
            </div>
          </div>
        </div>

        <!-- Unassigned Row -->
        <div class="flex border-b border-gray-300" :style="{ minHeight: `${getUnassignedRowHeight()}px` }">
          <div class="w-32 flex-shrink-0 border-r border-gray-300 bg-slate-50 px-3 py-2 flex items-center">
            <span class="text-sm font-medium text-slate-700">Chưa xếp</span>
          </div>
          <div class="flex relative" :style="{ minHeight: `${getUnassignedRowHeight()}px` }">
            <div
              v-for="timeSlot in timeSlots"
              :key="timeSlot"
              class="w-16 flex-shrink-0 border-r border-gray-200 relative"
            ></div>
            <!-- Unassigned blocks - stacked when overlapping -->
            <div
              v-for="(item, index) in getUnassignedItemsStacked()"
              :key="`unassigned-${item.id}`"
              :style="getUnassignedBlockStyle(item, index)"
              :class="getBlockClass(item)"
              class="absolute rounded px-2 py-1 text-xs font-medium text-white shadow-sm cursor-pointer hover:shadow-md transition-shadow"
              :title="getBlockTooltip(item)"
            >
              {{ getBlockLabel(item) }}
            </div>
          </div>
        </div>

        <!-- Table Rows -->
        <div
          v-for="table in tables"
          :key="table.id"
          class="flex border-b border-gray-300 min-h-[60px]"
        >
          <div class="w-32 flex-shrink-0 border-r border-gray-300 bg-slate-50 px-3 py-2 flex items-center">
            <span class="text-sm font-medium text-slate-900">{{ table.tableNumber }}</span>
          </div>
          <div class="flex relative" style="min-height: 60px;">
            <div
              v-for="timeSlot in timeSlots"
              :key="timeSlot"
              class="w-16 flex-shrink-0 border-r border-gray-200 relative"
            ></div>
            <!-- Scheduled blocks for this table -->
            <div
              v-for="item in getTableItems(table.id)"
              :key="`table-${table.id}-${item.id}`"
              :style="getBlockStyle(item)"
              :class="getBlockClass(item)"
              class="absolute rounded px-2 py-1 text-xs font-medium text-white shadow-sm cursor-pointer hover:shadow-md transition-shadow"
              :title="getBlockTooltip(item)"
            >
              {{ getBlockLabel(item) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Legend -->
    <div class="mt-4 pt-4 border-t border-gray-200 flex flex-wrap gap-4">
      <div class="flex items-center gap-2">
        <div class="w-4 h-4 rounded bg-red-500"></div>
        <span class="text-xs text-slate-600">Đang ăn (Đã check-in)</span>
      </div>
      <div class="flex items-center gap-2">
        <div class="w-4 h-4 rounded bg-yellow-500"></div>
        <span class="text-xs text-slate-600">Chưa check-in</span>
      </div>
      <div class="flex items-center gap-2">
        <div class="w-4 h-4 rounded bg-green-500"></div>
        <span class="text-xs text-slate-600">Đã thanh toán</span>
      </div>
      <div class="flex items-center gap-2">
        <div class="w-4 h-4 rounded bg-gray-400"></div>
        <span class="text-xs text-slate-600">Đã hủy</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { reservationService } from '@/services/reservationService'
import { orderService } from '@/services/orderService'
import { tableService } from '@/services/tableService'

const props = defineProps({
  date: {
    type: String,
    default: null
  }
})

const loading = ref(false)
const reservations = ref([])
const orders = ref([])
const tables = ref([])
const selectedDate = ref(props.date || new Date().toISOString().split('T')[0])
const currentTime = ref(new Date()) // For real-time updates of active orders
let timeUpdateInterval = null

// Generate time slots (15-minute intervals from 10:00 to 22:00)
const timeSlots = computed(() => {
  const slots = []
  for (let hour = 10; hour <= 22; hour++) {
    for (let minute = 0; minute < 60; minute += 15) {
      const timeStr = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
      slots.push(timeStr)
    }
  }
  return slots
})

// Get start time in minutes from 10:00
function getTimeInMinutes(timeStr) {
  const [hours, minutes] = timeStr.split(':').map(Number)
  // Handle hours >= 24 (next day)
  const adjustedHours = hours >= 24 ? hours - 24 : hours
  return (adjustedHours - 10) * 60 + minutes
}

// Get slot index for a time
function getSlotIndex(timeStr) {
  return getTimeInMinutes(timeStr) / 15
}

// Get all schedule items (reservations + orders)
const scheduleItems = computed(() => {
  const items = []
  
    // Add reservations
  reservations.value.forEach(reservation => {
    if (!reservation.reservationTime) return
    
    // Find related order to get check-in time and completion time
    const relatedOrder = orders.value.find(o => o.reservationId === reservation.id)
    
    const reservationTime = new Date(reservation.reservationTime)
    const reservationTimeStr = reservationTime.toTimeString().slice(0, 5) // HH:MM
    
    let startTime = reservationTimeStr
    let endTime
    let confirmedAt = null
    
    // Get confirmedAt (check-in time) - ưu tiên từ reservation (đã được set khi chọn bàn)
    // Nếu reservation chưa có confirmedAt thì lấy từ order
    if (reservation.confirmedAt) {
      confirmedAt = reservation.confirmedAt
    } else if (relatedOrder && relatedOrder.confirmedAt) {
      confirmedAt = relatedOrder.confirmedAt
    }
    
    // Logic for different statuses:
    if (reservation.status === 'COMPLETED' && relatedOrder && relatedOrder.completedAt) {
      // Đã thanh toán: hiển thị từ lúc đặt bàn đến lúc thanh toán
      startTime = reservationTimeStr
      const completedTime = new Date(relatedOrder.completedAt)
      endTime = completedTime.toTimeString().slice(0, 5)
    } else if (reservation.status === 'CHECKED_IN' && confirmedAt) {
      // Đang ăn (đã check-in nhưng chưa thanh toán): hiển thị từ lúc đặt bàn đến thời điểm hiện tại
      // Sử dụng reservationTime làm startTime (thời gian đặt bàn ban đầu)
      startTime = reservationTimeStr
      // End time là thời điểm hiện tại (để thanh tiến trình mở rộng theo thời gian thực)
      const now = currentTime.value
      const nowTimeStr = now.toTimeString().slice(0, 5)
      
      // Đảm bảo endTime không nhỏ hơn startTime
      const startMinutes = getTimeInMinutes(startTime)
      const nowMinutes = getTimeInMinutes(nowTimeStr)
      if (nowMinutes > startMinutes) {
        endTime = nowTimeStr
      } else {
        // Nếu thời gian hiện tại nhỏ hơn startTime (có thể do ngày khác), dùng startTime + 15 phút
        const endMinutes = startMinutes + 15
        const totalMinutes = endMinutes + (10 * 60)
        const endHours = Math.floor(totalMinutes / 60) % 24
        const endMins = totalMinutes % 60
        endTime = `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
      }
    } else {
      // Chờ check-in (PENDING, CONFIRMED): chỉ hiển thị 1 slot (15 phút) tại đúng slot thời gian đặt bàn
      // Làm tròn xuống slot gần nhất (ví dụ: 23:05 -> 23:00, 23:20 -> 23:15)
      const [hours, minutes] = reservationTimeStr.split(':').map(Number)
      const slotMinutes = Math.floor(minutes / 15) * 15 // Làm tròn xuống bội số của 15
      const roundedStartTime = `${hours.toString().padStart(2, '0')}:${slotMinutes.toString().padStart(2, '0')}`
      startTime = roundedStartTime
      const startMinutes = getTimeInMinutes(roundedStartTime)
      const endMinutes = startMinutes + 15 // Chỉ 15 phút (1 slot)
      // Convert back to hours:minutes format
      const totalMinutes = endMinutes + (10 * 60) // Add back the 10:00 offset
      const endHours = Math.floor(totalMinutes / 60) % 24 // Handle 24-hour wrap
      const endMins = totalMinutes % 60
      endTime = `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
    }
    
    items.push({
      id: `reservation-${reservation.id}`,
      type: 'reservation',
      reservation,
      startTime,
      endTime,
      tableId: reservation.tableId,
      status: reservation.status,
      confirmedAt: confirmedAt,
      completedAt: relatedOrder?.completedAt || null
    })
  })
  
  // Add orders (walk-in, no reservation)
  orders.value.forEach(order => {
    if (!order.createdAt) return
    
    const orderTime = new Date(order.createdAt)
    const startTime = orderTime.toTimeString().slice(0, 5)
    
    // Logic for different statuses:
    let endTime
    if (order.status === 'COMPLETED' && order.completedAt) {
      // Đã thanh toán: hiển thị từ lúc tạo đơn đến lúc thanh toán
      const completedTime = new Date(order.completedAt)
      endTime = completedTime.toTimeString().slice(0, 5)
    } else if (order.confirmedAt) {
      // Đang ăn (đã check-in nhưng chưa thanh toán): hiển thị từ lúc tạo đơn đến thời điểm hiện tại
      // Sử dụng createdAt làm startTime (thời gian tạo đơn ban đầu)
      const now = currentTime.value
      const nowTimeStr = now.toTimeString().slice(0, 5)
      
      // Đảm bảo endTime không nhỏ hơn startTime
      const startMinutes = getTimeInMinutes(startTime)
      const nowMinutes = getTimeInMinutes(nowTimeStr)
      if (nowMinutes > startMinutes) {
        endTime = nowTimeStr
      } else {
        // Nếu thời gian hiện tại nhỏ hơn startTime, dùng startTime + 15 phút
        const endMinutes = startMinutes + 15
        const totalMinutes = endMinutes + (10 * 60)
        const endHours = Math.floor(totalMinutes / 60) % 24
        const endMins = totalMinutes % 60
        endTime = `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
      }
    } else {
      // Chờ check-in: chỉ hiển thị 1 slot (15 phút) tại đúng slot thời gian tạo đơn
      // Làm tròn xuống slot gần nhất (ví dụ: 23:05 -> 23:00, 23:20 -> 23:15)
      const [hours, minutes] = startTime.split(':').map(Number)
      const slotMinutes = Math.floor(minutes / 15) * 15 // Làm tròn xuống bội số của 15
      const roundedStartTime = `${hours.toString().padStart(2, '0')}:${slotMinutes.toString().padStart(2, '0')}`
      const startMinutes = getTimeInMinutes(roundedStartTime)
      const endMinutes = startMinutes + 15 // Chỉ 15 phút (1 slot)
      // Convert back to hours:minutes format
      const totalMinutes = endMinutes + (10 * 60) // Add back the 10:00 offset
      const endHours = Math.floor(totalMinutes / 60) % 24 // Handle 24-hour wrap
      const endMins = totalMinutes % 60
      endTime = `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
      
      // Cập nhật startTime để đảm bảo hiển thị đúng slot
      startTime = roundedStartTime
    }
    
    items.push({
      id: `order-${order.id}`,
      type: 'order',
      order,
      startTime,
      endTime,
      tableId: order.tableId,
      status: order.status,
      confirmedAt: order.confirmedAt,
      completedAt: order.completedAt
    })
  })
  
  return items
})

// Get unassigned items (no table) - only pending/confirmed reservations
const unassignedItems = computed(() => {
  return scheduleItems.value.filter(item => 
    !item.tableId && 
    (item.status === 'PENDING' || item.status === 'CONFIRMED') &&
    item.type === 'reservation' // Only show reservations, not orders without table
  )
})

// Get unassigned items grouped by time slot and stacked
function getUnassignedItemsStacked() {
  const items = unassignedItems.value
  
  // Group items by their start time slot
  const itemsBySlot = {}
  items.forEach(item => {
    const slotKey = item.startTime
    if (!itemsBySlot[slotKey]) {
      itemsBySlot[slotKey] = []
    }
    itemsBySlot[slotKey].push(item)
  })
  
  // Flatten and add stack index
  const stacked = []
  Object.keys(itemsBySlot).forEach(slotKey => {
    itemsBySlot[slotKey].forEach((item, index) => {
      stacked.push({
        ...item,
        stackIndex: index // Index within the same time slot
      })
    })
  })
  
  return stacked
}

// Get height for unassigned row based on max stacked items
function getUnassignedRowHeight() {
  const items = unassignedItems.value
  if (items.length === 0) return 60
  
  // Group by time slot to find max stack
  const itemsBySlot = {}
  items.forEach(item => {
    const slotKey = item.startTime
    if (!itemsBySlot[slotKey]) {
      itemsBySlot[slotKey] = []
    }
    itemsBySlot[slotKey].push(item)
  })
  
  // Find max stack size
  const maxStack = Math.max(...Object.values(itemsBySlot).map(arr => arr.length), 1)
  
  // Each item is ~52px (60px - 8px padding), add some spacing
  return Math.max(60, maxStack * 52 + 8)
}

// Get style for unassigned block with stacking
function getUnassignedBlockStyle(item, index) {
  const startSlot = getSlotIndex(item.startTime)
  const endSlot = getSlotIndex(item.endTime)
  const width = (endSlot - startSlot) * 64 // 64px per slot
  const left = startSlot * 64
  
  // Stack items vertically if they overlap (same startTime)
  const itemsInSameSlot = unassignedItems.value.filter(i => i.startTime === item.startTime)
  const stackIndex = itemsInSameSlot.findIndex(i => i.id === item.id)
  const top = 4 + (stackIndex * 52) // 52px per stacked item
  
  return {
    left: `${left}px`,
    width: `${width}px`,
    top: `${top}px`,
    height: '48px' // Fixed height for each item
  }
}

// Get items for a specific table
function getTableItems(tableId) {
  return scheduleItems.value.filter(item => item.tableId === tableId)
}

// Get block style (position and width)
function getBlockStyle(item) {
  const startSlot = getSlotIndex(item.startTime)
  const endSlot = getSlotIndex(item.endTime)
  const width = (endSlot - startSlot) * 64 // 64px per slot (w-16 = 4rem = 64px)
  const left = startSlot * 64
  
  return {
    left: `${left}px`,
    width: `${width}px`,
    top: '4px',
    height: 'calc(100% - 8px)'
  }
}

// Get block class (color based on status)
function getBlockClass(item) {
  // Đã hủy
  if (item.status === 'CANCELLED') {
    return 'bg-gray-400'
  }
  
  // Đã thanh toán (hoàn thành) - chỉ khi status là COMPLETED
  if (item.status === 'COMPLETED' && item.completedAt) {
    return 'bg-green-500'
  }
  
  // Đang ăn (đã check-in nhưng chưa thanh toán)
  // CHECKED_IN hoặc có confirmedAt (đã check-in) nhưng chưa completed
  if (item.status === 'CHECKED_IN' || (item.confirmedAt && !item.completedAt)) {
    return 'bg-red-500'
  }
  
  // Chờ check-in (PENDING, CONFIRMED) - màu vàng
  return 'bg-yellow-500'
}

// Get block label
function getBlockLabel(item) {
  if (item.type === 'reservation') {
    return `Đơn #${item.reservation.id}`
  } else {
    return `Đơn #${item.order.orderNumber?.slice(-1) || item.order.id}`
  }
}

// Get block tooltip
function getBlockTooltip(item) {
  let tooltip = ''
  if (item.type === 'reservation') {
    tooltip = `Đặt bàn #${item.reservation.id}\n`
    tooltip += `Khách: ${item.reservation.customerName || 'N/A'}\n`
    tooltip += `Thời gian: ${item.startTime} - ${item.endTime}\n`
    tooltip += `Trạng thái: ${getStatusLabel(item.status)}`
    if (item.confirmedAt) {
      const checkInTime = new Date(item.confirmedAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
      tooltip += `\nCheck-in: ${checkInTime}`
    }
  } else {
    tooltip = `Đơn hàng #${item.order.orderNumber || item.order.id}\n`
    tooltip += `Thời gian: ${item.startTime} - ${item.endTime}\n`
    tooltip += `Trạng thái: ${getStatusLabel(item.status)}`
    if (item.confirmedAt) {
      const checkInTime = new Date(item.confirmedAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
      tooltip += `\nCheck-in: ${checkInTime}`
    }
  }
  return tooltip
}

function getStatusLabel(status) {
  const labels = {
    'PENDING': 'Chờ xác nhận',
    'CONFIRMED': 'Đã xác nhận',
    'CHECKED_IN': 'Đã check-in',
    'COMPLETED': 'Hoàn thành',
    'CANCELLED': 'Đã hủy'
  }
  return labels[status] || status
}

async function loadSchedule() {
  loading.value = true
  try {
    const date = new Date(selectedDate.value)
    const startOfDay = new Date(date.setHours(0, 0, 0, 0))
    const endOfDay = new Date(date.setHours(23, 59, 59, 999))
    
    // Load reservations, orders, and tables in parallel
    const [reservationsRes, ordersRes, tablesRes] = await Promise.all([
      reservationService.getAll(),
      orderService.getByDate(startOfDay.toISOString(), endOfDay.toISOString()),
      tableService.getAll()
    ])
    
    // Filter reservations for selected date
    if (reservationsRes && reservationsRes.data) {
      const allReservations = Array.isArray(reservationsRes.data) ? reservationsRes.data : []
      reservations.value = allReservations.filter(r => {
        if (!r.reservationTime) return false
        const resDate = new Date(r.reservationTime)
        return resDate >= startOfDay && resDate <= endOfDay
      }).map(r => ({
        ...r,
        // Ensure tableId is set from table object if available
        tableId: r.tableId || (r.table && r.table.id) || null
      }))
    }
    
    // Filter orders for selected date
    if (ordersRes && ordersRes.data) {
      const allOrders = Array.isArray(ordersRes.data) ? ordersRes.data : []
      orders.value = allOrders.map(o => ({
        ...o,
        // Ensure tableId is set from table object if available
        tableId: o.tableId || (o.table && o.table.id) || null,
        reservationId: o.reservationId || (o.reservation && o.reservation.id) || null
      }))
    }
    
    // Get all tables
    if (tablesRes && tablesRes.data) {
      const allTables = Array.isArray(tablesRes.data) ? tablesRes.data : []
      // Get unique tables from reservations and orders
      const tableIds = new Set()
      reservations.value.forEach(r => {
        if (r.tableId) tableIds.add(r.tableId)
      })
      orders.value.forEach(o => {
        if (o.tableId) tableIds.add(o.tableId)
      })
      
      // Get tables that have reservations/orders today, plus all active tables
      tables.value = allTables.filter(t => 
        t.active !== false && (tableIds.has(t.id) || t.status !== 'MAINTENANCE')
      ).sort((a, b) => {
        // Sort by table number
        const numA = parseInt(a.tableNumber?.replace(/\D/g, '') || '0')
        const numB = parseInt(b.tableNumber?.replace(/\D/g, '') || '0')
        return numA - numB
      })
    }
  } catch (error) {
    console.error('Error loading schedule:', error)
  } finally {
    loading.value = false
  }
}

function refreshSchedule() {
  loadSchedule()
}

onMounted(() => {
  loadSchedule()
  // Update current time every minute for real-time progress bars
  timeUpdateInterval = setInterval(() => {
    currentTime.value = new Date()
  }, 60000) // Update every minute
})

onUnmounted(() => {
  if (timeUpdateInterval) {
    clearInterval(timeUpdateInterval)
  }
})
</script>

