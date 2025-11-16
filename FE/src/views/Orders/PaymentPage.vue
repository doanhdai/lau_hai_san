<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header đơn giản không có sidebar -->
    <div class="sticky top-0 bg-slate-900 text-white shadow-md">
      <div class="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between">
        <h1 class="text-xl font-bold flex items-center gap-2">
          <i class="fas fa-bowl-food text-2xl"></i>
          <span>Thanh toán đơn hàng</span>
        </h1>
        <button 
          @click="handleBack"
          class="text-white/80 hover:text-white transition-colors flex items-center gap-2"
        >
          <i class="fas fa-arrow-left"></i>
          <span>Quay lại</span>
        </button>
      </div>
    </div>

    <!-- Content -->
    <div class="max-w-4xl mx-auto px-6 py-6">
      <!-- Loading State -->
      <div v-if="loadingOrder" class="flex flex-col items-center justify-center py-12">
        <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin mb-4"></div>
        <p class="text-sm text-slate-600 font-medium">Đang tải thông tin đơn hàng...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-center">
        <p class="text-red-600">{{ error }}</p>
        <button 
          @click="handleBack"
          class="mt-4 px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors"
        >
          Quay lại
        </button>
      </div>

      <!-- Payment Content -->
      <div v-else-if="selectedOrder" class="space-y-6">
        <!-- Step 1: Order Summary -->
        <div v-if="!showPaymentStep2" class="bg-white rounded-lg shadow-md p-6 space-y-6">
          <!-- Customer Info -->
          <div class="bg-slate-50 rounded-lg p-4">
            <h4 class="font-semibold text-slate-900 mb-3 flex items-center gap-2">
              <i class="fas fa-user text-slate-600"></i>
              <span>Thông tin khách hàng</span>
            </h4>
            <div class="space-y-2">
              <div class="flex items-start gap-3">
                <div class="flex-1">
                  <p class="text-xs text-slate-500 mb-0.5">Họ tên</p>
                  <p class="text-sm font-medium text-slate-900">{{ customerInfo?.fullName || selectedOrder.customerName || 'Khách vãng lai' }}</p>
                </div>
              </div>
              <div v-if="customerInfo?.phone || selectedOrder.customerPhone" class="flex items-start gap-3">
                <div class="flex-1">
                  <p class="text-xs text-slate-500 mb-0.5">Số điện thoại</p>
                  <p class="text-sm font-medium text-slate-700">{{ customerInfo?.phone || selectedOrder.customerPhone }}</p>
                </div>
              </div>
              <div v-if="customerInfo?.email" class="flex items-start gap-3">
                <div class="flex-1">
                  <p class="text-xs text-slate-500 mb-0.5">Email</p>
                  <p class="text-sm font-medium text-slate-700">{{ customerInfo.email }}</p>
                </div>
              </div>
              <div v-if="customerInfo?.address" class="flex items-start gap-3">
                <div class="flex-1">
                  <p class="text-xs text-slate-500 mb-0.5">Địa chỉ</p>
                  <p class="text-sm font-medium text-slate-700">{{ customerInfo.address }}</p>
                </div>
              </div>
              <div v-if="selectedOrder.tableNumber" class="flex items-start gap-3">
                <div class="flex-1">
                  <p class="text-xs text-slate-500 mb-0.5">Bàn</p>
                  <p class="text-sm font-medium text-slate-700">{{ selectedOrder.tableNumber }}</p>
                </div>
              </div>
              <div v-if="customerInfo?.isVip" class="flex items-center gap-2 mt-2">
                <span class="px-2 py-1 text-xs font-semibold text-yellow-700 bg-yellow-100 rounded-full">
                  <i class="fas fa-crown mr-1"></i>
                  VIP
                </span>
              </div>
            </div>
          </div>

          <!-- Order Items -->
          <div>
            <h4 class="text-sm font-semibold text-slate-900 mb-3">Danh sách món</h4>
            <div class="space-y-2">
              <div
                v-for="item in selectedOrder.items"
                :key="item.id"
                class="flex items-center justify-between p-3 bg-slate-50 rounded-lg"
              >
                <div class="flex-1">
                  <p class="text-sm font-medium text-slate-900">{{ item.dishName }}</p>
                  <p class="text-xs text-slate-500">SL: {{ item.quantity }} x {{ formatCurrency(item.price) }}</p>
                </div>
                <p class="text-sm font-bold text-slate-900">{{ formatCurrency(item.subtotal) }}</p>
              </div>
            </div>
          </div>

          <!-- Promotion Selection -->
          <div>
            <h4 class="text-sm font-semibold text-slate-900 mb-3">Khuyến mãi</h4>
            <div class="flex items-center gap-3">
              <div v-if="selectedPromotion" class="flex-1 p-2 bg-green-50 border border-green-200 rounded-lg">
                <div class="flex items-center justify-between">
                  <div class="flex-1">
                    <p class="text-sm font-medium text-green-900">
                      {{ selectedPromotion.name || selectedPromotion.promotionName }}
                    </p>
                  </div>
                  <span class="text-sm font-medium text-green-600 ml-3">
                    {{ selectedPromotion.discountPercent 
                      ? `${selectedPromotion.discountPercent}%` 
                      : formatCurrency(selectedPromotion.discountAmount) }}
                  </span>
                </div>
              </div>
              <div v-else class="flex-1 p-2 bg-slate-50 border border-slate-200 rounded-lg">
                <p class="text-sm text-slate-600">Chưa chọn khuyến mãi</p>
              </div>
              <button
                @click="showPromotionDialog = true"
                class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 whitespace-nowrap"
              >
                <i class="fas fa-tag"></i>
                <span>{{ selectedPromotion ? 'Đổi khuyến mãi' : 'Chọn khuyến mãi' }}</span>
              </button>
            </div>
          </div>

          <!-- Order Summary -->
          <div class="bg-gradient-to-br from-slate-50 to-slate-100 rounded-lg p-4 border border-slate-200">
            <h4 class="text-sm font-semibold text-slate-900 mb-3">Tổng kết</h4>
            <div class="space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-slate-600">Tạm tính:</span>
                <span class="font-medium text-slate-900">{{ formatCurrency(selectedOrder.subtotal) }}</span>
              </div>
              <div v-if="selectedOrder.discount && selectedOrder.discount > 0" class="flex justify-between text-sm">
                <span class="text-slate-600">Giảm giá hiện tại:</span>
                <span class="font-medium text-red-600">-{{ formatCurrency(selectedOrder.discount) }}</span>
              </div>
              <div v-if="selectedPromotion" class="flex justify-between text-sm">
                <span class="text-slate-600">Giảm giá khuyến mãi:</span>
                <span class="font-medium text-red-600">-{{ formatCurrency(getPromotionDiscount()) }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-slate-600">Thuế (10%):</span>
                <span class="font-medium text-slate-900">{{ formatCurrency(selectedOrder.tax) }}</span>
              </div>
              <div class="border-t border-slate-300 pt-2 mt-2">
                <div class="flex justify-between">
                  <span class="text-base font-bold text-slate-900">Tổng cộng:</span>
                  <span class="text-lg font-bold text-red-600">{{ formatCurrency(finalTotal) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center justify-end gap-3 pt-4 border-t border-slate-200">
            <button
              @click="handleBack"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
            >
              Quay lại
            </button>
            <button
              @click="goToPaymentStep2"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Tiếp tục
            </button>
          </div>
        </div>

        <!-- Step 2: Payment Method -->
        <div v-else class="bg-white rounded-lg shadow-md p-6 space-y-6">
          <h3 class="text-xl font-bold text-slate-900">Chọn phương thức thanh toán</h3>

          <div class="space-y-4">
            <!-- Payment Method Selection -->
            <div>
              <h4 class="text-sm font-semibold text-slate-900 mb-3">Phương thức thanh toán</h4>
              <div class="space-y-2">
                <label class="flex items-center gap-3 p-4 border-2 rounded-lg cursor-pointer transition-all"
                       :class="paymentMethod === 'CASH' ? 'border-blue-500 bg-blue-50' : 'border-slate-200 hover:border-slate-300'">
                  <input
                    type="radio"
                    v-model="paymentMethod"
                    value="CASH"
                    class="w-4 h-4 text-blue-600"
                    @change="qrCodeUrl = ''"
                  />
                  <div class="flex-1">
                    <p class="font-medium text-slate-900">Tiền mặt</p>
                    <p class="text-xs text-slate-500">Thanh toán bằng tiền mặt</p>
                  </div>
                </label>
                <label class="flex items-center gap-3 p-4 border-2 rounded-lg cursor-pointer transition-all"
                       :class="paymentMethod === 'BANK_TRANSFER' ? 'border-blue-500 bg-blue-50' : 'border-slate-200 hover:border-slate-300'">
                  <input
                    type="radio"
                    v-model="paymentMethod"
                    value="BANK_TRANSFER"
                    class="w-4 h-4 text-blue-600"
                  />
                  <div class="flex-1">
                    <p class="font-medium text-slate-900">Chuyển khoản</p>
                    <p class="text-xs text-slate-500">Thanh toán qua chuyển khoản ngân hàng</p>
                  </div>
                </label>
              </div>
            </div>

            <!-- QR Code for Bank Transfer -->
            <div v-if="paymentMethod === 'BANK_TRANSFER' && qrCodeUrl" class="bg-slate-50 rounded-lg p-4">
              <h4 class="text-sm font-semibold text-slate-900 mb-3">Quét mã QR để thanh toán</h4>
              <div class="flex flex-col items-center">
                <img :src="qrCodeUrl" alt="QR Code" class="w-64 h-auto border-2 border-slate-200 rounded-lg mb-3" />
                <div class="text-center text-xs text-slate-600 space-y-1">
                  <p>Số tài khoản: <span class="font-semibold">1234567890</span></p>
                  <p>Chủ tài khoản: <span class="font-semibold">NHÀ HÀNG LẨU HẢI SẢN</span></p>
                  <p>Ngân hàng: <span class="font-semibold">Vietcombank</span></p>
                  <p>Số tiền: <span class="font-semibold text-red-600">{{ formatCurrency(finalTotal) }}</span></p>
                  <p class="text-slate-500 italic">Nội dung: Thanh toan don hang {{ selectedOrder?.orderNumber }}</p>
                </div>
              </div>
            </div>

            <!-- Total Amount -->
            <div class="bg-gradient-to-br from-slate-50 to-slate-100 rounded-lg p-4 border border-slate-200">
              <div class="flex justify-between items-center">
                <span class="text-base font-bold text-slate-900">Tổng thanh toán:</span>
                <span class="text-xl font-bold text-red-600">{{ formatCurrency(finalTotal) }}</span>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center justify-between gap-3 pt-4 border-t border-slate-200">
            <button
              @click="backToPaymentStep1"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
            >
              Quay lại
            </button>
            <button
              @click="handlePayment"
              :disabled="loadingPayment || (paymentMethod === 'BANK_TRANSFER' && !qrCodeUrl)"
              class="px-4 py-2 text-sm font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              <span v-if="loadingPayment" class="inline-block animate-spin">
                <i class="fas fa-spinner"></i>
              </span>
              <span>{{ loadingPayment ? 'Đang xử lý...' : 'Đã nhận' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Promotion Selection Dialog -->
    <PromotionSelectionDialog
      :show="showPromotionDialog"
      v-model="selectedPromotion"
      :promotions="promotions"
      :order-subtotal="selectedOrder?.subtotal || 0"
      @close="showPromotionDialog = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderService } from '@/services/orderService'
import { promotionService } from '@/services/promotionService'
import { paymentService } from '@/services/paymentService'
import { customerService } from '@/services/customerService'
import { useNotificationStore } from '@/stores/notification'
import PromotionSelectionDialog from '@/components/modals/PromotionSelectionDialog.vue'

const route = useRoute()
const router = useRouter()
const notification = useNotificationStore()

const SERVED_KEYWORD = 'served'

const selectedOrder = ref(null)
const customerInfo = ref(null)
const loadingOrder = ref(false)
const loadingPayment = ref(false)
const showPaymentStep2 = ref(false)
const showPromotionDialog = ref(false)
const selectedPromotion = ref(null)
const paymentMethod = ref('CASH')
const promotions = ref([])
const qrCodeUrl = ref('')
const error = ref(null)

// Load order khi component mount
onMounted(async () => {
  const orderId = route.params.orderId || route.query.orderId
  if (orderId) {
    await loadOrder(orderId)
    await loadPromotions()
  } else {
    error.value = 'Không tìm thấy đơn hàng'
  }
})

async function loadOrder(orderId) {
  loadingOrder.value = true
  error.value = null
  try {
    const response = await orderService.getById(orderId)
    if (response.success) {
      selectedOrder.value = response.data
      // Load customer info nếu có customerId
      if (response.data.customerId) {
        await loadCustomerInfo(response.data.customerId)
      }
    } else {
      error.value = 'Không thể tải thông tin đơn hàng'
    }
  } catch (err) {
    console.error('Error loading order:', err)
    error.value = 'Không thể tải thông tin đơn hàng: ' + (err.response?.data?.message || err.message)
  } finally {
    loadingOrder.value = false
  }
}

async function loadCustomerInfo(customerId) {
  try {
    const response = await customerService.getById(customerId)
    if (response.success) {
      customerInfo.value = response.data
    }
  } catch (err) {
    console.error('Error loading customer info:', err)
    // Không hiển thị lỗi nếu không load được customer info
  }
}

async function loadPromotions() {
  try {
    const response = await promotionService.getActive()
    if (response.success) {
      promotions.value = response.data || []
    }
  } catch (error) {
    console.error('Error loading promotions:', error)
  }
}

function goToPaymentStep2() {
  if (!selectedOrder.value) return
  showPaymentStep2.value = true
  if (paymentMethod.value === 'BANK_TRANSFER') {
    generateQRCode()
  }
}

function backToPaymentStep1() {
  showPaymentStep2.value = false
}

// Tính toán tổng tiền sau khi áp dụng khuyến mãi
const finalTotal = computed(() => {
  if (!selectedOrder.value) return 0
  
  let subtotal = selectedOrder.value.subtotal || 0
  let tax = selectedOrder.value.tax || 0
  let discount = selectedOrder.value.discount || 0
  
  // Tính discount từ promotion
  if (selectedPromotion.value) {
    const promotion = selectedPromotion.value
    if (!promotion.minOrderValue || subtotal >= promotion.minOrderValue) {
      let promotionDiscount = 0
      
      if (promotion.discountPercent) {
        promotionDiscount = (subtotal * promotion.discountPercent) / 100
        const maxDiscount = promotion.maxDiscount || promotionDiscount
        promotionDiscount = Math.min(promotionDiscount, maxDiscount)
      } else if (promotion.discountAmount) {
        promotionDiscount = promotion.discountAmount
      }
      
      discount = discount + promotionDiscount
    }
  }
  
  const total = subtotal - discount + tax
  return Math.max(0, total)
})

// Watch paymentMethod để tự động generate QR code
watch(() => paymentMethod.value, (newVal) => {
  if (newVal === 'BANK_TRANSFER') {
    generateQRCode()
  } else {
    qrCodeUrl.value = ''
  }
})

// Tính discount từ promotion
function getPromotionDiscount() {
  if (!selectedOrder.value || !selectedPromotion.value) return 0
  
  const promotion = selectedPromotion.value
  const subtotal = selectedOrder.value.subtotal || 0
  
  if (promotion.minOrderValue && subtotal < promotion.minOrderValue) {
    return 0
  }
  
  if (promotion.discountPercent) {
    const discount = (subtotal * promotion.discountPercent) / 100
    const maxDiscount = promotion.maxDiscount || discount
    return Math.min(discount, maxDiscount)
  } else if (promotion.discountAmount) {
    return promotion.discountAmount
  }
  
  return 0
}

// Tạo QR code cho chuyển khoản
async function generateQRCode() {
  if (!selectedOrder.value || paymentMethod.value !== 'BANK_TRANSFER') return
  
  try {
    const accountNumber = '1234567890'
    const accountName = 'NHÀ HÀNG LẨU HẢI SẢN'
    const bankCode = 'VCB'
    const amount = finalTotal.value
    const note = `Thanh toan don hang ${selectedOrder.value.orderNumber}`
    
    qrCodeUrl.value = `https://img.vietqr.io/image/${bankCode}-${accountNumber}-compact2.png?amount=${amount}&addInfo=${encodeURIComponent(note)}`
  } catch (error) {
    console.error('Error generating QR code:', error)
    notification.error('Không thể tạo QR code')
  }
}

// Xử lý thanh toán
async function handlePayment() {
  if (!selectedOrder.value || loadingPayment.value) return
  
  loadingPayment.value = true
  
  try {
    // Xóa SERVED_KEYWORD khỏi notes của tất cả order_details
    if (selectedOrder.value.items && selectedOrder.value.items.length > 0) {
      for (const item of selectedOrder.value.items) {
        if (item.notes && item.notes.includes(SERVED_KEYWORD)) {
          const regex = new RegExp(SERVED_KEYWORD, 'gi')
          const cleanedNotes = item.notes.replace(regex, '').trim()
          const updateItemData = {
            dishId: item.dishId || item.dish?.id,
            quantity: item.quantity,
            notes: cleanedNotes || null
          }
          await orderService.updateOrderDetail(selectedOrder.value.id, item.id, updateItemData)
        }
      }
    }
    
    // Tạo payment
    const paymentData = {
      orderId: selectedOrder.value.id,
      amount: finalTotal.value,
      paymentMethod: paymentMethod.value,
      paymentStatus: 'COMPLETED',
      notes: selectedPromotion.value ? `Áp dụng khuyến mãi: ${selectedPromotion.value.name || selectedPromotion.value.promotionName}` : null
    }
    
    try {
      await paymentService.create(paymentData)
    } catch (error) {
      console.warn('Payment API not available, skipping payment creation:', error)
    }
    
    // Cập nhật discount và total nếu có promotion
    if (selectedPromotion.value) {
      const promotionDiscount = getPromotionDiscount()
      const newDiscount = (selectedOrder.value.discount || 0) + promotionDiscount
      const newTotal = finalTotal.value
      
      const updateData = {
        notes: selectedOrder.value.notes || '',
        discount: newDiscount,
        total: newTotal
      }
      await orderService.update(selectedOrder.value.id, updateData)
    }
    
    // Cập nhật order status thành COMPLETED
    await orderService.updateStatus(selectedOrder.value.id, 'COMPLETED')
    
    notification.success('Thanh toán thành công')
    
    // Redirect về trang check-in sau 1.5 giây
    setTimeout(() => {
      router.push('/admin/reservations/checkin')
    }, 1500)
  } catch (error) {
    console.error('Error processing payment:', error)
    notification.error('Không thể xử lý thanh toán: ' + (error.response?.data?.message || error.message))
  } finally {
    loadingPayment.value = false
  }
}

function handleBack() {
  router.push('/admin/reservations/checkin')
}

function formatCurrency(value) {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}
</script>

