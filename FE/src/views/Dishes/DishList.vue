<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Quản lý Món ăn</h1>
        <p class="text-slate-600 mt-1 text-sm">Thực đơn và món ăn nhà hàng</p>
      </div>
      <button @click="showCreateModal = true" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
        <i class="fas fa-plus"></i>
        <span>Thêm món ăn</span>
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white border border-gray-200 rounded-lg p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên món, mô tả..."
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Danh mục</label>
          <select v-model="filterCategory" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
            <option value="">Tất cả</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
            <option value="">Tất cả</option>
            <option value="AVAILABLE">Còn món</option>
            <option value="UNAVAILABLE">Hết món</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="loadDishes" class="bg-gray-100 hover:bg-gray-200 text-slate-700 w-full px-4 py-2 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors">
            <i class="fas fa-search"></i>
            <span>Tìm kiếm</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Dishes Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div 
        v-for="dish in filteredDishes" 
        :key="dish.id"
        class="bg-white border border-gray-200 rounded-lg overflow-hidden group hover:shadow-lg transition-all duration-200 cursor-pointer"
      >
        <!-- Image -->
        <div class="relative h-48 bg-gray-100 overflow-hidden">
          <div v-if="dish.imageUrl" class="w-full h-full">
            <img :src="dish.imageUrl" :alt="dish.name" class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" />
          </div>
          <div v-else class="w-full h-full flex items-center justify-center bg-gray-50">
            <i class="fas fa-utensils text-5xl text-gray-300"></i>
          </div>
          <!-- Status badge -->
          <div class="absolute top-2 right-2">
            <span v-if="dish.status === 'AVAILABLE'" class="px-2.5 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800">
              Còn món
            </span>
            <span v-else class="px-2.5 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800">
              Hết món
            </span>
          </div>
          <!-- Promotion badge -->
          <div v-if="dish.isPromotion" class="absolute top-2 left-2">
            <span class="px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500 text-white flex items-center gap-1">
              <i class="fas fa-gift text-xs"></i>
              <span>Khuyến mãi</span>
            </span>
          </div>
        </div>

        <!-- Info -->
        <div class="p-4 space-y-3">
          <div>
            <h3 class="font-bold text-base text-slate-900 group-hover:text-slate-700 transition line-clamp-1">
              {{ dish.name }}
            </h3>
            <p class="text-sm text-slate-600 line-clamp-2 mt-1">{{ dish.description || 'Không có mô tả' }}</p>
          </div>

          <div class="flex items-center justify-between">
            <span class="px-2.5 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">{{ dish.categoryName }}</span>
            <span class="text-lg font-bold text-slate-900">{{ formatCurrency(dish.price) }}</span>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-2 pt-3 border-t border-gray-100">
            <button @click.stop="editDish(dish)" class="flex-1 bg-gray-100 hover:bg-gray-200 text-slate-700 text-sm py-2 rounded-lg font-medium transition-colors flex items-center justify-center gap-1">
              <i class="fas fa-edit text-xs"></i>
              <span>Sửa</span>
            </button>
            <button 
              @click.stop="toggleDishStatus(dish)" 
              :class="dish.status === 'AVAILABLE' ? 'bg-amber-500 hover:bg-amber-600' : 'bg-green-500 hover:bg-green-600'"
              class="flex-1 text-white font-medium py-2 px-3 rounded-lg text-sm transition-colors"
            >
              {{ dish.status === 'AVAILABLE' ? 'Hết món' : 'Còn món' }}
            </button>
            <button @click.stop="deleteDish(dish)" class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
              <i class="fas fa-trash text-sm"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredDishes.length === 0" class="bg-white border border-gray-200 rounded-lg text-center py-12">
      <i class="fas fa-utensils text-6xl text-slate-300 block mb-4"></i>
      <p class="text-slate-600 text-base">Không tìm thấy món ăn nào</p>
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
