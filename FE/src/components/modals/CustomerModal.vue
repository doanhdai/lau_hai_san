<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full mx-4 animate-slide-in">
      <!-- Header -->
      <div class="bg-gradient-to-r from-red-500 to-red-600 text-white px-6 py-4 rounded-t-2xl">
        <h3 class="text-xl font-bold">{{ customer ? 'Chỉnh sửa khách hàng' : 'Thêm khách hàng mới' }}</h3>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Họ tên <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.fullName"
              type="text"
              required
              class="input-field"
              placeholder="Nhập họ tên"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Số điện thoại <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.phone"
              type="tel"
              required
              class="input-field"
              placeholder="0912345678"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Email
            </label>
            <input
              v-model="form.email"
              type="email"
              class="input-field"
              placeholder="email@example.com"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Địa chỉ
            </label>
            <input
              v-model="form.address"
              type="text"
              class="input-field"
              placeholder="Nhập địa chỉ"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Ghi chú
          </label>
          <textarea
            v-model="form.notes"
            rows="3"
            class="input-field"
            placeholder="Ghi chú về khách hàng..."
          ></textarea>
        </div>

        <div class="flex items-center gap-3">
          <input
            v-model="form.isVip"
            type="checkbox"
            id="isVip"
            class="w-4 h-4 text-red-600 focus:ring-red-500 border-gray-300 rounded"
          />
          <label for="isVip" class="text-sm font-medium text-gray-700 flex items-center gap-2">
            <StarIcon class="w-5 h-5 text-yellow-500" />
            Khách hàng VIP
          </label>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <button type="button" @click="$emit('close')" class="btn-secondary">
            Hủy
          </button>
          <button type="submit" class="btn-primary">
            {{ customer ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { StarIcon } from '@heroicons/vue/24/outline'
import { validateEmail, validatePhone } from '@/utils/validation'
import { useNotificationStore } from '@/stores/notification'

const props = defineProps({
  customer: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'save'])

const notification = useNotificationStore()

const form = ref({
  fullName: '',
  phone: '',
  email: '',
  address: '',
  notes: '',
  isVip: false
})

watch(() => props.customer, (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
  } else {
    // Reset form for new customer
    form.value = {
      fullName: '',
      phone: '',
      email: '',
      address: '',
      notes: '',
      isVip: false
    }
  }
}, { immediate: true })

function handleSubmit() {
  // Validate required fields
  if (!form.value.fullName.trim()) {
    notification.error('Vui lòng nhập họ tên')
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

  emit('save', form.value)
}
</script>
