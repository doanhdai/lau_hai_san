<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-lg w-full mx-4 animate-slide-in">
      <!-- Header -->
      <div class="bg-gradient-to-r from-red-500 to-red-600 text-white px-6 py-4 rounded-t-2xl">
        <h3 class="text-xl font-bold">{{ table ? 'Chỉnh sửa bàn' : 'Thêm bàn mới' }}</h3>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Số bàn <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.tableNumber"
              type="text"
              required
              :class="['input-field', tableNumberError ? 'border-red-500 focus:ring-red-500' : '']"
              placeholder="T-01"
              @blur="checkDuplicateTableNumber"
            />
            <p v-if="tableNumberError" class="text-red-500 text-xs mt-1">
              {{ tableNumberError }}
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Sức chứa <span class="text-red-500">*</span>
            </label>
            <input
              v-model.number="form.capacity"
              type="number"
              required
              min="1"
              class="input-field"
              placeholder="4"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Trạng thái
            </label>
            <select v-model="form.status" class="input-field">
              <option value="AVAILABLE">Trống</option>
              <option value="OCCUPIED">Đang phục vụ</option>
              <option value="RESERVED">Đã đặt</option>
              <option value="MAINTENANCE">Bảo trì</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Vị trí
            </label>
            <input
              v-model="form.location"
              type="text"
              class="input-field"
              placeholder="Tầng 1 - Khu A"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Ghi chú
          </label>
          <textarea
            v-model="form.notes"
            rows="2"
            class="input-field"
            placeholder="Ghi chú về bàn..."
          ></textarea>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <button type="button" @click="$emit('close')" class="btn-secondary">
            Hủy
          </button>
          <button type="submit" class="btn-primary">
            {{ table ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useNotificationStore } from '@/stores/notification'

const notification = useNotificationStore()

const props = defineProps({
  table: {
    type: Object,
    default: null
  },
  tables: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'save'])

const form = ref({
  tableNumber: '',
  capacity: 4,
  status: 'AVAILABLE',
  location: '',
  notes: ''
})

const tableNumberError = ref('')

watch(() => props.table, (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
  } else {
    // Reset form when creating new table
    form.value = {
      tableNumber: '',
      capacity: 4,
      status: 'AVAILABLE',
      location: '',
      notes: ''
    }
  }
  tableNumberError.value = ''
}, { immediate: true })

watch(() => form.value.tableNumber, () => {
  // Clear error when user types
  if (tableNumberError.value) {
    tableNumberError.value = ''
  }
})

function checkDuplicateTableNumber() {
  if (!form.value.tableNumber || !form.value.tableNumber.trim()) {
    tableNumberError.value = 'Vui lòng nhập số bàn'
    return false
  }

  const tableNumber = form.value.tableNumber.trim()
  
  // Check if table number already exists (excluding current table if editing)
  const existingTable = props.tables.find(t => {
    // Exclude deleted tables and current table if editing
    if (t.isDeleted) return false
    if (props.table && props.table.id && t.id === props.table.id) return false
    return t.tableNumber && t.tableNumber.trim().toLowerCase() === tableNumber.toLowerCase()
  })

  if (existingTable) {
    tableNumberError.value = `Số bàn "${tableNumber}" đã tồn tại`
    return false
  }

  tableNumberError.value = ''
  return true
}

function handleSubmit() {
  // Clear previous error
  tableNumberError.value = ''

  // Validate table number
  if (!checkDuplicateTableNumber()) {
    return
  }

  // Validate capacity
  if (!form.value.capacity || form.value.capacity < 1) {
    notification.error('Sức chứa phải lớn hơn 0')
    return
  }

  emit('save', form.value)
}
</script>
