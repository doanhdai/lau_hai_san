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

              <!-- Dish Selection -->
              <div class="border-t border-gray-200 pt-6">
                <div class="flex items-center justify-between mb-4">
                  <label class="block text-sm font-medium text-gray-700">
                    Đặt món trước (Tùy chọn)
                  </label>
                  <span class="text-xs text-gray-500">Có thể đặt sau</span>
                </div>
                
                <!-- Loading dishes -->
                <div v-if="loadingDishes" class="text-center py-8">
                  <i class="fas fa-spinner fa-spin text-2xl text-gray-400 mb-2"></i>
                  <p class="text-sm text-gray-500">Đang tải danh sách món...</p>
                </div>
                
                <!-- Dish list -->
                <div v-else-if="dishes.length > 0" class="space-y-3 max-h-96 overflow-y-auto border border-gray-200 rounded-lg p-4">
                  <div
                    v-for="dish in dishes"
                    :key="dish.id"
                    class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                  >
                    <!-- Dish Image -->
                    <div class="flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden bg-gray-200 border border-gray-300">
                      <img
                        v-if="getDishImage(dish)"
                        :src="getDishImage(dish)"
                        :alt="dish.name"
                        class="w-full h-full object-cover"
                        @error="handleImageError"
                      />
                      <div v-else class="w-full h-full flex items-center justify-center bg-gray-100">
                        <i :class="['fas', getDishIcon(dish.name)]" class="text-2xl text-gray-400"></i>
                      </div>
                    </div>
                    
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2 mb-1">
                        <h4 class="font-semibold text-gray-900 text-sm">{{ dish.name }}</h4>
                        <span v-if="dish.isPromotion" class="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">Khuyến mãi</span>
                      </div>
                      <p class="text-xs text-gray-600 mb-1 line-clamp-2">{{ dish.description }}</p>
                      <p class="text-sm font-bold text-slate-900">{{ formatPrice(dish.price) }}đ</p>
                    </div>
                    
                    <!-- Quantity selector -->
                    <div class="flex items-center gap-2 ml-4 flex-shrink-0">
                      <button
                        @click="decreaseQuantity(dish.id)"
                        type="button"
                        :disabled="getQuantity(dish.id) === 0"
                        class="w-8 h-8 rounded-lg border border-gray-300 bg-white hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center transition-colors"
                      >
                        <i class="fas fa-minus text-xs"></i>
                      </button>
                      <span class="w-10 text-center font-semibold text-gray-900">{{ getQuantity(dish.id) }}</span>
                      <button
                        @click="increaseQuantity(dish.id)"
                        type="button"
                        class="w-8 h-8 rounded-lg border border-gray-300 bg-white hover:bg-gray-100 flex items-center justify-center transition-colors"
                      >
                        <i class="fas fa-plus text-xs"></i>
                      </button>
                    </div>
                  </div>
                </div>
                
                <!-- No dishes available -->
                <div v-else class="text-center py-8 text-gray-500 text-sm">
                  <i class="fas fa-utensils text-2xl mb-2"></i>
                  <p>Hiện chưa có món nào</p>
                </div>
                
                <!-- Selected dishes summary -->
                <div v-if="selectedDishes.length > 0" class="mt-4 bg-slate-50 border border-slate-200 rounded-lg p-4">
                  <p class="text-sm font-semibold text-slate-900 mb-3">Món đã chọn:</p>
                  <div class="space-y-2 mb-3">
                    <div
                      v-for="item in selectedDishes"
                      :key="item.dishId"
                      class="flex items-center gap-3 text-sm"
                    >
                      <!-- Dish Image -->
                      <div class="flex-shrink-0 w-12 h-12 rounded-lg overflow-hidden bg-gray-200 border border-gray-300">
                        <img
                          v-if="getDishImage(dishes.find(d => d.id === item.dishId))"
                          :src="getDishImage(dishes.find(d => d.id === item.dishId))"
                          :alt="getDishName(item.dishId)"
                          class="w-full h-full object-cover"
                          @error="handleImageError"
                        />
                        <div v-else class="w-full h-full flex items-center justify-center bg-gray-100">
                          <i :class="['fas', getDishIcon(getDishName(item.dishId))]" class="text-lg text-gray-400"></i>
                        </div>
                      </div>
                      <span class="flex-1 text-gray-700">
                        {{ getDishName(item.dishId) }} x {{ item.quantity }}
                      </span>
                      <span class="font-semibold text-slate-900">
                        {{ formatPrice(getDishPrice(item.dishId) * item.quantity) }}đ
                      </span>
                    </div>
                  </div>
                  <div class="flex items-center justify-between pt-3 border-t border-slate-200">
                    <span class="font-bold text-slate-900">Tổng tiền:</span>
                    <span class="text-lg font-bold text-slate-900">{{ formatPrice(totalAmount) }}đ</span>
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
                  <h3 class="text-xl font-bold text-slate-900 mb-2">Đặt Bàn Thành Công!</h3>
                  <p class="text-slate-700 mb-3 text-sm">
                    Chúng tôi đã nhận được yêu cầu đặt bàn của bạn.<br/>
                    Mã đặt bàn: <strong class="text-slate-900">{{ reservationCode }}</strong>
                  </p>
                  <p class="text-slate-600 mb-4 text-sm">
                    Vui lòng kiểm tra email/SMS để xác nhận. Chúng tôi sẽ liên hệ trong vòng 15 phút.
                  </p>
                  <div class="flex gap-3 justify-center">
                    <button @click="closeSuccessModal" class="bg-slate-900 hover:bg-slate-800 text-white px-6 py-2.5 rounded-lg font-semibold text-sm transition-colors">
                      Đóng
                    </button>
                    <button @click="resetForm" class="bg-green-600 hover:bg-green-700 text-white px-6 py-2.5 rounded-lg font-semibold text-sm transition-colors">
                      Đặt Bàn Mới
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </Teleport>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { reservationService } from '@/services/reservationService'
import { tableService } from '@/services/tableService'
import { dishService } from '@/services/dishService'
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

