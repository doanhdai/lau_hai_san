<template>
  <div class="fixed inset-0 bg-black/70 flex items-center justify-center z-50 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto animate-slide-in">
      <!-- Header -->
      <div class="bg-slate-900 text-white px-6 py-4 rounded-t-2xl">
        <h3 class="text-xl font-bold">{{ promotion ? 'Chỉnh sửa khuyến mãi' : 'Thêm khuyến mãi mới' }}</h3>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Tên chương trình <span class="text-red-500">*</span>
          </label>
          <input
            v-model="form.promotionName"
            type="text"
            required
            class="input-field"
            placeholder="Giảm giá cuối tuần"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Mô tả
          </label>
          <textarea
            v-model="form.description"
            rows="2"
            class="input-field"
            placeholder="Mô tả chi tiết chương trình..."
          ></textarea>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Loại giảm giá
            </label>
            <select v-model="form.discountType" class="input-field">
              <option value="PERCENTAGE">Phần trăm (%)</option>
              <option value="FIXED">Số tiền cố định (VND)</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Giá trị giảm <span class="text-red-500">*</span>
            </label>
            <input
              v-model.number="form.discountValue"
              type="number"
              required
              min="0"
              :max="form.discountType === 'PERCENTAGE' ? 100 : undefined"
              class="input-field"
              :placeholder="form.discountType === 'PERCENTAGE' ? '10' : '50000'"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Ngày bắt đầu <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.startDate"
              type="date"
              required
              :min="minStartDate"
              class="input-field"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Ngày kết thúc <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.endDate"
              type="date"
              required
              :min="minEndDate"
              class="input-field"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Giá trị đơn tối thiểu
            </label>
            <input
              v-model.number="form.minOrderValue"
              type="number"
              min="0"
              class="input-field"
              placeholder="200000"
            />
          </div>

          <div v-if="form.discountType === 'PERCENTAGE'">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Giảm tối đa (VND)
            </label>
            <input
              v-model.number="form.maxDiscount"
              type="number"
              min="0"
              class="input-field"
              placeholder="100000"
            />
          </div>
        </div>


        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <button type="button" @click="$emit('close')" class="btn-secondary">
            Hủy
          </button>
          <button type="submit" class="btn-primary">
            {{ promotion ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  promotion: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'save'])

const form = ref({
  promotionName: '',
  description: '',
  discountType: 'PERCENTAGE',
  discountValue: 0,
  startDate: '',
  endDate: '',
  minOrderValue: 0,
  maxDiscount: 0,
  active: true // Mặc định là true (hoạt động) khi tạo mới
})

// Computed min dates for validation
const minStartDate = computed(() => {
  const today = new Date()
  // Get local date in Vietnam timezone (UTC+7)
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

const minEndDate = computed(() => {
  if (form.value.startDate) {
    return form.value.startDate
  }
  return minStartDate.value
})

// Watch startDate to validate endDate
watch(() => form.value.startDate, (newStartDate) => {
  if (newStartDate && form.value.endDate) {
    const startDate = new Date(newStartDate)
    const endDate = new Date(form.value.endDate)
    
    // Nếu ngày kết thúc < ngày bắt đầu, reset ngày kết thúc
    if (endDate < startDate) {
      form.value.endDate = newStartDate
    }
  }
})

// Helper function to extract date from datetime string (avoid timezone issues)
function extractDate(dateString) {
  if (!dateString) return ''
  
  // If it's already in YYYY-MM-DD format, return as is
  if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
    return dateString
  }
  
  // Extract date part from ISO string or datetime string
  // Handle formats like: "2025-11-15T00:00:00" or "2025-11-15T00:00:00.000Z" or "2025-11-15 00:00:00"
  const dateMatch = dateString.match(/^(\d{4}-\d{2}-\d{2})/)
  if (dateMatch) {
    return dateMatch[1]
  }
  
  // Fallback: try Date object but adjust for timezone
  try {
    const date = new Date(dateString)
    // Get local date components to avoid timezone shift
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch (e) {
    return ''
  }
}

watch(() => props.promotion, (newVal) => {
  if (newVal) {
    // Map backend response to form format
    form.value = {
      promotionName: newVal.promotionName || newVal.name || '',
      description: newVal.description || '',
      discountType: newVal.discountType || (newVal.discountPercent ? 'PERCENTAGE' : 'FIXED') || 'PERCENTAGE',
      discountValue: newVal.discountValue || newVal.discountPercent || newVal.discountAmount || 0,
      startDate: extractDate(newVal.startDate),
      endDate: extractDate(newVal.endDate),
      minOrderValue: newVal.minOrderValue || 0,
      maxDiscount: newVal.maxDiscount || 0,
      active: newVal.active !== undefined ? newVal.active : true // Mặc định true khi edit
    }
  } else {
    // Reset form when no promotion (create new)
    form.value = {
      promotionName: '',
      description: '',
      discountType: 'PERCENTAGE',
      discountValue: 0,
      startDate: '',
      endDate: '',
      minOrderValue: 0,
      maxDiscount: 0,
      active: true
    }
  }
}, { immediate: true })

function handleSubmit() {
  // Validate: ngày bắt đầu phải <= ngày kết thúc
  if (form.value.startDate && form.value.endDate) {
    const startDate = new Date(form.value.startDate)
    const endDate = new Date(form.value.endDate)
    
    if (startDate > endDate) {
      alert('Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc!')
      return
    }
  }
  
  emit('save', form.value)
}
</script>
