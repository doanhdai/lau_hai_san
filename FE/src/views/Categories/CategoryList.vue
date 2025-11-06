<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Danh mục</h1>
        <p class="text-gray-600 mt-1">Danh mục món ăn</p>
      </div>
      <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
        <span class="text-lg">➕</span>
        Thêm danh mục
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-blue-100 text-sm">Tổng danh mục</p>
            <p class="text-3xl font-bold mt-1">{{ categories.length }}</p>
          </div>
          <span class="text-4xl opacity-50">📁</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-green-100 text-sm">Đang hoạt động</p>
            <p class="text-3xl font-bold mt-1">{{ activeCount }}</p>
          </div>
          <span class="text-4xl opacity-50">✅</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-gray-500 to-gray-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-100 text-sm">Ngừng hoạt động</p>
            <p class="text-3xl font-bold mt-1">{{ inactiveCount }}</p>
          </div>
          <span class="text-4xl opacity-50">❌</span>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm theo tên..."
            class="input-field"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="input-field">
            <option value="">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Ngừng hoạt động</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="loadCategories" class="btn-secondary w-full flex items-center justify-center gap-2">
            <span class="text-lg">🔄</span>
            Làm mới
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="loading-spinner"></div>
    </div>

    <!-- Categories Table -->
    <div v-else class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tên danh mục</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mô tả</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="category in filteredCategories" :key="category.id" class="hover:bg-gray-50 transition">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ category.id }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white">
                    <span class="text-lg">📁</span>
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-900">{{ category.categoryName }}</p>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">
                <div class="max-w-xs truncate">{{ category.description || '-' }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span v-if="category.active" class="badge-success">Hoạt động</span>
                <span v-else class="badge bg-gray-100 text-gray-800">Ngừng hoạt động</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                  <button @click="editCategory(category)" class="text-blue-600 hover:text-blue-800 font-medium">
                    Sửa
                  </button>
                  <button 
                    @click="toggleStatus(category)" 
                    :class="category.active ? 'text-yellow-600 hover:text-yellow-800' : 'text-green-600 hover:text-green-800'"
                    class="font-medium"
                  >
                    {{ category.active ? 'Tắt' : 'Bật' }}
                  </button>
                  <button @click="confirmDelete(category)" class="text-red-600 hover:text-red-800 font-medium">
                    Xóa
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty state -->
      <div v-if="filteredCategories.length === 0" class="text-center py-12">
        <span class="text-8xl text-gray-300 block mb-4">📁</span>
        <p class="text-gray-500 text-lg">Không tìm thấy danh mục nào</p>
      </div>
    </div>

    <!-- Category Modal -->
    <CategoryModal
      v-if="showCreateModal || selectedCategory"
      :category="selectedCategory"
      @close="closeModal"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { categoryService } from '@/services/categoryService'
import { useNotificationStore } from '@/stores/notification'
// Heroicons replaced with emojis
import CategoryModal from '@/components/modals/CategoryModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const categories = ref([])
const searchQuery = ref('')
const filterStatus = ref('')
const showCreateModal = ref(false)
const selectedCategory = ref(null)

const activeCount = computed(() => categories.value.filter(c => c.active).length)
const inactiveCount = computed(() => categories.value.filter(c => !c.active).length)

const filteredCategories = computed(() => {
  let result = categories.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c => 
      c.categoryName.toLowerCase().includes(query) ||
      (c.description && c.description.toLowerCase().includes(query))
    )
  }

  if (filterStatus.value === 'active') {
    result = result.filter(c => c.active)
  } else if (filterStatus.value === 'inactive') {
    result = result.filter(c => !c.active)
  }

  return result
})

onMounted(() => {
  loadCategories()
})

async function loadCategories() {
  loading.value = true
  try {
    const response = await categoryService.getAll()
    if (response.success) {
      categories.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách danh mục')
  } finally {
    loading.value = false
  }
}

function editCategory(category) {
  selectedCategory.value = { ...category }
}

async function toggleStatus(category) {
  try {
    const updatedData = { ...category, active: !category.active }
    await categoryService.update(category.id, updatedData)
    notification.success(`Đã ${!category.active ? 'bật' : 'tắt'} danh mục`)
    loadCategories()
  } catch (error) {
    notification.error('Không thể cập nhật trạng thái')
  }
}

async function confirmDelete(category) {
  if (confirm(`Bạn có chắc muốn xóa danh mục "${category.categoryName}"?`)) {
    try {
      await categoryService.delete(category.id)
      notification.success('Xóa danh mục thành công')
      loadCategories()
    } catch (error) {
      notification.error('Không thể xóa danh mục')
    }
  }
}

function closeModal() {
  showCreateModal.value = false
  selectedCategory.value = null
}

async function handleSave(categoryData) {
  try {
    if (selectedCategory.value && selectedCategory.value.id) {
      await categoryService.update(selectedCategory.value.id, categoryData)
      notification.success('Cập nhật danh mục thành công')
    } else {
      await categoryService.create(categoryData)
      notification.success('Thêm danh mục thành công')
    }
    closeModal()
    loadCategories()
  } catch (error) {
    notification.error('Không thể lưu danh mục')
  }
}
</script>
