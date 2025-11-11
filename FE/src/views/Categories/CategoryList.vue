<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Quản lý Danh mục</h1>
        <p class="text-slate-600 mt-1 text-sm">Danh mục món ăn</p>
      </div>
      <button @click="showCreateModal = true" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
        <i class="fas fa-plus"></i>
        <span>Thêm danh mục</span>
      </button>
    </div>



    <!-- Filters -->
    <div class="bg-white border border-gray-200 rounded-lg p-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm theo tên..."
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
            <option value="">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Ngừng hoạt động</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="loadCategories" class="bg-gray-100 hover:bg-gray-200 text-slate-700 w-full px-4 py-2 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors">
            <i class="fas fa-sync-alt"></i>
            <span>Làm mới</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Categories Table -->
    <div v-else class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-slate-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">ID</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Tên danh mục</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Mô tả</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Trạng thái</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="category in filteredCategories" :key="category.id" class="hover:bg-slate-50 transition">
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">{{ category.id }}</td>
              <td class="px-4 py-3 whitespace-nowrap">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 bg-slate-100 rounded-lg flex items-center justify-center">
                    <i class="fas fa-folder text-slate-600"></i>
                  </div>
                  <div>
                    <p class="text-sm font-medium text-slate-900">{{ category.categoryName || category.name }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-slate-600">
                <div class="max-w-xs truncate">{{ category.description || '-' }}</div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap">
                <span v-if="category.active" class="px-2.5 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800">Hoạt động</span>
                <span v-else class="px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800">Ngừng hoạt động</span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center gap-3">
                  <button @click="editCategory(category)" class="text-slate-600 hover:text-slate-900 font-medium flex items-center gap-1">
                    <i class="fas fa-edit text-xs"></i>
                    <span>Sửa</span>
                  </button>
                  <button @click="confirmDelete(category)" class="text-red-600 hover:text-red-800 font-medium flex items-center gap-1">
                    <i class="fas fa-trash text-xs"></i>
                    <span>Xóa</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty state -->
      <div v-if="filteredCategories.length === 0" class="text-center py-12">
        <i class="fas fa-folder-open text-6xl text-slate-300 block mb-4"></i>
        <p class="text-slate-600 text-base">Không tìm thấy danh mục nào</p>
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

const filteredCategories = computed(() => {
  let result = categories.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c => {
      const name = (c.categoryName || c.name || '').toLowerCase()
      const desc = (c.description || '').toLowerCase()
      return name.includes(query) || desc.includes(query)
    })
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

async function confirmDelete(category) {
  const categoryName = category.categoryName || category.name || 'danh mục này'
  if (confirm(`Bạn có chắc muốn xóa danh mục "${categoryName}"?`)) {
    try {
      // Update active = false instead of deleting
      const updatedData = { ...category, active: false }
      await categoryService.update(category.id, updatedData)
      notification.success('Xóa danh mục thành công')
      await loadCategories()
    } catch (error) {
      console.error('Error deleting category:', error)
      const errorMessage = error.response?.data?.message || error.message || 'Không thể xóa danh mục'
      notification.error(errorMessage)
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
