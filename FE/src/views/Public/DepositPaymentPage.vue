<template>
  <div class="min-h-screen bg-gray-50 py-12">
    <div class="container mx-auto px-4">
      <div class="max-w-2xl mx-auto">
        <!-- Header -->
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">Thanh Toán Cọc</h1>
          <p class="text-gray-600">Thanh toán 20% tổng đơn món để hoàn tất đặt bàn</p>
        </div>

        <!-- Payment Card -->
        <div class="bg-white rounded-lg shadow-lg p-8">
          <!-- Reservation Info -->
          <div class="mb-6 pb-6 border-b border-gray-200">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">Thông tin đặt bàn</h2>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-600">Mã đặt bàn:</span>
                <span class="font-semibold text-gray-900">{{ reservationId || 'N/A' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Tổng đơn món:</span>
                <span class="font-semibold text-gray-900">{{ formatCurrency(totalAmount) }}đ</span>
              </div>
            </div>
          </div>

          <!-- Payment Amount -->
          <div class="mb-6 pb-6 border-b border-gray-200">
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div class="flex justify-between items-center mb-2">
                <span class="text-gray-700 font-medium">Số tiền cần thanh toán (20%):</span>
                <span class="text-2xl font-bold text-blue-600">{{ formatCurrency(depositAmount) }}đ</span>
              </div>
              <p class="text-xs text-gray-600 mt-2">
                <i class="fas fa-info-circle mr-1"></i>
                Số tiền còn lại sẽ thanh toán khi đến nhà hàng
              </p>
            </div>
          </div>

          <!-- Payment Method (Simple - just a button) -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-3">Phương thức thanh toán</label>
            <div class="bg-gray-50 border border-gray-200 rounded-lg p-4">
              <div class="flex items-center gap-3">
                <i class="fas fa-credit-card text-2xl text-gray-400"></i>
                <div>
                  <p class="font-medium text-gray-900">Thanh toán trực tuyến</p>
                  <p class="text-xs text-gray-600">Thanh toán ngay bằng cách nhấn nút bên dưới</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Payment Button -->
          <button
            @click="processPayment"
            :disabled="processing"
            class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-4 px-6 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <span v-if="!processing" class="flex items-center gap-2">
              <i class="fas fa-credit-card"></i>
              <span>Thanh Toán {{ formatCurrency(depositAmount) }}đ</span>
            </span>
            <span v-else class="flex items-center gap-2">
              <i class="fas fa-spinner fa-spin"></i>
              <span>Đang xử lý...</span>
            </span>
          </button>

          <!-- Note -->
          <p class="text-center text-xs text-gray-500 mt-4">
            Bằng việc thanh toán, bạn đồng ý với 
            <a href="#" class="text-blue-600 hover:underline">Điều khoản dịch vụ</a>
          </p>
        </div>
      </div>
    </div>

    <!-- Success Modal -->
    <Teleport to="body">
      <div
        v-if="showSuccess"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999] p-4"
        @click.self="closeSuccessModal"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-md mx-auto" @click.stop>
          <div class="p-6 text-center">
            <i class="fas fa-check-circle text-5xl mb-3 text-green-600"></i>
            <h3 class="text-xl font-bold text-gray-900 mb-2">Thanh Toán Thành Công!</h3>
            <p class="text-gray-700 mb-4 text-sm">
              Chúng tôi đã nhận được thanh toán cọc của bạn.<br/>
              <span v-if="reservationId">Mã đặt bàn: <strong class="text-gray-900">{{ reservationId }}</strong></span>
              <span v-else>Đặt bàn đã được tạo thành công.</span>
            </p>
            <p class="text-gray-600 mb-4 text-sm">
              Email xác nhận đã được gửi đến địa chỉ email của bạn.
            </p>
            <button
              @click="goToHome"
              class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-lg transition-colors"
            >
              Về Trang Chủ
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { reservationService } from '@/services/reservationService'
import { useNotificationStore } from '@/stores/notification'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const notification = useNotificationStore()
const authStore = useAuthStore()

const reservationId = ref(null)
const depositAmount = ref(0)
const totalAmount = ref(0)
const processing = ref(false)
const showSuccess = ref(false)
const reservationData = ref(null)

onMounted(() => {
  // Kiểm tra xem có reservationId (đã tạo reservation) hay có thông tin để tạo reservation mới
  const reservationIdParam = route.query.reservationId
  const depositAmountParam = route.query.depositAmount
  const totalAmountParam = route.query.totalAmount

  // Nếu có reservationId, đây là trường hợp thanh toán cọc cho reservation đã tạo
  if (reservationIdParam && depositAmountParam && totalAmountParam) {
    reservationId.value = parseInt(reservationIdParam)
    depositAmount.value = parseFloat(depositAmountParam)
    totalAmount.value = parseFloat(totalAmountParam)
    return
  }

  // Nếu không có reservationId nhưng có thông tin reservation, đây là trường hợp tạo reservation + thanh toán cọc
  const customerName = route.query.customerName
  const customerPhone = route.query.customerPhone
  const reservationDateTime = route.query.reservationDateTime
  const numberOfGuests = route.query.numberOfGuests
  const itemsStr = route.query.items

  if (!customerName || !customerPhone || !reservationDateTime || !numberOfGuests || !depositAmountParam || !totalAmountParam || !itemsStr) {
    notification.error('Thông tin đặt bàn không hợp lệ')
    router.push('/reservation')
    return
  }

  // Lấy userId nếu đã đăng nhập (ưu tiên từ query params, nếu không có thì lấy từ authStore)
  const userId = route.query.userId ? parseInt(route.query.userId) : (authStore.user?.id || null)

  // Lưu thông tin để tạo reservation khi thanh toán
  reservationData.value = {
    customerName: customerName,
    customerPhone: customerPhone,
    customerEmail: route.query.customerEmail || '',
    reservationDateTime: reservationDateTime,
    numberOfGuests: parseInt(numberOfGuests),
    notes: route.query.notes || '',
    tableId: route.query.tableId ? parseInt(route.query.tableId) : null,
    items: JSON.parse(itemsStr),
    depositAmount: parseFloat(depositAmountParam),
    userId: userId // Gửi userId nếu đã đăng nhập
  }

  depositAmount.value = parseFloat(depositAmountParam)
  totalAmount.value = parseFloat(totalAmountParam)
})

function formatCurrency(amount) {
  return new Intl.NumberFormat('vi-VN').format(amount)
}

async function processPayment() {
  processing.value = true
  try {
    let response

    // Nếu có reservationId, đây là thanh toán cọc cho reservation đã tạo
    if (reservationId.value) {
      if (!depositAmount.value) {
        notification.error('Thông tin thanh toán không hợp lệ')
        return
      }
      response = await reservationService.payDeposit(reservationId.value, depositAmount.value)
    } 
    // Nếu có reservationData, đây là tạo reservation mới + thanh toán cọc
    else if (reservationData.value) {
      // Chuyển đổi depositAmount từ number sang string để gửi API
      const requestData = {
        ...reservationData.value,
        depositAmount: depositAmount.value.toString()
      }
      response = await reservationService.createWithDeposit(requestData)
      // Lưu reservationId từ response
      if (response.success && response.data?.id) {
        reservationId.value = response.data.id
      }
    } else {
      notification.error('Thông tin thanh toán không hợp lệ')
      return
    }
    
    if (response.success) {
      showSuccess.value = true
      notification.success('Thanh toán cọc thành công!')
    } else {
      notification.error(response.message || 'Thanh toán thất bại. Vui lòng thử lại!')
    }
  } catch (error) {
    console.error('Payment error:', error)
    const errorMessage = error.response?.data?.message || error.message || 'Có lỗi xảy ra, vui lòng thử lại!'
    notification.error(errorMessage)
  } finally {
    processing.value = false
  }
}

function closeSuccessModal() {
  showSuccess.value = false
  goToHome()
}

function goToHome() {
  router.push('/home')
}
</script>

