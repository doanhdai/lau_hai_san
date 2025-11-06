<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Món ăn</h1>
        <p class="text-gray-600 mt-1">Thực đơn và món ăn nhà hàng</p>
      </div>
      <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
        <span class="text-lg">➕</span>
        Thêm món ăn
      </button>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên món, mô tả..."
            class="input-field"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Danh mục</label>
          <select v-model="filterCategory" class="input-field">
            <option value="">Tất cả</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="input-field">
            <option value="">Tất cả</option>
            <option value="AVAILABLE">Còn món</option>
            <option value="UNAVAILABLE">Hết món</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="loadDishes" class="btn-secondary w-full flex items-center justify-center gap-2">
            <span class="text-lg">🔍</span>
            Tìm kiếm
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="loading-spinner"></div>
    </div>

    <!-- Dishes Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div 
        v-for="dish in filteredDishes" 
        :key="dish.id"
        class="card group hover:shadow-2xl transition-all duration-300 cursor-pointer"
      >
        <!-- Image -->
        <div class="relative h-48 bg-gradient-to-br from-red-100 to-orange-100 rounded-lg mb-4 overflow-hidden">
          <div v-if="dish.imageUrl" class="w-full h-full">
            <img :src="dish.imageUrl" :alt="dish.name" class="w-full h-full object-cover" />
          </div>
          <div v-else class="w-full h-full flex items-center justify-center">
            <span class="text-8xl text-red-300">🍰</span>
          </div>
          <!-- Status badge -->
          <div class="absolute top-2 right-2">
            <span v-if="dish.status === 'AVAILABLE'" class="badge-success">
              Còn món
            </span>
            <span v-else class="badge-danger">
              Hết món
            </span>
          </div>
          <!-- Promotion badge -->
          <div v-if="dish.isPromotion" class="absolute top-2 left-2">
            <span class="badge bg-yellow-500 text-white">
              <span class="text-sm inline mr-1">🎁</span>
              Khuyến mãi
            </span>
          </div>
        </div>

        <!-- Info -->
        <div class="space-y-3">
          <div>
            <h3 class="font-bold text-lg text-gray-900 group-hover:text-red-600 transition">
              {{ dish.name }}
            </h3>
            <p class="text-sm text-gray-600 line-clamp-2">{{ dish.description || 'Không có mô tả' }}</p>
          </div>

          <div class="flex items-center justify-between">
            <span class="badge bg-blue-100 text-blue-800">{{ dish.categoryName }}</span>
            <span class="text-xl font-bold text-red-600">{{ formatCurrency(dish.price) }}</span>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-2 pt-3 border-t">
            <button @click.stop="editDish(dish)" class="flex-1 btn-secondary text-sm py-2">
              <span class="text-sm inline mr-1">✏️</span>
              Sửa
            </button>
            <button 
              @click.stop="toggleDishStatus(dish)" 
              :class="dish.status === 'AVAILABLE' ? 'bg-yellow-500 hover:bg-yellow-600' : 'bg-green-500 hover:bg-green-600'"
              class="flex-1 text-white font-semibold py-2 px-3 rounded-lg text-sm transition"
            >
              {{ dish.status === 'AVAILABLE' ? 'Hết món' : 'Còn món' }}
            </button>
            <button @click.stop="deleteDish(dish)" class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition">
              <span class="text-lg">🗑️</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredDishes.length === 0" class="card text-center py-12">
      <span class="text-8xl text-gray-300 block mb-4">🍰</span>
      <p class="text-gray-500 text-lg">Không tìm thấy món ăn nào</p>
    </div>

    <!-- Create/Edit Modal -->
    <DishModal
      v-if="showCreateModal || selectedDish"
      :dish="selectedDish"
      :categories="categories"
      @close="closeModal"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dishService } from '@/services/dishService'
import { categoryService } from '@/services/categoryService'
import { useNotificationStore } from '@/stores/notification'
import DishModal from '@/components/modals/DishModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const dishes = ref([])
const categories = ref([])
const searchQuery = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const showCreateModal = ref(false)
const selectedDish = ref(null)

const filteredDishes = computed(() => {
  let result = dishes.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(d => 
      d.name.toLowerCase().includes(query) ||
      (d.description && d.description.toLowerCase().includes(query))
    )
  }

  if (filterCategory.value) {
    result = result.filter(d => d.categoryId === filterCategory.value)
  }

  if (filterStatus.value) {
    result = result.filter(d => d.status === filterStatus.value)
  }

  return result
})

onMounted(() => {
  loadDishes()
  loadCategories()
})

async function loadDishes() {
  loading.value = true
  try {
    const response = await dishService.getAll()
    if (response.success) {
      dishes.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách món ăn')
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const response = await categoryService.getActive()
    if (response.success) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('Không thể tải danh mục')
  }
}

function editDish(dish) {
  selectedDish.value = { ...dish }
}

async function toggleDishStatus(dish) {
  const newStatus = dish.status === 'AVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE'
  try {
    await dishService.updateStatus(dish.id, newStatus)
    notification.success('Cập nhật trạng thái thành công')
    loadDishes()
  } catch (error) {
    notification.error('Không thể cập nhật trạng thái')
  }
}

async function deleteDish(dish) {
  if (!confirm(`Xóa món "${dish.name}"?`)) return

  try {
    await dishService.delete(dish.id)
    notification.success('Đã xóa món ăn')
    loadDishes()
  } catch (error) {
    notification.error('Không thể xóa món ăn')
  }
}

function closeModal() {
  showCreateModal.value = false
  selectedDish.value = null
}

async function handleSave(payload) {
  try {
    if (selectedDish.value) {
      // Update dish
      if (payload.isFormData) {
        // Send FormData if there's a new image file
        await dishService.updateWithImage(selectedDish.value.id, payload.data)
      } else {
        // Send JSON if no new image file
        await dishService.update(selectedDish.value.id, payload.data)
      }
      notification.success('Cập nhật món ăn thành công')
    } else {
      // Create new dish - check if FormData or JSON
      if (payload.isFormData) {
        await dishService.createWithImage(payload.data)
      } else {
        await dishService.create(payload.data)
      }
      notification.success('Thêm món ăn thành công')
    }
    closeModal()
    loadDishes()
  } catch (error) {
    notification.error('Không thể lưu thông tin món ăn')
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value || 0)
}
</script>
