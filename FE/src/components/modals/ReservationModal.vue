<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999] animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto animate-slide-in">
      <!-- Header -->
      <div class="bg-gradient-to-r from-indigo-500 to-indigo-600 text-white px-6 py-4 rounded-t-2xl">
        <h3 class="text-xl font-bold">{{ reservation ? 'Xem đặt bàn' : 'Thêm đặt bàn mới' }}</h3>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Tên khách hàng <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.customerName"
              type="text"
              required
              disabled
              class="input-field opacity-60 cursor-not-allowed"
              placeholder="Nguyễn Văn A"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Số điện thoại <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.customerPhone"
              type="tel"
              required
              disabled
              class="input-field opacity-60 cursor-not-allowed"
              placeholder="0912345678"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Ngày đặt <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.reservationDate"
              type="date"
              required
              :min="minDate"
              disabled
              class="input-field opacity-60 cursor-not-allowed"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Giờ đặt <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.reservationTime"
              type="time"
              required
              :min="minTime"
              disabled
              class="input-field opacity-60 cursor-not-allowed"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Số người <span class="text-red-500">*</span>
            </label>
            <input
              v-model.number="form.numberOfGuests"
              type="number"
              required
              min="1"
              disabled
              class="input-field opacity-60 cursor-not-allowed"
              placeholder="4"
            />
          </div>

          <div v-if="reservation">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Trạng thái
            </label>
            <select v-model="form.status" disabled class="input-field opacity-60 cursor-not-allowed">
              <option value="PENDING">Chờ xác nhận</option>
              <option value="CONFIRMED">Đã xác nhận</option>
              <option value="CANCELLED">Đã hủy</option>
              <option value="COMPLETED">Hoàn thành</option>
            </select>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Ghi chú
          </label>
          <textarea
            v-model="form.notes"
            rows="3"
            disabled
            class="input-field opacity-60 cursor-not-allowed"
            placeholder="Yêu cầu đặc biệt..."
          ></textarea>
        </div>

        <!-- Order Items Section -->
        <div v-if="reservation && (orders.length > 0 || loadingOrders)" class="border-t pt-4">
          <h4 class="text-lg font-semibold text-gray-900 mb-4">Món đã đặt</h4>
          
          <div v-if="loadingOrders" class="text-center py-4">
            <i class="fas fa-spinner fa-spin text-gray-400"></i>
            <p class="text-sm text-gray-500 mt-2">Đang tải...</p>
          </div>
          
          <div v-else-if="orders.length === 0" class="text-center py-4">
            <p class="text-sm text-gray-500">Chưa có món nào được đặt</p>
          </div>
          
          <div v-else class="space-y-4">
            <div
              v-for="order in orders"
              :key="order.id"
              class="bg-gray-50 rounded-lg p-4 border border-gray-200"
            >
              <div class="flex items-center justify-between mb-3 pb-3 border-b border-gray-200">
                <div>
                  <p class="text-sm font-semibold text-gray-900">Đơn hàng: {{ order.orderNumber }}</p>
                  <p class="text-xs text-gray-500 mt-1">
                    Trạng thái: 
                    <span :class="getStatusBadgeClass(order.status)" class="ml-1">
                      {{ getStatusLabel(order.status) }}
                    </span>
                  </p>
                </div>
                <div class="text-right">
                  <p class="text-sm font-bold text-gray-900">{{ formatCurrency(order.total) }}</p>
                </div>
              </div>
              
              <div class="space-y-2">
                <div
                  v-for="item in order.items"
                  :key="item.id"
                  class="flex items-start gap-3 p-3 bg-white rounded border border-gray-100"
                >
                  <!-- Dish Image -->
                  <div class="flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden bg-gray-200 border border-gray-300">
                    <img
                      v-if="getDishImage(item)"
                      :src="getDishImage(item)"
                      :alt="item.dishName || 'Dish'"
                      class="w-full h-full object-cover"
                      @error="handleImageError"
                    />
                    <div v-else class="w-full h-full flex items-center justify-center bg-gray-100">
                      <i :class="['fas', getDishIcon(item.dishName || '')]" class="text-2xl text-gray-400"></i>
                    </div>
                  </div>
                  
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-gray-900 mb-1">
                      {{ item.dishName || 'Món không xác định' }}
                    </p>
                    <p class="text-xs text-gray-500">
                      Số lượng: {{ item.quantity }} x {{ formatCurrency(item.price) }}
                    </p>
                    <p v-if="item.notes" class="text-xs text-gray-400 italic mt-1">
                      Ghi chú: {{ item.notes }}
                    </p>
                  </div>
                  <div class="text-right ml-4 flex-shrink-0">
                    <p class="text-sm font-semibold text-gray-900">
                      {{ formatCurrency(item.subtotal || (item.price * item.quantity)) }}
                    </p>
                  </div>
                </div>
              </div>
              
              <div v-if="order.items && order.items.length > 0" class="mt-3 pt-3 border-t border-gray-200">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Tạm tính:</span>
                  <span class="font-medium text-gray-900">{{ formatCurrency(order.subtotal) }}</span>
                </div>
                <div v-if="order.tax && order.tax > 0" class="flex justify-between text-sm mt-1">
                  <span class="text-gray-600">Thuế (10%):</span>
                  <span class="font-medium text-gray-900">{{ formatCurrency(order.tax) }}</span>
                </div>
                <div class="flex justify-between text-sm mt-2 pt-2 border-t border-gray-200">
                  <span class="font-semibold text-gray-900">Tổng cộng:</span>
                  <span class="font-bold text-lg text-gray-900">{{ formatCurrency(order.total) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <button type="button" @click="$emit('close')" class="btn-primary">
            Đóng
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { tableService } from '@/services/tableService'
import { orderService } from '@/services/orderService'
import { validatePhone } from '@/utils/validation'
import { useNotificationStore } from '@/stores/notification'

const notification = useNotificationStore()

const props = defineProps({
  reservation: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'save'])

const availableTables = ref([])
const loadingTables = ref(false)
const orders = ref([])
const loadingOrders = ref(false)

const form = ref({
  customerName: '',
  customerPhone: '',
  reservationDate: '',
  reservationTime: '',
  numberOfGuests: null,
  tableId: '',
  status: 'PENDING',
  notes: ''
})

// Get current date and time for validation
const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const minTime = computed(() => {
  // If selected date is today, set minimum time to current time
  if (form.value.reservationDate === minDate.value) {
    const now = new Date()
    return now.toTimeString().slice(0, 5)
  }
  return '00:00'
})

watch(() => props.reservation, async (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
    // Format datetime if needed
    if (newVal.reservationDateTime || newVal.reservationTime) {
      const dt = new Date(newVal.reservationDateTime || newVal.reservationTime)
      form.value.reservationDate = dt.toISOString().split('T')[0]
      form.value.reservationTime = dt.toTimeString().slice(0, 5)
    }
    // Load orders for this reservation
    await loadOrders(newVal.id)
  } else {
    // Reset form to default values for new reservation
    const today = new Date().toISOString().split('T')[0]
    form.value = {
      customerName: '',
      customerPhone: '',
      reservationDate: today,
      reservationTime: '',
      numberOfGuests: null,
      tableId: '',
      status: 'PENDING', // Always default to PENDING for new reservations
      notes: ''
    }
    orders.value = []
  }
}, { immediate: true })

// Watch for date and time changes to load available tables
watch([() => form.value.reservationDate, () => form.value.reservationTime], async ([date, time]) => {
  // Only load tables if both date and time are selected
  if (date && time) {
    await loadAvailableTablesByTime()
  } else {
    // Clear tables if date or time is not selected
    availableTables.value = []
    form.value.tableId = ''
  }
})

async function loadAvailableTablesByTime() {
  if (!form.value.reservationDate || !form.value.reservationTime) return
  
  loadingTables.value = true
  try {
    // Combine date and time to ISO format (treat as UTC to avoid timezone conversion)
    const reservationDateTime = `${form.value.reservationDate}T${form.value.reservationTime}:00.000Z`
    
    const response = await tableService.getAvailableByTime(reservationDateTime)
    if (response.success) {
      availableTables.value = response.data
      // Reset table selection if current selection is not in the new list
      if (form.value.tableId && !availableTables.value.find(t => t.id === form.value.tableId)) {
        form.value.tableId = ''
      }
    }
  } catch (error) {
    console.error('Không thể tải danh sách bàn theo thời gian:', error)
    availableTables.value = []
  } finally {
    loadingTables.value = false
  }
}

async function loadOrders(reservationId) {
  if (!reservationId) {
    orders.value = []
    return
  }
  
  loadingOrders.value = true
  try {
    const response = await orderService.getByReservationId(reservationId)
    if (response.success) {
      orders.value = response.data || []
    } else {
      orders.value = []
    }
  } catch (error) {
    console.error('Error loading orders:', error)
    orders.value = []
  } finally {
    loadingOrders.value = false
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value || 0)
}

function getStatusLabel(status) {
  const labels = {
    'PENDING': 'Chờ xử lý',
    'CONFIRMED': 'Đã xác nhận',
    'PREPARING': 'Đang chuẩn bị',
    'SERVED': 'Đã phục vụ',
    'COMPLETED': 'Hoàn thành',
    'CANCELLED': 'Đã hủy'
  }
  return labels[status] || status
}

function getStatusBadgeClass(status) {
  const classes = {
    'PENDING': 'px-2 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800',
    'CONFIRMED': 'px-2 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-800',
    'PREPARING': 'px-2 py-1 rounded-full text-xs font-semibold bg-orange-100 text-orange-800',
    'SERVED': 'px-2 py-1 rounded-full text-xs font-semibold bg-purple-100 text-purple-800',
    'COMPLETED': 'px-2 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800',
    'CANCELLED': 'px-2 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800'
  }
  return classes[status] || 'px-2 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800'
}

// Lấy hình ảnh món ăn
function getDishImage(item) {
  if (item && item.dishImageUrl && item.dishImageUrl.trim() !== '') {
    return item.dishImageUrl
  }
  return null
}

// Lấy icon mặc định cho món ăn
function getDishIcon(dishName) {
  if (!dishName) return 'fa-utensils'
  const name = dishName.toLowerCase()
  if (name.includes('lẩu thai') || name.includes('thái')) return 'fa-bowl-food'
  if (name.includes('tôm')) return 'fa-shrimp'
  if (name.includes('bò')) return 'fa-drumstick-bite'
  if (name.includes('hải sản')) return 'fa-fish'
  if (name.includes('nấm')) return 'fa-seedling'
  if (name.includes('rau')) return 'fa-leaf'
  if (name.includes('mì') || name.includes('bún')) return 'fa-bowl-rice'
  if (name.includes('nước') || name.includes('trà') || name.includes('bia')) return 'fa-glass-water'
  if (name.includes('cá')) return 'fa-fish'
  if (name.includes('cua')) return 'fa-crab'
  if (name.includes('gà')) return 'fa-drumstick-bite'
  if (name.includes('heo') || name.includes('sườn')) return 'fa-drumstick-bite'
  return 'fa-utensils'
}

// Xử lý lỗi khi load ảnh
function handleImageError(event) {
  const img = event.target
  const parent = img.parentElement
  if (parent) {
    img.style.display = 'none'
    const iconPlaceholder = document.createElement('div')
    iconPlaceholder.className = 'w-full h-full flex items-center justify-center bg-gray-100'
    const dishName = img.alt || 'Dish'
    const iconClass = getDishIcon(dishName)
    iconPlaceholder.innerHTML = `<i class="fas ${iconClass} text-2xl text-gray-400"></i>`
    parent.appendChild(iconPlaceholder)
  }
}

onMounted(async () => {
  // Initialize with current date and PENDING status if creating new reservation
  if (!props.reservation) {
    form.value.reservationDate = minDate.value
    form.value.status = 'PENDING' // Ensure status is PENDING for new reservations
  } else {
    // If editing, load tables for the existing reservation time
    if (form.value.reservationDate && form.value.reservationTime) {
      await loadAvailableTablesByTime()
    }
    // Load orders if reservation exists
    if (props.reservation.id) {
      await loadOrders(props.reservation.id)
    }
  }
})

function handleSubmit() {
  // Validate customer name
  if (!form.value.customerName.trim()) {
    notification.error('Vui lòng nhập tên khách hàng')
    return
  }

  // Validate phone (required)
  const phoneError = validatePhone(form.value.customerPhone, true)
  if (phoneError) {
    notification.error(phoneError)
    return
  }

  // Validate number of guests
  if (!form.value.numberOfGuests || form.value.numberOfGuests < 1) {
    notification.error('Vui lòng nhập số người hợp lệ (ít nhất 1 người)')
    return
  }

  // Validate table selection
  if (!form.value.tableId) {
    notification.error('Vui lòng chọn bàn')
    return
  }

  // Combine date and time - format directly as ISO string to avoid timezone conversion
  const reservationDateTime = `${form.value.reservationDate}T${form.value.reservationTime}:00.000Z`
  
  // Validate that the datetime is not in the past (create Date object for comparison only)
  const datetime = new Date(reservationDateTime)
  if (datetime < new Date()) {
    notification.error('Không thể đặt bàn cho thời gian đã qua. Vui lòng chọn thời gian trong tương lai.')
    return
  }
  
  const data = {
    ...form.value,
    reservationDateTime: reservationDateTime
  }
  
  // Ensure status is PENDING when creating new reservation
  if (!props.reservation) {
    data.status = 'PENDING'
  }
  
  delete data.reservationDate
  delete data.reservationTime
  emit('save', data)
}
</script>