// Dish selection
const dishes = ref([])
const selectedDishes = ref([]) // Array of { dishId, quantity, notes }
const loadingDishes = ref(false)

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
  '10:00', '10:15', '10:30', '10:45', '11:00', '11:15', '11:30', '11:45',
  '12:00', '12:15', '12:30', '12:45', '13:00', '13:15', '13:30', '13:45',
  '14:00', '14:15', '14:30', '14:45', '15:00', '15:15', '15:30', '15:45',
  '16:00', '16:15', '16:30', '16:45', '17:00', '17:15', '17:30', '17:45',
  '18:00', '18:15', '18:30', '18:45', '19:00', '19:15', '19:30', '19:45',
  '20:00', '20:15', '20:30', '20:45', '21:00', '21:15', '21:30', '21:45'
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

// Watch for table availability to load dishes
watch(() => isTableAvailable.value, (available) => {
  if (available && dishes.value.length === 0) {
    loadDishes()
  }
})

// Load dishes
async function loadDishes() {
  loadingDishes.value = true
  try {
    const response = await dishService.getAvailable()
    if (response && response.success) {
      dishes.value = response.data || []
    }
  } catch (error) {
    console.error('Error loading dishes:', error)
    notification.error('Không thể tải danh sách món. Vui lòng thử lại!')
  } finally {
    loadingDishes.value = false
  }
}

// Dish selection functions
function increaseQuantity(dishId) {
  const existing = selectedDishes.value.find(item => item.dishId === dishId)
  if (existing) {
    existing.quantity++
  } else {
    selectedDishes.value.push({
      dishId,
      quantity: 1,
      notes: ''
    })
  }
}

function decreaseQuantity(dishId) {
  const existing = selectedDishes.value.find(item => item.dishId === dishId)
  if (existing) {
    existing.quantity--
    if (existing.quantity <= 0) {
      const index = selectedDishes.value.findIndex(item => item.dishId === dishId)
      if (index > -1) {
        selectedDishes.value.splice(index, 1)
      }
    }
  }
}

function getQuantity(dishId) {
  const item = selectedDishes.value.find(item => item.dishId === dishId)
  return item ? item.quantity : 0
}

function getDishName(dishId) {
  const dish = dishes.value.find(d => d.id === dishId)
  return dish ? dish.name : 'Unknown'
}

function getDishPrice(dishId) {
  const dish = dishes.value.find(d => d.id === dishId)
  return dish ? parseFloat(dish.price) : 0
}

function getDishImage(dish) {
  if (dish && dish.imageUrl && dish.imageUrl.trim() !== '') {
    return dish.imageUrl
  }
  return null
}

function getDishIcon(dishName) {
  if (!dishName) return 'fa-utensils'
  const name = dishName.toLowerCase()
  if (name.includes('lẩu thai') || name.includes('thái')) return 'fa-bowl-food'
  if (name.includes('tôm')) return 'fa-shrimp'
  if (name.includes('bò')) return 'fa-drumstick-bite'
  if (name.includes('hải sản')) return 'fa-fish'
  if (name.includes('nấm')) return 'fa-seedling'
  if (name.includes('rau')) return 'fa-leaf'
  if (name.includes('mì') || name.includes('bún')) return 'fa-bowl-rice'
  if (name.includes('nước') || name.includes('trà') || name.includes('bia')) return 'fa-glass-water'
  if (name.includes('cá')) return 'fa-fish'
  if (name.includes('cua')) return 'fa-crab'
  if (name.includes('gà')) return 'fa-drumstick-bite'
  if (name.includes('heo') || name.includes('sườn')) return 'fa-drumstick-bite'
  return 'fa-utensils'
}

