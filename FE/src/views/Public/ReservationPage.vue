<template>
  <div>
    <!-- Hero -->
    <section class="relative h-96 flex items-center justify-center bg-gradient-to-br from-sky-600 to-blue-700">
      <div class="relative z-10 text-center text-white px-4">
        <h1 class="text-5xl md:text-6xl font-bold mb-4">Đặt Bàn Online</h1>
        <p class="text-xl">Đặt bàn nhanh chóng, nhận ưu đãi ngay</p>
      </div>
    </section>

    <!-- Reservation Form -->
    <section class="py-20 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="max-w-3xl mx-auto">
          <div class="bg-white rounded-2xl shadow-2xl p-8 md:p-12">
            <div class="text-center mb-8">
              <h2 class="text-3xl font-bold text-gray-900 mb-2">Thông Tin Đặt Bàn</h2>
              <p class="text-gray-600">Vui lòng điền đầy đủ thông tin</p>
            </div>

            <form @submit.prevent="submitReservation" class="space-y-6">
              <!-- Personal Info -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Họ và tên <span class="text-red-600">*</span>
                  </label>
                  <input
                    v-model="form.name"
                    type="text"
                    required
                    class="input-field"
                    placeholder="Nguyễn Văn A"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Số điện thoại <span class="text-red-600">*</span>
                  </label>
                  <input
                    v-model="form.phone"
                    type="tel"
                    required
                    class="input-field"
                    placeholder="0123456789"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
                <input
                  v-model="form.email"
                  type="email"
                  class="input-field"
                  placeholder="email@example.com"
                />
              </div>

              <!-- Reservation Details -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Ngày <span class="text-red-600">*</span>
                  </label>
                  <input
                    v-model="form.date"
                    type="date"
                    required
                    :min="minDate"
                    class="input-field"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Giờ <span class="text-red-600">*</span>
                  </label>
                  <select v-model="form.time" required class="input-field">
                    <option value="">Chọn giờ</option>
                    <option v-for="time in timeSlots" :key="time" :value="time">{{ time }}</option>
                  </select>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Số người <span class="text-red-600">*</span>
                  </label>
                  <select v-model="form.guests" required class="input-field">
                    <option value="">Chọn số người</option>
                    <option v-for="n in 20" :key="n" :value="n">{{ n }} người</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Loại bàn</label>
                  <select v-model="form.tableType" class="input-field">
                    <option value="normal">Bàn thường</option>
                    <option value="vip">Phòng VIP</option>
                  </select>
                </div>
              </div>

              <!-- Special Requests -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Yêu cầu đặc biệt</label>
                <textarea
                  v-model="form.notes"
                  rows="4"
                  class="input-field"
                  placeholder="Vị trí gần cửa sổ, sinh nhật, kỷ niệm..."
                ></textarea>
              </div>

              <!-- Promotions -->
              <div class="bg-sky-50 border-2 border-sky-200 rounded-lg p-4">
                <div class="flex items-start gap-3">
                  <input
                    type="checkbox"
                    v-model="form.acceptPromo"
                    id="promo"
                    class="mt-1"
                  />
                  <label for="promo" class="text-sm text-gray-700">
                    <span class="font-bold text-sky-600">Nhận ưu đãi 20%</span> cho lần đặt bàn đầu tiên và các chương trình khuyến mãi qua email/SMS
                  </label>
                </div>
              </div>

              <!-- Submit -->
              <button
                type="submit"
                :disabled="submitting"
                class="w-full btn-primary py-4 text-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="!submitting">🎉 Xác Nhận Đặt Bàn</span>
                <span v-else>Đang xử lý...</span>
              </button>

              <p class="text-center text-sm text-gray-500">
                Bằng việc đặt bàn, bạn đồng ý với 
                <a href="#" class="text-sky-600 hover:underline">Điều khoản dịch vụ</a>
              </p>
            </form>
          </div>

          <!-- Success Message -->
          <div v-if="showSuccess" class="mt-8 bg-green-50 border-2 border-green-500 rounded-2xl p-8 text-center animate-fade-in-up">
            <div class="text-6xl mb-4">✅</div>
            <h3 class="text-2xl font-bold text-gray-900 mb-2">Đặt Bàn Thành Công!</h3>
            <p class="text-gray-700 mb-4">
              Chúng tôi đã nhận được yêu cầu đặt bàn của bạn.<br/>
              Mã đặt bàn: <strong class="text-sky-600">{{ reservationCode }}</strong>
            </p>
            <p class="text-gray-600 mb-6">
              Vui lòng kiểm tra email/SMS để xác nhận. Chúng tôi sẽ liên hệ trong vòng 15 phút.
            </p>
            <button @click="resetForm" class="btn-primary px-8 py-3">
              Đặt Bàn Mới
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Why Book Online -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16">
          <h2 class="text-4xl font-bold text-gray-900 mb-4">Tại Sao Đặt Bàn Online?</h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div class="text-center group">
            <div class="w-20 h-20 bg-gradient-to-br from-sky-500 to-blue-600 rounded-full flex items-center justify-center text-3xl mx-auto mb-4 transition-transform duration-300 group-hover:scale-110 group-hover:shadow-xl">
              ⚡
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-2">Nhanh Chóng</h3>
            <p class="text-gray-600">Chỉ 2 phút hoàn tất</p>
          </div>
          <div class="text-center group">
            <div class="w-20 h-20 bg-gradient-to-br from-sky-500 to-blue-600 rounded-full flex items-center justify-center text-3xl mx-auto mb-4 transition-transform duration-300 group-hover:scale-110 group-hover:shadow-xl">
              💰
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-2">Ưu Đãi</h3>
            <p class="text-gray-600">Giảm 20% lần đầu</p>
          </div>
          <div class="text-center group">
            <div class="w-20 h-20 bg-gradient-to-br from-sky-500 to-blue-600 rounded-full flex items-center justify-center text-3xl mx-auto mb-4 transition-transform duration-300 group-hover:scale-110 group-hover:shadow-xl">
              ✅
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-2">Đảm Bảo</h3>
            <p class="text-gray-600">Luôn có chỗ</p>
          </div>
          <div class="text-center group">
            <div class="w-20 h-20 bg-gradient-to-br from-sky-500 to-blue-600 rounded-full flex items-center justify-center text-3xl mx-auto mb-4 transition-transform duration-300 group-hover:scale-110 group-hover:shadow-xl">
              📱
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-2">Tiện Lợi</h3>
            <p class="text-gray-600">Đặt mọi lúc, mọi nơi</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { reservationService } from '@/services/reservationService'
