<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999] animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto animate-slide-in">
      <!-- Header -->
      <div class="bg-gradient-to-r from-indigo-500 to-indigo-600 text-white px-6 py-4 rounded-t-2xl">
        <h3 class="text-xl font-bold">{{ reservation ? 'Chỉnh sửa đặt bàn' : 'Thêm đặt bàn mới' }}</h3>
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
              class="input-field"
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
              class="input-field"
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
              class="input-field"
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
              class="input-field"
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
              class="input-field"
              placeholder="4"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Bàn <span class="text-red-500">*</span>
            </label>
            <select v-model="form.tableId" required class="input-field" :disabled="loadingTables || !form.reservationDate || !form.reservationTime">
              <option value="">{{ loadingTables ? 'Đang tải...' : (!form.reservationDate || !form.reservationTime ? 'Vui lòng chọn ngày và giờ trước' : '-- Chọn bàn --') }}</option>
              <option v-for="table in availableTables" :key="table.id" :value="table.id">
                {{ table.tableNumber }} ({{ table.capacity }} người)
              </option>
            </select>
          </div>

          <div v-if="reservation">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Trạng thái
            </label>
            <select v-model="form.status" class="input-field">
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
            class="input-field"
            placeholder="Yêu cầu đặc biệt..."
          ></textarea>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <button type="button" @click="$emit('close')" class="btn-secondary">
            Hủy
          </button>
          <button type="submit" class="btn-primary">
            {{ reservation ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { tableService } from '@/services/tableService'
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

watch(() => props.reservation, (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
    // Format datetime if needed
    if (newVal.reservationDateTime || newVal.reservationTime) {
      const dt = new Date(newVal.reservationDateTime || newVal.reservationTime)
      form.value.reservationDate = dt.toISOString().split('T')[0]
      form.value.reservationTime = dt.toTimeString().slice(0, 5)
    }
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
