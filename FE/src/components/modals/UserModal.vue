<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="px-6 py-4 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-900">
            {{ isEdit ? 'Chỉnh sửa người dùng' : 'Thêm người dùng mới' }}
          </h3>
          <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-6">
        <!-- Basic Info -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Họ và tên <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.fullName"
              type="text"
              required
              class="input-field"
              placeholder="Nhập họ và tên"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Tên đăng nhập <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.username"
              type="text"
              required
              :disabled="isEdit"
              class="input-field"
              :class="{ 'bg-gray-100 cursor-not-allowed': isEdit }"
              placeholder="Nhập tên đăng nhập"
            />
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Email <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.email"
              type="email"
              required
              class="input-field"
              placeholder="Nhập địa chỉ email"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Số điện thoại
            </label>
            <input
              v-model="form.phoneNumber"
              type="tel"
              class="input-field"
              placeholder="Nhập số điện thoại"
            />
          </div>
        </div>

        <!-- Password -->
        <div v-if="!isEdit" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Mật khẩu <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.password"
              type="password"
              required
              class="input-field"
              placeholder="Nhập mật khẩu"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Xác nhận mật khẩu <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.confirmPassword"
              type="password"
              required
              class="input-field"
              placeholder="Nhập lại mật khẩu"
            />
          </div>
        </div>

        <!-- Role Selection -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Vai trò <span class="text-red-500">*</span>
          </label>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <label
              v-for="role in availableRoles"
              :key="role.value"
              class="flex items-center p-3 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50"
              :class="{ 'border-primary-500 bg-primary-50': form.roles.includes(role.value) }"
            >
              <input
                type="checkbox"
                :value="role.value"
                v-model="form.roles"
                class="sr-only"
              />
              <div class="flex items-center">
                <div
                  class="w-4 h-4 border-2 rounded mr-3 flex items-center justify-center"
                  :class="form.roles.includes(role.value) ? 'border-primary-500 bg-primary-500' : 'border-gray-300'"
                >
                  <CheckIcon v-if="form.roles.includes(role.value)" class="w-3 h-3 text-white" />
                </div>
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ role.label }}</div>
                  <div class="text-xs text-gray-500">{{ role.description }}</div>
                </div>
              </div>
            </label>
          </div>
        </div>

        <!-- Address -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Địa chỉ
          </label>
          <textarea
            v-model="form.address"
            rows="3"
            class="input-field"
            placeholder="Nhập địa chỉ"
          ></textarea>
        </div>

        <!-- Status -->
        <div class="flex items-center">
          <input
            id="active"
            v-model="form.active"
            type="checkbox"
            class="w-4 h-4 text-primary-600 bg-gray-100 border-gray-300 rounded focus:ring-primary-500"
          />
          <label for="active" class="ml-2 text-sm font-medium text-gray-700">
            Tài khoản hoạt động
          </label>
        </div>

        <!-- Actions -->
        <div class="flex justify-end gap-3 pt-4 border-t border-gray-200">
          <button
            type="button"
            @click="$emit('close')"
            class="btn-secondary"
          >
            Hủy
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="btn-primary"
            :class="{ 'opacity-50 cursor-not-allowed': loading }"
          >
            <span v-if="loading" class="flex items-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Đang xử lý...
            </span>
            <span v-else>
              {{ isEdit ? 'Cập nhật' : 'Tạo mới' }}
            </span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { userService } from '@/services/userService'
import { useNotificationStore } from '@/stores/notification'
import { XMarkIcon, CheckIcon } from '@heroicons/vue/24/outline'

const props = defineProps({
  user: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

const notification = useNotificationStore()
const loading = ref(false)

const isEdit = computed(() => !!props.user?.id)

const availableRoles = [
  {
    value: 'ROLE_ADMIN',
    label: 'Quản trị viên',
    description: 'Toàn quyền hệ thống'
  },
  {
    value: 'ROLE_MANAGER',
    label: 'Quản lý',
    description: 'Quản lý nhà hàng'
  },
  {
    value: 'ROLE_STAFF',
    label: 'Nhân viên',
    description: 'Phục vụ khách hàng'
  },
  {
    value: 'ROLE_CUSTOMER',
    label: 'Khách hàng',
    description: 'Đặt bàn và order'
  }
]

const form = reactive({
  fullName: '',
  username: '',
  email: '',
  phoneNumber: '',
  password: '',
  confirmPassword: '',
  roles: [],
  address: '',
  active: true
})

// Initialize form when user prop changes
watch(() => props.user, (newUser) => {
  if (newUser) {
    Object.assign(form, {
      fullName: newUser.fullName || '',
      username: newUser.username || '',
      email: newUser.email || '',
      phoneNumber: newUser.phoneNumber || '',
      password: '',
      confirmPassword: '',
      roles: newUser.roles || [],
      address: newUser.address || '',
      active: newUser.active !== false
    })
  } else {
    // Reset form for new user
    Object.assign(form, {
      fullName: '',
      username: '',
      email: '',
      phoneNumber: '',
      password: '',
      confirmPassword: '',
      roles: ['ROLE_CUSTOMER'],
      address: '',
      active: true
    })
  }
}, { immediate: true })

async function handleSubmit() {
  // Validation
  if (!form.fullName.trim()) {
    notification.error('Vui lòng nhập họ và tên')
    return
  }

  if (!form.username.trim()) {
    notification.error('Vui lòng nhập tên đăng nhập')
    return
  }

  if (!form.email.trim()) {
    notification.error('Vui lòng nhập email')
    return
  }

  if (!isEdit.value) {
    if (!form.password.trim()) {
      notification.error('Vui lòng nhập mật khẩu')
      return
    }

    if (form.password !== form.confirmPassword) {
      notification.error('Mật khẩu xác nhận không khớp')
      return
    }

    if (form.password.length < 6) {
      notification.error('Mật khẩu phải có ít nhất 6 ký tự')
      return
    }
  }

  if (form.roles.length === 0) {
    notification.error('Vui lòng chọn ít nhất một vai trò')
    return
  }

  // Email validation
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.email)) {
    notification.error('Email không hợp lệ')
    return
  }

  loading.value = true

  try {
    const userData = {
      fullName: form.fullName.trim(),
      username: form.username.trim(),
      email: form.email.trim(),
      phoneNumber: form.phoneNumber.trim() || null,
      roles: form.roles,
      address: form.address.trim() || null,
      active: form.active
    }

    if (!isEdit.value) {
      userData.password = form.password
    }

    let response
    if (isEdit.value) {
      response = await userService.update(props.user.id, userData)
    } else {
      response = await userService.create(userData)
    }

    if (response.success) {
      notification.success(
        isEdit.value ? 'Cập nhật người dùng thành công' : 'Tạo người dùng thành công'
      )
      emit('saved')
    }
  } catch (error) {
    console.error('Error saving user:', error)
    const message = error.response?.data?.message || error.message
    notification.error(`Lỗi: ${message}`)
  } finally {
    loading.value = false
  }
}
</script>