import { useNotificationStore } from '@/stores/notification'

const notification = useNotificationStore()

const form = ref({
  name: '',
  phone: '',
  email: '',
  date: '',
  time: '',
  guests: '',
  tableType: 'normal',
  notes: '',
  acceptPromo: true
})

const submitting = ref(false)
const showSuccess = ref(false)
const reservationCode = ref('')

const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const timeSlots = [
  '10:00', '10:30', '11:00', '11:30', '12:00', '12:30',
  '13:00', '13:30', '14:00', '14:30', '15:00', '15:30',
  '16:00', '16:30', '17:00', '17:30', '18:00', '18:30',
  '19:00', '19:30', '20:00', '20:30', '21:00', '21:30'
]

async function submitReservation() {
  submitting.value = true
  
  try {
    const reservationData = {
      customerName: form.value.name,
      customerPhone: form.value.phone,
      customerEmail: form.value.email,
      reservationDateTime: `${form.value.date}T${form.value.time}:00`,
      numberOfGuests: parseInt(form.value.guests),
      tableType: form.value.tableType,
      notes: form.value.notes
    }

    const response = await reservationService.createPublic(reservationData)
    
    if (response.success) {
      // Sử dụng ID từ backend
      reservationCode.value = `#${response.data.id}`
      showSuccess.value = true
      notification.success('Đặt bàn thành công!')
      
      // Scroll to success message
      setTimeout(() => {
        window.scrollTo({ top: 600, behavior: 'smooth' })
      }, 100)
    }
  } catch (error) {
    console.error('Reservation error:', error)
    const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại!'
    notification.error(errorMessage)
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  form.value = {
    name: '',
    phone: '',
    email: '',
    date: '',
    time: '',
    guests: '',
    tableType: 'normal',
    notes: '',
    acceptPromo: true
  }
  showSuccess.value = false
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 0.8s ease-out forwards;
}
</style>