function handleImageError(event) {
  // Replace broken image with icon placeholder
  const img = event.target
  const parent = img.parentElement
  if (parent) {
    img.style.display = 'none'
    const iconPlaceholder = document.createElement('div')
    iconPlaceholder.className = 'w-full h-full flex items-center justify-center bg-gray-100'
    const dishName = img.alt || 'Dish'
    const iconClass = getDishIcon(dishName)
    iconPlaceholder.innerHTML = `<i class="fas ${iconClass} text-2xl text-gray-400"></i>`
    parent.appendChild(iconPlaceholder)
  }
}

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN').format(price)
}

// Tính subtotal (tổng giá món chưa thuế)
const subtotal = computed(() => {
  return selectedDishes.value.reduce((total, item) => {
    return total + (getDishPrice(item.dishId) * item.quantity)
  }, 0)
})

// Tính tax (10%)
const tax = computed(() => {
  return subtotal.value * 0.1
})

// Tính total (subtotal + tax)
const totalAmount = computed(() => {
  return subtotal.value + tax.value
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
    // Prepare items for order
    const items = selectedDishes.value
      .filter(item => item.quantity > 0)
      .map(item => ({
        dishId: item.dishId,
        quantity: item.quantity,
        notes: item.notes || ''
      }))
    
    // Lấy userId nếu đã đăng nhập
    const userId = authStore.user?.id || null

    const reservationData = {
      customerName: form.value.name.trim(),
      customerPhone: form.value.phone.trim(),
      customerEmail: form.value.email ? form.value.email.trim() : '',
      reservationDateTime: `${form.value.date}T${form.value.time}:00`,
      numberOfGuests: parseInt(form.value.guests),
      notes: form.value.notes ? form.value.notes.trim() : '',
      items: items.length > 0 ? items : undefined, // Only include if there are items
      userId: userId // Gửi userId nếu đã đăng nhập
    }

    // Nếu có món đã chọn, không tạo reservation ngay, mà redirect đến trang thanh toán
    if (items.length > 0) {
      // Tính tổng đơn món (subtotal + tax)
      const totalOrderAmount = totalAmount.value
      const depositAmount = Math.round(totalOrderAmount * 0.2) // 20%, làm tròn
      
      // Lưu thông tin reservation vào query params để trang thanh toán có thể tạo reservation
      const queryParams = {
        customerName: form.value.name.trim(),
        customerPhone: form.value.phone.trim(),
        customerEmail: form.value.email ? form.value.email.trim() : '',
        reservationDateTime: `${form.value.date}T${form.value.time}:00`,
        numberOfGuests: parseInt(form.value.guests),
        notes: form.value.notes ? form.value.notes.trim() : '',
        depositAmount: depositAmount,
        totalAmount: totalOrderAmount,
        items: JSON.stringify(items)
      }
      
      // Thêm userId nếu đã đăng nhập
      if (userId) {
        queryParams.userId = userId
      }
      
      router.push({
        path: '/reservation/deposit-payment',
        query: queryParams
      })
      return
    }
    
    // Nếu không có món, tạo reservation ngay như bình thường
    console.log('Sending reservation data:', reservationData)
    const response = await reservationService.createPublic(reservationData)
    console.log('Reservation response:', response)
    
    if (response.success) {
      
      // Nếu không có món, hiển thị thông báo thành công như bình thường
      notification.success('Đặt bàn thành công!')
      
      // Lấy mã đặt bàn từ response
      const reservationId = response.data?.id
      reservationCode.value = reservationId || response.data?.reservationCode || 'N/A'
      
      // Reset form nhưng giữ lại thông tin đã chọn để hiển thị
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
      
      // Reset availability check
      isTableAvailable.value = false
      availabilityChecked.value = false
      
      // Reset selected dishes
      selectedDishes.value = []
      
      // Hiển thị thông báo thành công
      showSuccess.value = true
      
      // Scroll to top để hiển thị thông báo thành công
      window.scrollTo({ top: 0, behavior: 'smooth' })
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

function closeSuccessModal() {
  showSuccess.value = false
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
  selectedDishes.value = []
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Load dishes on mount
onMounted(() => {
  // Dishes will be loaded when table is available
})
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
