<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-lg w-full mx-4 animate-slide-in">
      <!-- Header -->
      <div class="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-4 rounded-t-2xl">
        <h3 class="text-xl font-bold">{{ category ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới' }}</h3>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Tên danh mục <span class="text-red-500">*</span>
          </label>
          <input
            v-model="form.categoryName"
            type="text"
            required
            class="input-field"
            placeholder="Lẩu, Nướng, Hải sản..."
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Mô tả
          </label>
          <textarea
            v-model="form.description"
            rows="3"
            class="input-field"
            placeholder="Mô tả về danh mục..."
          ></textarea>
        </div>

        <div class="flex items-center gap-2">
          <input
            v-model="form.active"
            type="checkbox"
            id="active"
            class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
          />
          <label for="active" class="text-sm font-medium text-gray-700">
            Kích hoạt
          </label>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <button type="button" @click="$emit('close')" class="btn-secondary">
            Hủy
          </button>
          <button type="submit" class="btn-primary">
            {{ category ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  category: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'save'])

const form = ref({
  categoryName: '',
  description: '',
  active: true
})

watch(() => props.category, (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
  }
}, { immediate: true })

function handleSubmit() {
  emit('save', form.value)
}
</script>
