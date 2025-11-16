<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[10001] p-4"
      @click.self="handleClose"
    >
      <div class="bg-white rounded-lg shadow-2xl w-[50vw] md:w-[40vw] max-h-[90vh] overflow-hidden flex flex-col">
        <div class="bg-slate-900 text-white px-6 py-4 flex items-center justify-between">
          <h3 class="text-xl font-bold">Chọn khuyến mãi</h3>
          <button @click="handleClose" class="text-white/80 hover:text-white transition-colors">
            <i class="fas fa-times text-xl"></i>
          </button>
        </div>

        <div class="flex-1 overflow-y-auto p-6">
          <div class="space-y-2">
            <!-- Option: Không áp dụng khuyến mãi -->
            <label class="flex items-start gap-3 p-3 border border-slate-200 rounded-lg cursor-pointer hover:bg-slate-50 transition-colors">
              <input
                type="radio"
                :value="null"
                :checked="tempSelectedPromotion === null"
                @change="handleSelect(null)"
                class="mt-1 w-4 h-4 text-blue-600 border-slate-300 focus:ring-blue-500"
              />
              <div class="flex-1">
                <p class="text-sm font-medium text-slate-900">Không áp dụng khuyến mãi</p>
              </div>
            </label>
            
            <!-- List promotions -->
            <label
              v-for="promotion in promotions"
              :key="promotion.id"
              class="flex items-start gap-3 p-3 border rounded-lg transition-colors"
              :class="isPromotionEligible(promotion) 
                ? 'border-slate-200 cursor-pointer hover:bg-slate-50' 
                : 'border-slate-200 bg-slate-100 opacity-60 cursor-not-allowed'"
            >
              <input
                type="radio"
                :value="promotion.id"
                :checked="tempSelectedPromotion?.id === promotion.id"
                @change="handleSelect(promotion)"
                :disabled="!isPromotionEligible(promotion)"
                class="mt-1 w-4 h-4 text-blue-600 border-slate-300 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
              />
              <div class="flex-1">
                <div class="flex items-center justify-between">
                  <p class="text-sm font-medium text-slate-900">
                    {{ promotion.name || promotion.promotionName }}
                  </p>
                  <span class="text-sm font-bold text-red-600">
                    {{ promotion.discountPercent 
                      ? `${promotion.discountPercent}%` 
                      : formatCurrency(promotion.discountAmount) }}
                  </span>
                </div>
                <p v-if="promotion.description" class="text-xs text-slate-600 mt-1">
                  {{ promotion.description }}
                </p>
                <p v-if="promotion.minOrderValue" class="text-xs mt-1"
                   :class="isPromotionEligible(promotion) ? 'text-slate-500' : 'text-red-500'">
                  <i class="fas fa-info-circle mr-1"></i>
                  Đơn hàng tối thiểu: {{ formatCurrency(promotion.minOrderValue) }}
                  <span v-if="!isPromotionEligible(promotion)" class="font-medium">
                    (Chưa đủ điều kiện)
                  </span>
                </p>
              </div>
            </label>
          </div>
        </div>

        <div class="border-t border-slate-200 bg-slate-50 px-6 py-4 flex items-center justify-end gap-3">
          <button
            @click="handleClose"
            class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
          >
            Hủy
          </button>
          <button
            @click="handleConfirm"
            class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
          >
            Xác nhận
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  modelValue: {
    type: Object,
    default: null
  },
  promotions: {
    type: Array,
    default: () => []
  },
  orderSubtotal: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

// Lưu promotion được chọn tạm thời (chưa xác nhận)
const tempSelectedPromotion = ref(null)

// Khi dialog mở, khởi tạo tempSelectedPromotion từ modelValue
watch(() => props.show, (newVal) => {
  if (newVal) {
    tempSelectedPromotion.value = props.modelValue
  }
})

// Kiểm tra promotion có đủ điều kiện không
function isPromotionEligible(promotion) {
  if (!promotion || !props.orderSubtotal) return false
  
  // Kiểm tra minOrderValue
  if (promotion.minOrderValue && props.orderSubtotal < promotion.minOrderValue) {
    return false
  }
  
  return true
}

// Xử lý chọn promotion (chỉ lưu tạm, chưa cập nhật)
function handleSelect(promotion) {
  tempSelectedPromotion.value = promotion
}

// Xử lý xác nhận - mới thực sự cập nhật
function handleConfirm() {
  emit('update:modelValue', tempSelectedPromotion.value)
  emit('close')
}

// Xử lý đóng dialog (không xác nhận) - không cập nhật
function handleClose() {
  // Reset về giá trị ban đầu
  tempSelectedPromotion.value = props.modelValue
  emit('close')
}

// Format currency
function formatCurrency(value) {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}
</script>

