<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto animate-slide-in">
      <!-- Header -->
      <div class="bg-gradient-to-r from-red-500 to-red-600 text-white px-6 py-4 rounded-t-2xl sticky top-0 z-10">
        <h3 class="text-xl font-bold">{{ dish ? 'Chỉnh sửa món ăn' : 'Thêm món ăn mới' }}</h3>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Tên món ăn <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.name"
              type="text"
              required
              class="input-field"
              placeholder="Nhập tên món ăn"
            />
          </div>

          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Mô tả
            </label>
            <textarea
              v-model="form.description"
              rows="3"
              class="input-field"
              placeholder="Mô tả về món ăn..."
            ></textarea>
          </div>

          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Hình ảnh
            </label>
            
            <!-- Image Preview -->
            <div v-if="form.imageUrl" class="mb-3">
              <img 
                :src="form.imageUrl" 
                alt="Preview" 
                class="w-32 h-32 object-cover rounded-lg border-2 border-gray-200"
              />
            </div>
            
            <!-- Upload Input -->
            <div class="flex items-center gap-3">
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                @change="handleFileSelect"
                class="hidden"
                id="imageUpload"
              />
              <label
                for="imageUpload"
                class="btn-secondary cursor-pointer flex items-center gap-2"
              >
                <span>📷 {{ form.imageUrl ? 'Đổi ảnh' : 'Chọn ảnh' }}</span>
              </label>
              
              <!-- Manual URL Input -->
              <div class="flex-1">
                <input
                  v-model="form.imageUrl"
                  type="url"
                  class="input-field"
                  placeholder="Hoặc nhập URL ảnh..."
                />
              </div>
              
              <!-- Remove Image Button -->
              <button
                v-if="form.imageUrl"
                type="button"
                @click="removeImage"
                class="btn-secondary text-red-600 hover:bg-red-50"
              >
                ✕
              </button>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Danh mục <span class="text-red-500">*</span>
            </label>
            <select v-model="form.categoryId" required class="input-field">
              <option value="">-- Chọn danh mục --</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Giá <span class="text-red-500">*</span>
            </label>
            <input
              v-model.number="form.price"
              type="number"
              required
              min="0"
              step="1000"
              class="input-field"
              placeholder="0"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Trạng thái
            </label>
            <select v-model="form.status" class="input-field">
              <option value="AVAILABLE">Còn món</option>
              <option value="UNAVAILABLE">Hết món</option>
            </select>
          </div>

          <div class="hidden">
            <!-- Keep old imageUrl input hidden for backward compatibility -->
          </div>
        </div>

        <div class="flex items-center gap-3 pt-2">
          <input
            v-model="form.isPromotion"
            type="checkbox"
            id="isPromotion"
            class="w-4 h-4 text-red-600 focus:ring-red-500 border-gray-300 rounded"
          />
          <label for="isPromotion" class="text-sm font-medium text-gray-700 flex items-center gap-2">
            <GiftIcon class="w-5 h-5 text-yellow-500" />
            Món khuyến mãi
          </label>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <button type="button" @click="$emit('close')" class="btn-secondary">
            Hủy
          </button>
          <button type="submit" class="btn-primary">
            {{ dish ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { GiftIcon } from '@heroicons/vue/24/outline'
import { useNotificationStore } from '@/stores/notification'

const notification = useNotificationStore()

const props = defineProps({
  dish: {
    type: Object,
    default: null
  },
  categories: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'save'])

const fileInput = ref(null)
const selectedFile = ref(null)
const originalImageUrl = ref('')

const form = ref({
  name: '',
  description: '',
  categoryId: '',
  price: 0,
  status: 'AVAILABLE',
  imageUrl: '',
  isPromotion: false
})

watch(() => props.dish, (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
    originalImageUrl.value = newVal.imageUrl || ''
    selectedFile.value = null
  } else {
    // Reset form when creating new dish
    form.value = {
      name: '',
      description: '',
      categoryId: '',
      price: 0,
      status: 'AVAILABLE',
      imageUrl: '',
      isPromotion: false
    }
    originalImageUrl.value = ''
    selectedFile.value = null
  }
}, { immediate: true })

function handleFileSelect(event) {
  const file = event.target.files[0]
  if (!file) return

  // Validate file type
  if (!file.type.startsWith('image/')) {
    notification.error('Vui lòng chọn file ảnh')
    return
  }

  // Validate file size (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    notification.error('Kích thước ảnh không được vượt quá 5MB')
    return
  }

  // Store selected file for later upload
  selectedFile.value = file
  
  // Create preview URL
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.imageUrl = e.target.result
  }
  reader.readAsDataURL(file)
}

function removeImage() {
  form.value.imageUrl = originalImageUrl.value || ''
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function handleSubmit() {
  // If there's a new file selected, prepare FormData
  if (selectedFile.value) {
    const formData = new FormData()
    formData.append('name', form.value.name)
    formData.append('description', form.value.description || '')
    formData.append('price', form.value.price.toString())
    formData.append('categoryId', form.value.categoryId.toString())
    formData.append('status', form.value.status)
    formData.append('isPromotion', form.value.isPromotion.toString())
    formData.append('image', selectedFile.value)
    
    emit('save', { isFormData: true, data: formData })
  } else {
    // No new file, send JSON as usual
    emit('save', { isFormData: false, data: form.value })
  }
}
</script>
