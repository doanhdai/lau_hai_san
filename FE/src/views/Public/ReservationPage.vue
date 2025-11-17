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
              <p class="text-slate-600 text-sm mb-3">Vui lòng điền đầy đủ thông tin</p>
              <div class="bg-green-50 border border-green-200 rounded-lg p-3 inline-block">
                <p class="text-xs text-green-800">
                  <i class="fas fa-info-circle mr-1"></i>
                  <strong>Không cần đăng nhập</strong> - Bạn có thể đặt bàn ngay mà không cần tài khoản
                </p>
              </div>
            </div>

            <!-- Step 1: Check Availability -->
            <div v-if="!isTableAvailable" class="space-y-6">
              <div class="bg-blue-50 border-2 border-blue-200 rounded-lg p-4 mb-6">
                <p class="text-sm text-blue-800">
                  <i class="fas fa-info-circle mr-2"></i>
                  Vui lòng chọn số người, ngày và giờ để kiểm tra bàn còn trống
                </p>
              </div>

              <!-- Reservation Details for Check -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Số người <span class="text-red-600">*</span>
                  </label>
                  <input
                    v-model="form.guests"
                    @input="clearError('guests')"
                    type="number"
                    min="1"
                    max="50"
                    required
                    class="input-field"
                    :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.guests }"
                    placeholder="Nhập số người"
                  />
                  <p v-if="errors.guests" class="mt-1 text-xs text-red-600">{{ errors.guests }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Ngày <span class="text-red-600">*</span>
                  </label>
                  <input
                    v-model="form.date"
                    @change="clearError('date')"
                    type="date"
                    required
                    :min="minDate"
                    class="input-field"
                    :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.date }"
                  />
                  <p v-if="errors.date" class="mt-1 text-xs text-red-600">{{ errors.date }}</p>
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Giờ <span class="text-red-600">*</span>
                </label>
                <select 
                  v-model="form.time"
                  @change="clearError('time')"
                  required 
                  class="input-field"
                  :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.time }"
                >
                  <option value="">Chọn giờ</option>
                  <option v-for="time in availableTimeSlots" :key="time" :value="time">{{ time }}</option>
                </select>
                <p v-if="errors.time" class="mt-1 text-xs text-red-600">{{ errors.time }}</p>
              </div>

              <!-- Check Availability Button -->
              <button
                @click="checkAvailability"
                :disabled="checkingAvailability || !form.guests || !form.date || !form.time"
                class="w-full bg-slate-900 hover:bg-slate-800 text-white px-5 py-4 text-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed rounded-lg shadow-sm transition-colors"
              >
                <span v-if="!checkingAvailability" class="flex items-center justify-center gap-2">
                  <i class="fas fa-search"></i>
                  <span>Kiểm tra bàn</span>
                </span>
                <span v-else class="flex items-center justify-center gap-2">
                  <i class="fas fa-spinner fa-spin"></i>
                  <span>Đang kiểm tra...</span>
                </span>
              </button>

              <!-- Availability Message -->
              <div v-if="availabilityChecked && !isTableAvailable" class="bg-red-50 border-2 border-red-200 rounded-lg p-4">
                <div class="flex items-center gap-2">
                  <i class="fas fa-times-circle text-red-600"></i>
                  <p class="text-sm font-medium text-red-800">
                    Rất tiếc, không còn bàn trống cho thời gian này. Vui lòng chọn thời gian khác.
                  </p>
                </div>
              </div>
            </div>

            <!-- Step 2: Full Form (only show if table is available) -->
            <form v-else @submit.prevent="submitReservation" novalidate class="space-y-6">
              <!-- Success Message for Availability -->
              <div class="bg-green-50 border-2 border-green-200 rounded-lg p-4 mb-6">
                <div class="flex items-center gap-2">
                  <i class="fas fa-check-circle text-green-600"></i>
                  <p class="text-sm font-medium text-green-800">
                    Còn bàn trống! Vui lòng điền thông tin để hoàn tất đặt bàn.
                  </p>
                </div>
                <button
                  @click="resetAvailabilityCheck"
                  type="button"
                  class="mt-2 text-xs text-green-700 hover:text-green-900 underline"
                >
                  Chọn thời gian khác
                </button>
              </div>

              <!-- Personal Info -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Họ và tên <span class="text-red-600">*</span>
                  </label>
                  <input
                    v-model="form.name"
                    @input="clearError('name')"
                    type="text"
                    required
                    class="input-field"
                    :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.name }"
                    placeholder="Nguyễn Văn A"
                  />
                  <p v-if="errors.name" class="mt-1 text-xs text-red-600">{{ errors.name }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    Số điện thoại <span class="text-red-600">*</span>
                  </label>
                  <input
                    v-model="form.phone"
                    @input="clearError('phone')"
                    type="tel"
                    required
                    class="input-field"
                    :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.phone }"
                    placeholder="0123456789"
                  />
                  <p v-if="errors.phone" class="mt-1 text-xs text-red-600">{{ errors.phone }}</p>
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
                <input
                  v-model="form.email"
                  @input="clearError('email')"
                  type="email"
                  class="input-field"
                  :class="{ 'border-red-500 focus:ring-red-500 focus:border-red-500': errors.email }"
                  placeholder="email@example.com"
                />
                <p v-if="errors.email" class="mt-1 text-xs text-red-600">{{ errors.email }}</p>
              </div>

              <!-- Reservation Details (Read-only) -->
              <div class="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-600 mb-3 font-semibold">Thông tin đặt bàn đã chọn:</p>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">Số người</p>
                    <p class="text-sm font-semibold text-slate-900">{{ form.guests }} người</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">Ngày</p>
                    <p class="text-sm font-semibold text-slate-900">{{ formatDate(form.date) }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">Giờ</p>
                    <p class="text-sm font-semibold text-slate-900">{{ form.time }}</p>
                  </div>
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
                class="w-full btn-primary py-4 text-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed bg-slate-900 hover:bg-slate-800 text-white px-5 rounded-lg shadow-sm"
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
import { tableService } from '@/services/tableService'
import { useNotificationStore } from '@/stores/notification'
import { useAuthStore } from '@/stores/auth'
import { validateEmail, validatePhone } from '@/utils/validation'

const router = useRouter()
const notification = useNotificationStore()
const authStore = useAuthStore()

const form = ref({
  name: '',
  phone: '',
  email: '',
  date: '',
  time: '',
  guests: '',
  notes: '',
  acceptPromo: true
})

const submitting = ref(false)
const showSuccess = ref(false)
const reservationCode = ref('')
const isTableAvailable = ref(false)
const checkingAvailability = ref(false)
const availabilityChecked = ref(false)

// Validation errors
const errors = ref({
  name: '',
  phone: '',
  email: '',
  guests: '',
  date: '',
  time: ''
})

// Clear errors when user types
function clearError(field) {
  if (errors.value[field]) {
    errors.value[field] = ''
  }
}

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
  // Reset availability check when date changes
  if (isTableAvailable.value) {
    resetAvailabilityCheck()
  }
})

