<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto animate-slide-in">
      <!-- Header -->
      <div class="bg-gradient-to-r from-pink-500 to-pink-600 text-white px-6 py-4 rounded-t-2xl">
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

          <div>
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

        <div class="flex items-center gap-2">
          <input
            v-model="form.active"
            type="checkbox"
            id="active"
            class="w-4 h-4 text-pink-600 rounded focus:ring-pink-500"
          />
          <label for="active" class="text-sm font-medium text-gray-700">
            Kích hoạt ngay
          </label>
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
import { ref, watch } from 'vue'

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
  active: true
})

watch(() => props.promotion, (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
    // Format dates if needed
    if (newVal.startDate) {
      form.value.startDate = new Date(newVal.startDate).toISOString().split('T')[0]
    }
    if (newVal.endDate) {
      form.value.endDate = new Date(newVal.endDate).toISOString().split('T')[0]
    }
  }
}, { immediate: true })

function handleSubmit() {
  emit('save', form.value)
}
</script>
