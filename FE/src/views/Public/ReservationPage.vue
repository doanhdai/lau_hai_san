<template>
  <div>
    <!-- Hero -->
    <section class="relative h-64 flex items-center justify-center bg-slate-900">
      <div class="absolute inset-0 bg-black/40"></div>
      <div class="relative z-10 text-center text-white px-4 max-w-3xl mx-auto">
        <h1 class="text-3xl md:text-4xl font-bold mb-3 tracking-tight">Đặt Bàn Online</h1>
        <p class="text-base md:text-lg text-slate-200">Đặt bàn nhanh chóng, nhận ưu đãi ngay</p>
      </div>
    </section>

    <!-- Reservation Form -->
    <section class="py-12 bg-white">
      <div class="container mx-auto px-4">
        <div class="max-w-2xl mx-auto">
          <div class="bg-white border border-gray-200 rounded-lg shadow-sm p-6 md:p-10">
            <div class="text-center mb-8">
              <h2 class="text-2xl font-bold text-slate-900 mb-2">Thông Tin Đặt Bàn</h2>
              <p class="text-slate-600 text-sm">Vui lòng điền đầy đủ thông tin</p>
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
                    <option v-for="time in availableTimeSlots" :key="time" :value="time">{{ time }}</option>
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
                <span v-if="!submitting" class="flex items-center justify-center gap-2">
                  <i class="fas fa-check-circle"></i>
                  <span>Xác Nhận Đặt Bàn</span>
                </span>
                <span v-else>Đang xử lý...</span>
              </button>

              <p class="text-center text-xs text-slate-500">
                Bằng việc đặt bàn, bạn đồng ý với 
                <a href="#" class="text-slate-900 hover:underline font-medium">Điều khoản dịch vụ</a>
              </p>
            </form>
          </div>

          <!-- Success Message -->
          <div v-if="showSuccess" class="mt-8 bg-green-50 border border-green-200 rounded-lg p-6 text-center">
            <i class="fas fa-check-circle text-5xl mb-3 text-green-600"></i>
            <h3 class="text-xl font-bold text-slate-900 mb-2">Đặt Bàn Thành Công!</h3>
            <p class="text-slate-700 mb-3 text-sm">
              Chúng tôi đã nhận được yêu cầu đặt bàn của bạn.<br/>
              Mã đặt bàn: <strong class="text-slate-900">{{ reservationCode }}</strong>
            </p>
            <p class="text-slate-600 mb-4 text-sm">
              Vui lòng kiểm tra email/SMS để xác nhận. Chúng tôi sẽ liên hệ trong vòng 15 phút.
            </p>
            <button @click="resetForm" class="bg-slate-900 hover:bg-slate-800 text-white px-6 py-2.5 rounded-lg font-semibold text-sm transition-colors">
              Đặt Bàn Mới
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Why Book Online -->
    <section class="py-16 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-12">
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 mb-3">Tại Sao Đặt Bàn Online?</h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div class="text-center group">
            <div class="w-16 h-16 bg-slate-900 rounded-lg flex items-center justify-center mx-auto mb-4 transition-transform duration-200 group-hover:scale-105">
              <i class="fas fa-bolt text-white text-2xl"></i>
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-2">Nhanh Chóng</h3>
            <p class="text-slate-600 text-sm">Chỉ 2 phút hoàn tất</p>
          </div>
          <div class="text-center group">
            <div class="w-16 h-16 bg-slate-900 rounded-lg flex items-center justify-center mx-auto mb-4 transition-transform duration-200 group-hover:scale-105">
              <i class="fas fa-tag text-white text-2xl"></i>
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-2">Ưu Đãi</h3>
            <p class="text-slate-600 text-sm">Giảm 20% lần đầu</p>
          </div>
          <div class="text-center group">
            <div class="w-16 h-16 bg-slate-900 rounded-lg flex items-center justify-center mx-auto mb-4 transition-transform duration-200 group-hover:scale-105">
              <i class="fas fa-check-circle text-white text-2xl"></i>
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-2">Đảm Bảo</h3>
            <p class="text-slate-600 text-sm">Luôn có chỗ</p>
          </div>
          <div class="text-center group">
            <div class="w-16 h-16 bg-slate-900 rounded-lg flex items-center justify-center mx-auto mb-4 transition-transform duration-200 group-hover:scale-105">
              <i class="fas fa-mobile-alt text-white text-2xl"></i>
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-2">Tiện Lợi</h3>
            <p class="text-slate-600 text-sm">Đặt mọi lúc, mọi nơi</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { reservationService } from '@/services/reservationService'
import { useNotificationStore } from '@/stores/notification'
import { validateEmail, validatePhone } from '@/utils/validation'

const router = useRouter()
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

// Filter time slots based on selected date
const availableTimeSlots = computed(() => {
  if (!form.value.date) {
    return timeSlots
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const selectedDate = new Date(form.value.date)
  selectedDate.setHours(0, 0, 0, 0)
  
  // If selected date is today, filter out past times
  if (selectedDate.getTime() === today.getTime()) {
    const now = new Date()
    const currentHour = now.getHours()
    const currentMinute = now.getMinutes()
    
    return timeSlots.filter(time => {
      const [hour, minute] = time.split(':').map(Number)
      const timeInMinutes = hour * 60 + minute
      const currentTimeInMinutes = currentHour * 60 + currentMinute
      
      // Only show times that are at least 1 hour in the future
      return timeInMinutes > currentTimeInMinutes + 60
    })
  }
  
  // If selected date is in the future, show all time slots
  return timeSlots
})

// Watch for date changes and reset time if it's no longer valid
watch(() => form.value.date, (newDate) => {
  if (newDate && form.value.time) {
    // Check if current selected time is still available
    const isTimeAvailable = availableTimeSlots.value.includes(form.value.time)
    if (!isTimeAvailable) {
      form.value.time = ''
    }
  }
})

async function submitReservation() {
  // Validate required fields
  if (!form.value.name.trim()) {
    notification.error('Vui lòng nhập họ và tên')
    return
  }

  // Validate phone (required)
  const phoneError = validatePhone(form.value.phone, true)
  if (phoneError) {
    notification.error(phoneError)
    return
  }

  // Validate email (optional but validate if provided)
  if (form.value.email && form.value.email.trim()) {
    const emailError = validateEmail(form.value.email, false)
    if (emailError) {
      notification.error(emailError)
      return
    }
  }

  // Validate date and time
  if (!form.value.date || !form.value.time) {
    notification.error('Vui lòng chọn ngày và giờ đặt bàn')
    return
  }

  // Validate number of guests
  if (!form.value.guests || parseInt(form.value.guests) < 1) {
    notification.error('Vui lòng chọn số người hợp lệ')
    return
  }

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
      notification.success('Đặt bàn thành công!')
      
      // Reset form
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
      
      // Navigate to home after a short delay
      setTimeout(() => {
        router.push('/home')
      }, 1000)
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