// Watch for time or guests changes and reset availability check
watch([() => form.value.time, () => form.value.guests], () => {
  if (isTableAvailable.value) {
    resetAvailabilityCheck()
  }
})

async function checkAvailability() {
  // Clear previous errors
  errors.value = {
    name: '',
    phone: '',
    email: '',
    guests: '',
    date: '',
    time: ''
  }

  // Validate required fields
  let hasError = false

  if (!form.value.guests || form.value.guests === '') {
    errors.value.guests = 'Vui lòng nhập số người'
    hasError = true
  } else {
    const guestsNum = parseInt(form.value.guests)
    if (isNaN(guestsNum) || guestsNum < 1) {
      errors.value.guests = 'Số người phải lớn hơn 0'
      hasError = true
    } else if (guestsNum > 50) {
      errors.value.guests = 'Số người không được vượt quá 50'
      hasError = true
    }
  }

  if (!form.value.date) {
    errors.value.date = 'Vui lòng chọn ngày'
    hasError = true
  }

  if (!form.value.time) {
    errors.value.time = 'Vui lòng chọn giờ'
    hasError = true
  }

  if (hasError) {
    return
  }

  checkingAvailability.value = true
  availabilityChecked.value = false
  isTableAvailable.value = false

  try {
    // Format reservation time
    const reservationTime = `${form.value.date}T${form.value.time}:00`
    const numberOfGuests = parseInt(form.value.guests)

    const response = await tableService.checkAvailability(reservationTime, numberOfGuests)
    
    availabilityChecked.value = true

    if (response.success && response.data === true) {
      isTableAvailable.value = true
      notification.success('Còn bàn trống! Vui lòng điền thông tin để hoàn tất đặt bàn.')
    } else {
      isTableAvailable.value = false
      notification.error('Rất tiếc, không còn bàn trống cho thời gian này. Vui lòng chọn thời gian khác.')
    }
  } catch (error) {
    console.error('Check availability error:', error)
    availabilityChecked.value = true
    isTableAvailable.value = false
    const errorMessage = error.response?.data?.message || error.message || 'Không thể kiểm tra bàn. Vui lòng thử lại!'
    notification.error(errorMessage)
  } finally {
    checkingAvailability.value = false
  }
}

