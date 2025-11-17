<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Thanh toán đơn hàng</h1>
        <p class="text-slate-600 mt-1 text-sm">Xác nhận và thanh toán đơn hàng</p>
      </div>
      <button 
        @click="handleBack"
        class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors flex items-center gap-2"
      >
        <i class="fas fa-arrow-left"></i>
        <span>Quay lại</span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loadingOrder" class="flex flex-col items-center justify-center py-12 bg-white rounded-lg shadow-sm">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin mb-4"></div>
      <p class="text-sm text-slate-600 font-medium">Đang tải thông tin đơn hàng...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
      <p class="text-red-600 mb-4">{{ error }}</p>
      <button 
        @click="handleBack"
        class="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors"
      >
        Quay lại
      </button>
    </div>

    <!-- Payment Content - Layout 2 cột -->
    <div v-else-if="selectedOrder" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Cột 1: Thông tin khách hàng và sản phẩm (2/3 width) -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Customer Info -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h3 class="text-lg font-semibold text-slate-900 mb-4 flex items-center gap-2">
            <i class="fas fa-user text-slate-600"></i>
            <span>Thông tin khách hàng</span>
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p class="text-xs text-slate-500 mb-1">Họ tên</p>
              <p class="text-sm font-medium text-slate-900">{{ customerInfo?.fullName || selectedOrder.customerName || 'Khách vãng lai' }}</p>
            </div>
            <div v-if="customerInfo?.phone || selectedOrder.customerPhone">
              <p class="text-xs text-slate-500 mb-1">Số điện thoại</p>
              <p class="text-sm font-medium text-slate-700">{{ customerInfo?.phone || selectedOrder.customerPhone }}</p>
            </div>
            <div v-if="customerInfo?.email">
              <p class="text-xs text-slate-500 mb-1">Email</p>
              <p class="text-sm font-medium text-slate-700">{{ customerInfo.email }}</p>
            </div>
            <div v-if="customerInfo?.address">
              <p class="text-xs text-slate-500 mb-1">Địa chỉ</p>
              <p class="text-sm font-medium text-slate-700">{{ customerInfo.address }}</p>
            </div>
            <div v-if="selectedOrder.tableNumber">
              <p class="text-xs text-slate-500 mb-1">Bàn</p>
              <p class="text-sm font-medium text-slate-700">{{ selectedOrder.tableNumber }}</p>
            </div>
            <div v-if="customerInfo?.isVip" class="flex items-center">
              <span class="px-3 py-1 text-xs font-semibold text-yellow-700 bg-yellow-100 rounded-full">
                <i class="fas fa-crown mr-1"></i>
                VIP
              </span>
            </div>
          </div>
        </div>

        <!-- Order Items -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h3 class="text-lg font-semibold text-slate-900 mb-4">Danh sách món</h3>
          <div class="space-y-3">
            <div
              v-for="item in selectedOrder.items"
              :key="item.id"
              class="flex items-center justify-between p-4 bg-slate-50 rounded-lg border border-slate-200"
            >
              <div class="flex-1">
                <p class="text-sm font-medium text-slate-900 mb-1">{{ item.dishName }}</p>
                <p class="text-xs text-slate-500">Số lượng: {{ item.quantity }} x {{ formatCurrency(item.price) }}</p>
              </div>
              <p class="text-sm font-bold text-slate-900 ml-4">{{ formatCurrency(item.subtotal) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Cột 2: Thanh toán (1/3 width) -->
      <div class="lg:col-span-1 space-y-6">
        <!-- Payment Summary Card -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h3 class="text-lg font-semibold text-slate-900 mb-4">Thanh toán</h3>
          
          <!-- Order Summary -->
          <div class="space-y-3 mb-6 pb-6 border-b border-slate-200">
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
          </div>

          <!-- Total Amount -->
          <div class="mb-6 pb-6 border-b border-slate-200">
            <div class="flex justify-between items-center">
              <span class="text-base font-bold text-slate-900">Tổng cộng:</span>
              <span class="text-2xl font-bold text-red-600">{{ formatCurrency(finalTotal) }}</span>
            </div>
          </div>

          <!-- Promotion Selection -->
          <div class="mb-6 pb-6 border-b border-slate-200">
            <h4 class="text-sm font-semibold text-slate-900 mb-3">Khuyến mãi</h4>
            <div v-if="selectedPromotion" class="p-3 bg-green-50 border border-green-200 rounded-lg mb-3">
              <div class="flex items-center justify-between">
                <div class="flex-1">
                  <p class="text-sm font-medium text-green-900">
                    {{ selectedPromotion.name || selectedPromotion.promotionName }}
                  </p>
                </div>
                <span class="text-sm font-medium text-green-600 ml-2">
                  {{ selectedPromotion.discountPercent 
                    ? `${selectedPromotion.discountPercent}%` 
                    : formatCurrency(selectedPromotion.discountAmount) }}
                </span>
              </div>
            </div>
            <button
              @click="showPromotionDialog = true"
              class="w-full px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 transition-colors flex items-center justify-center gap-2"
            >
              <i class="fas fa-tag"></i>
              <span>{{ selectedPromotion ? 'Đổi khuyến mãi' : 'Chọn khuyến mãi' }}</span>
            </button>
          </div>

          <!-- Payment Method -->
          <div class="mb-6">
            <h4 class="text-sm font-semibold text-slate-900 mb-3">Phương thức thanh toán</h4>
            <div class="space-y-2">
              <label class="flex items-center gap-3 p-3 border-2 rounded-lg cursor-pointer transition-all"
                     :class="paymentMethod === 'CASH' ? 'border-blue-500 bg-blue-50' : 'border-slate-200 hover:border-slate-300'">
                <input
                  type="radio"
                  v-model="paymentMethod"
                  value="CASH"
                  class="w-4 h-4 text-blue-600"
                  @change="qrCodeUrl = ''"
                />
                <div class="flex-1">
                  <p class="font-medium text-slate-900 text-sm">Tiền mặt</p>
                  <p class="text-xs text-slate-500">Thanh toán bằng tiền mặt</p>
                </div>
              </label>
              <label class="flex items-center gap-3 p-3 border-2 rounded-lg cursor-pointer transition-all"
                     :class="paymentMethod === 'BANK_TRANSFER' ? 'border-blue-500 bg-blue-50' : 'border-slate-200 hover:border-slate-300'">
                <input
                  type="radio"
                  v-model="paymentMethod"
                  value="BANK_TRANSFER"
                  class="w-4 h-4 text-blue-600"
                />
                <div class="flex-1">
                  <p class="font-medium text-slate-900 text-sm">Chuyển khoản</p>
                  <p class="text-xs text-slate-500">Thanh toán qua ngân hàng</p>
                </div>
              </label>
            </div>
          </div>

          <!-- QR Code Button for Bank Transfer -->
          <div v-if="paymentMethod === 'BANK_TRANSFER' && qrCodeUrl" class="mb-4">
            <button
              @click="openQRModal"
              class="w-full px-2 py-1 text-sm font-medium text-blue-600 rounded-lg transition-colors flex items-center justify-center gap-2"
            >
              <i class="fas fa-qrcode text-lg"></i>
              <span>Xem mã QR thanh toán</span>
            </button>
          </div>

          <!-- Payment Button -->
          <button
            @click="handlePayment"
            :disabled="loadingPayment || (paymentMethod === 'BANK_TRANSFER' && !qrCodeUrl)"
            class="w-full px-4 py-3 text-base font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <span v-if="loadingPayment" class="inline-block animate-spin">
              <i class="fas fa-spinner"></i>
            </span>
            <i v-else class="fas fa-check-circle"></i>
            <span>{{ loadingPayment ? 'Đang xử lý...' : 'Xác nhận thanh toán' }}</span>
          </button>
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

    <!-- QR Code Modal -->
    <Teleport to="body">
      <div
        v-if="showQRModal"
        class="fixed top-0 left-0 right-0 bottom-0 z-[9999] bg-black bg-opacity-50 overflow-y-auto"
        @click.self="closeQRModal"
        style="margin: 0; padding: 0;"
      >
        <div class="min-h-screen flex items-center justify-center p-4">
          <div class="bg-white rounded-xl shadow-2xl max-w-4xl w-full p-6 animate-scale-in flex flex-col max-h-[calc(100vh-2rem)] overflow-y-auto">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6 flex-shrink-0">
          <h3 class="text-xl font-bold text-slate-900 flex items-center gap-2">
            <i class="fas fa-qrcode text-blue-600"></i>
            <span>QR thanh toán</span>
          </h3>
          <button
            @click="closeQRModal"
            class="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <i class="fas fa-times text-slate-500"></i>
          </button>
        </div>

        <!-- QR Code Content - Layout ngang -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 flex-1 items-start">
          <!-- QR Code Image - Cột trái -->
          <div class="flex flex-col items-center justify-center">
            <div class="flex justify-center items-center bg-white p-2 rounded-lg border-2 border-slate-200 w-full">
              <img 
                :src="qrCodeUrl" 
                alt="QR Code" 
                class="w-full max-w-xs h-auto object-contain"
              />
            </div>
          </div>

          <!-- Payment Info - Cột phải -->
          <div class="flex flex-col justify-center">
            <div class="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-lg p-5 border border-blue-200">
              <h4 class="text-base font-semibold text-slate-900 mb-4 flex items-center gap-2">
                <i class="fas fa-info-circle text-blue-600"></i>
                <span>Thông tin chuyển khoản</span>
              </h4>
              <div class="space-y-3 text-sm">
                <div class="flex justify-between items-center">
                  <span class="text-slate-600">Số tài khoản:</span>
                  <span class="font-semibold text-slate-900 text-base">{{ bankAccountNumber }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-600">Chủ tài khoản:</span>
                  <span class="font-semibold text-slate-900 text-base">{{ bankAccountName }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-600">Ngân hàng:</span>
                  <span class="font-semibold text-slate-900 text-base">{{ bankName }}</span>
                </div>
                <div class="flex justify-between items-center pt-3 border-t border-blue-200">
                  <span class="text-slate-600 font-medium">Số tiền:</span>
                  <span class="text-xl font-bold text-red-600">{{ formatCurrency(finalTotal) }}</span>
                </div>
                <div class="pt-3 border-t border-blue-200">
                  <p class="text-xs text-slate-500 mb-1">Nội dung chuyển khoản:</p>
                  <p class="text-sm font-medium text-slate-700 italic break-words">Thanh toan don hang {{ selectedOrder?.orderNumber }}</p>
                </div>
              </div>
            </div>
            <!-- Instructions -->
            <div class="mt-4 bg-yellow-50 border border-yellow-200 rounded-lg p-3 w-full">
              <p class="text-xs text-yellow-800 flex items-start gap-2">
                <i class="fas fa-lightbulb text-yellow-600 mt-0.5"></i>
                <span>Mở app ngân hàng của bạn và quét mã QR để thanh toán tự động</span>
              </p>
            </div>
          </div>
        </div>

        <!-- Close Button -->
        <!-- <div class="mt-3 flex justify-end flex-shrink-0">
          <button
            @click="closeQRModal"
            class="px-6 py-2.5 text-sm font-medium rounded-lg border hover:border-black transition-colors"
          >
            Thoát
          </button>
        </div> -->
          </div>
        </div>
      </div>
    </Teleport>
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

// Thông tin ngân hàng - có thể thay đổi dễ dàng
const bankAccountNumber = ref('1234567890')
const bankAccountName = ref('NHÀ HÀNG LẨU HẢI SẢN')
const bankName = ref('Vietcombank')
const bankCode = ref('VCB') // Xem bank code tại https://api.vietqr.io/v2/banks

const selectedOrder = ref(null)
const customerInfo = ref(null)
const loadingOrder = ref(false)
const loadingPayment = ref(false)
const showPromotionDialog = ref(false)
const showQRModal = ref(false)
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
    const amount = finalTotal.value
    const note = `Thanh toan don hang ${selectedOrder.value.orderNumber}`
    
    qrCodeUrl.value = `https://img.vietqr.io/image/${bankCode.value}-${bankAccountNumber.value}-compact2.png?amount=${amount}&addInfo=${encodeURIComponent(note)}`
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

function openQRModal() {
  showQRModal.value = true
}

function closeQRModal() {
  showQRModal.value = false
}

function formatCurrency(value) {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-scale-in {
  animation: scaleIn 0.3s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
