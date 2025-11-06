<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-sky-500 via-sky-600 to-blue-700 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full">
      <div class="text-center mb-8">
        <h2 class="text-4xl font-extrabold text-white">Đăng ký tài khoản</h2>
      </div>

      <div class="bg-white rounded-2xl shadow-2xl p-8">
        <form @submit.prevent="handleRegister" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Tên đăng nhập</label>
            <input v-model="form.username" type="text" required class="input-field" />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
            <input v-model="form.email" type="email" required class="input-field" />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Họ tên</label>
            <input v-model="form.fullName" type="text" required class="input-field" />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại</label>
            <input v-model="form.phone" type="tel" class="input-field" />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Mật khẩu</label>
            <input v-model="form.password" type="password" required class="input-field" />
          </div>

          <button type="submit" :disabled="authStore.loading" class="w-full btn-primary">
            Đăng ký
          </button>
        </form>

        <div class="text-center mt-4">
          <router-link to="/login" class="text-sm text-sky-600 hover:text-sky-500">
            Đã có tài khoản? Đăng nhập
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const form = ref({
  username: '',
  email: '',
  fullName: '',
  phone: '',
  password: ''
})

async function handleRegister() {
  await authStore.register(form.value)
}
</script>