function resetAvailabilityCheck() {
  isTableAvailable.value = false
  availabilityChecked.value = false
}

function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

async function submitReservation(e) {
  // Prevent default form submission
  if (e) {
    e.preventDefault()
  }
  
  console.log('submitReservation called', form.value)
  
  // Clear previous errors
  errors.value = {
    name: '',
    phone: '',
    email: '',
    guests: '',
    date: '',
    time: ''
  }

  let hasError = false

  // Validate name
  if (!form.value.name || !form.value.name.trim()) {
    errors.value.name = 'Vui lòng nhập họ và tên'
    hasError = true
  }

  // Validate phone (required)
  const phoneError = validatePhone(form.value.phone, true)
  if (phoneError) {
    errors.value.phone = phoneError
    hasError = true
  }

  // Validate email (optional but validate if provided)
  if (form.value.email && form.value.email.trim()) {
    const emailError = validateEmail(form.value.email, false)
    if (emailError) {
      errors.value.email = emailError
      hasError = true
    }
  }

  // Validate date and time
  if (!form.value.date) {
    errors.value.date = 'Vui lòng chọn ngày đặt bàn'
    hasError = true
  }

  if (!form.value.time) {
    errors.value.time = 'Vui lòng chọn giờ đặt bàn'
    hasError = true
  }

  // Validate number of guests
  if (!form.value.guests || form.value.guests === '') {
    errors.value.guests = 'Vui lòng nhập số người'
    hasError = true
  } else {
    const guestsNum = parseInt(form.value.guests)
    if (isNaN(guestsNum) || guestsNum < 1) {
      errors.value.guests = 'Số người phải lớn hơn 0'
      hasError = true
    } else if (guestsNum > 50) {
      errors.value.guests = 'Số người không được vượt quá 50'
      hasError = true
    }
  }

  if (hasError) {
    // Scroll to first error
    const firstErrorField = document.querySelector('.border-red-500')
    if (firstErrorField) {
      firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
    return
  }

  console.log('Validation passed, submitting...')
  submitting.value = true
  
  try {
    const reservationData = {
      customerName: form.value.name.trim(),
      customerPhone: form.value.phone.trim(),
      customerEmail: form.value.email ? form.value.email.trim() : '',
      reservationDateTime: `${form.value.date}T${form.value.time}:00`,
      numberOfGuests: parseInt(form.value.guests),
      notes: form.value.notes ? form.value.notes.trim() : ''
    }

    console.log('Sending reservation data:', reservationData)
    const response = await reservationService.createPublic(reservationData)
    console.log('Reservation response:', response)
    
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
        notes: '',
        acceptPromo: true
      }
      errors.value = {
        name: '',
        phone: '',
        email: '',
        guests: '',
        date: '',
        time: ''
      }
      showSuccess.value = false
      
      // Navigate based on authentication status
      setTimeout(() => {
        if (authStore.isAuthenticated) {
          // Nếu đã login, chuyển về trang lịch sử đặt bàn
          router.push('/my-reservations')
        } else {
          // Nếu chưa login, chuyển về trang home
          router.push('/home')
        }
      }, 1000)
    } else {
      notification.error(response.message || 'Có lỗi xảy ra, vui lòng thử lại!')
    }
  } catch (error) {
    console.error('Reservation error:', error)
    console.error('Error details:', {
      message: error.message,
      response: error.response,
      data: error.response?.data
    })
    const errorMessage = error.response?.data?.message || error.message || 'Có lỗi xảy ra, vui lòng thử lại!'
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
    notes: '',
    acceptPromo: true
  }
  errors.value = {
    name: '',
    phone: '',
    email: '',
    guests: '',
    date: '',
    time: ''
  }
  showSuccess.value = false
  resetAvailabilityCheck()
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
