<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-900 py-12 px-4 sm:px-6 lg:px-8 relative">
    <!-- Background Pattern -->
    <div class="absolute inset-0 bg-black/40"></div>

    <div class="max-w-md w-full relative z-10">
      <!-- Logo & Title -->
      <div class="text-center mb-8">
        <div class="flex justify-center mb-4">
          <div
            class="w-16 h-16 bg-white rounded-lg flex items-center justify-center shadow-lg"
          >
            <i class="fas fa-bowl-food text-slate-900 text-3xl"></i>
          </div>
        </div>
        <h2 class="text-3xl md:text-4xl font-bold text-white mb-2">
          Đăng ký tài khoản
        </h2>
        <p class="text-base text-slate-200">Tạo tài khoản mới để bắt đầu</p>
      </div>

      <div class="bg-white rounded-lg shadow-xl p-8 border border-gray-200">
        <form @submit.prevent="handleRegister" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-2">
              Tên đăng nhập <span class="text-red-600">*</span>
            </label>
            <input 
              v-model="form.username" 
              type="text" 
              required 
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
              placeholder="Nhập tên đăng nhập"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-2">
              Email <span class="text-red-600">*</span>
            </label>
            <input 
              v-model="form.email" 
              type="email" 
              required 
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
              placeholder="Nhập email"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-2">
              Họ tên <span class="text-red-600">*</span>
            </label>
            <input 
              v-model="form.fullName" 
              type="text" 
              required 
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
              placeholder="Nhập họ tên"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-2">
              Số điện thoại
            </label>
            <input 
              v-model="form.phone" 
              type="tel" 
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
              placeholder="Nhập số điện thoại"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-2">
              Mật khẩu <span class="text-red-600">*</span>
            </label>
            <input 
              v-model="form.password" 
              type="password" 
              required 
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
              placeholder="Nhập mật khẩu (tối thiểu 6 ký tự)"
            />
          </div>

          <!-- Error Message -->
          <div
            v-if="authStore.error"
            class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm"
          >
            {{ authStore.error }}
          </div>

          <button 
            type="submit" 
            :disabled="authStore.loading" 
            class="w-full bg-slate-900 hover:bg-slate-800 text-white py-2.5 rounded-lg font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <span v-if="!authStore.loading">Đăng ký</span>
            <span v-else class="flex items-center justify-center gap-2">
              <i class="fas fa-spinner fa-spin"></i>
              <span>Đang xử lý...</span>
            </span>
          </button>
        </form>

        <div class="text-center mt-6">
          <p class="text-sm text-slate-600">
            Đã có tài khoản?
            <router-link to="/login" class="font-medium text-slate-900 hover:text-slate-700 transition">
              Đăng nhập
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { validateEmail, validatePhone } from '@/utils/validation'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const authStore = useAuthStore()
const notification = useNotificationStore()

const form = ref({
  username: '',
  email: '',
  fullName: '',
  phone: '',
  password: ''
})

async function handleRegister() {
  // Validate required fields
  if (!form.value.username.trim()) {
    notification.error('Vui lòng nhập tên đăng nhập')
    return
  }

  if (!form.value.fullName.trim()) {
    notification.error('Vui lòng nhập họ tên')
    return
  }

  // Validate email (required)
  const emailError = validateEmail(form.value.email, true)
  if (emailError) {
    notification.error(emailError)
    return
  }

  // Validate phone (optional but validate if provided)
  if (form.value.phone && form.value.phone.trim()) {
    const phoneError = validatePhone(form.value.phone, false)
    if (phoneError) {
      notification.error(phoneError)
      return
    }
  }

  // Validate password
  if (!form.value.password || form.value.password.length < 6) {
    notification.error('Mật khẩu phải có ít nhất 6 ký tự')
    return
  }

  try {
    const result = await authStore.register(form.value)
    if (result && result.success) {
      notification.success('Đăng ký tài khoản thành công! Vui lòng đăng nhập.')
      setTimeout(() => {
        router.push('/login')
      }, 1000)
    } else {
      notification.error(authStore.error || 'Đăng ký thất bại. Vui lòng thử lại.')
    }
  } catch (error) {
    notification.error('Đã xảy ra lỗi. Vui lòng thử lại.')
  }
}
</script>
