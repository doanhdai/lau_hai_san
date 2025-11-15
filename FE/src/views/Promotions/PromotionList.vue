<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Quản lý Khuyến mãi</h1>
        <p class="text-slate-600 mt-1 text-sm">Chương trình khuyến mãi và ưu đãi</p>
      </div>
      <button @click="showCreateModal = true" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
        <i class="fas fa-plus"></i>
        <span>Thêm khuyến mãi</span>
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Đang hoạt động</p>
            <p class="text-2xl font-bold text-slate-900">{{ activeCount }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-check-circle text-green-600 text-xl"></i>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Ngừng hoạt động</p>
            <p class="text-2xl font-bold text-slate-900">{{ inactiveCount }}</p>
          </div>
          <div class="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-times-circle text-gray-600 text-xl"></i>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Tổng khuyến mãi</p>
            <p class="text-2xl font-bold text-slate-900">{{ totalCount }}</p>
          </div>
          <div class="w-12 h-12 bg-amber-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-gift text-amber-600 text-xl"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white border border-gray-200 rounded-lg p-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên chương trình..."
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
          <button @click="refreshData" class="bg-gray-100 hover:bg-gray-200 text-slate-700 w-full px-4 py-2 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors">
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

    <!-- Promotions Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div 
        v-for="promotion in filteredPromotions" 
        :key="promotion.id"
        class="bg-white border rounded-lg p-5 hover:shadow-lg transition-all duration-200"
        :class="promotion.active ? 'border-2 border-amber-200' : 'border-gray-200 opacity-70'"
      >
        <div class="space-y-4">
          <!-- Header -->
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
                <i class="fas fa-gift text-amber-600"></i>
                <span>{{ promotion.promotionName || promotion.name }}</span>
              </h3>
              <p class="text-sm text-slate-600 mt-1 line-clamp-2">{{ promotion.description || '-' }}</p>
            </div>
            <span :class="promotion.active ? 'px-2.5 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800' : 'px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800'">
              {{ promotion.active ? 'Hoạt động' : 'Tắt' }}
            </span>
          </div>

          <!-- Discount Value -->
          <div class="bg-amber-50 border border-amber-200 rounded-lg p-4 text-center">
            <p class="text-xs text-amber-700 mb-1 font-medium">Giảm giá</p>
            <p class="text-2xl font-bold text-amber-700">
              <template v-if="promotion.discountType === 'PERCENTAGE' || promotion.discountPercent">
                {{ (promotion.discountValue || promotion.discountPercent) + '%' }}
              </template>
              <template v-else-if="promotion.discountType === 'FIXED' || promotion.discountAmount">
                {{ formatCurrency(promotion.discountValue || promotion.discountAmount) }}
              </template>
              <template v-else>
                -
              </template>
            </p>
          </div>

          <!-- Details -->
          <div class="space-y-2 text-sm">
            <div v-if="promotion.minOrderValue" class="flex justify-between">
              <span class="text-slate-600">Đơn tối thiểu:</span>
              <span class="font-medium text-slate-900">{{ formatCurrency(promotion.minOrderValue) }}</span>
            </div>
            <div v-if="promotion.maxDiscount" class="flex justify-between">
              <span class="text-slate-600">Giảm tối đa:</span>
              <span class="font-medium text-slate-900">{{ formatCurrency(promotion.maxDiscount) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-600">Bắt đầu:</span>
              <span class="font-medium text-slate-900">{{ formatDate(promotion.startDate) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-600">Kết thúc:</span>
              <span class="font-medium text-slate-900">{{ formatDate(promotion.endDate) }}</span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-2 pt-3 border-t border-gray-100">
            <button @click="editPromotion(promotion)" class="flex-1 bg-gray-100 hover:bg-gray-200 text-slate-700 text-sm py-2 rounded-lg font-medium transition-colors flex items-center justify-center gap-1">
              <i class="fas fa-edit text-xs"></i>
              <span>Sửa</span>
            </button>
            <button 
              @click="toggleStatus(promotion)" 
              :class="promotion.active ? 'bg-amber-50 text-amber-700 hover:bg-amber-100' : 'bg-green-50 text-green-700 hover:bg-green-100'"
              class="flex-1 text-sm py-2 px-3 rounded-lg font-medium transition-colors"
            >
              {{ promotion.active ? 'Tắt' : 'Bật' }}
            </button>
            <button @click="confirmDelete(promotion)" class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Xóa">
              <i class="fas fa-trash text-sm"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredPromotions.length === 0" class="bg-white border border-gray-200 rounded-lg text-center py-12">
      <i class="fas fa-gift text-6xl text-slate-300 block mb-4"></i>
      <p class="text-slate-600 text-base">Không tìm thấy khuyến mãi nào</p>
    </div>

    <!-- Promotion Modal -->
    <PromotionModal
      v-if="showCreateModal || selectedPromotion"
      :promotion="selectedPromotion"
      @close="closeModal"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { promotionService } from '@/services/promotionService'
import { useNotificationStore } from '@/stores/notification'
import PromotionModal from '@/components/modals/PromotionModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const promotions = ref([])
const searchQuery = ref('')
const filterStatus = ref('')
const showCreateModal = ref(false)
const selectedPromotion = ref(null)

// Stats computed from promotions array (client-side)
const activeCount = computed(() => {
  return promotions.value.filter(p => p.status === 1 || (p.active === true && !p.status)).length
})

const inactiveCount = computed(() => {
  return promotions.value.filter(p => p.status === 0 || p.status === 2 || (p.active === false && !p.status)).length
})

const totalCount = computed(() => promotions.value.length)

// Client-side filtering
const filteredPromotions = computed(() => {
  let result = promotions.value

  // Filter by search query (name or description)
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(p => 
      (p.promotionName || p.name || '').toLowerCase().includes(query) ||
      (p.description || '').toLowerCase().includes(query)
    )
  }

  // Filter by status (0 = ngừng hoạt động, 1 = hoạt động, 2 = tắt)
  if (filterStatus.value === 'active') {
    result = result.filter(p => p.status === 1 || (p.active === true && !p.status))
  } else if (filterStatus.value === 'inactive') {
    result = result.filter(p => p.status === 0 || p.status === 2 || (p.active === false && !p.status))
  }

  return result
})

onMounted(() => {
  loadPromotions()
})

async function loadPromotions() {
  loading.value = true
  try {
    const response = await promotionService.getAll()
    if (response.success) {
      promotions.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách khuyến mãi')
  } finally {
    loading.value = false
  }
}

function editPromotion(promotion) {
  selectedPromotion.value = { ...promotion }
}

async function toggleStatus(promotion) {
  const action = promotion.active ? 'tắt' : 'bật'
  const promotionName = promotion.promotionName || promotion.name
  
  if (!confirm(`Bạn có chắc muốn ${action} khuyến mãi "${promotionName}"?`)) {
    return
  }
  
  try {
    if (promotion.active) {
      await promotionService.deactivate(promotion.id)
      notification.success('Đã tắt khuyến mãi')
    } else {
      await promotionService.activate(promotion.id)
      notification.success('Đã bật khuyến mãi')
    }
    loadPromotions()
  } catch (error) {
    notification.error('Không thể cập nhật trạng thái')
  }
}

async function confirmDelete(promotion) {
  if (confirm(`Bạn có chắc muốn xóa khuyến mãi "${promotion.promotionName || promotion.name}"?`)) {
    try {
      await promotionService.delete(promotion.id)
      notification.success('Xóa khuyến mãi thành công')
      loadPromotions()
    } catch (error) {
      notification.error('Không thể xóa khuyến mãi')
    }
  }
}

function closeModal() {
  showCreateModal.value = false
  selectedPromotion.value = null
}

async function handleSave(promotionData) {
  const isUpdate = selectedPromotion.value && selectedPromotion.value.id
  const action = isUpdate ? 'cập nhật' : 'thêm mới'
  const promotionName = promotionData.promotionName || promotionData.name || 'khuyến mãi này'
  
  if (!confirm(`Bạn có chắc muốn ${action} khuyến mãi "${promotionName}"?`)) {
    return
  }
  
  try {
    if (isUpdate) {
      const response = await promotionService.update(selectedPromotion.value.id, promotionData)
      if (response.success) {
        notification.success('Cập nhật khuyến mãi thành công')
      } else {
        notification.error(response.message || 'Cập nhật khuyến mãi thất bại')
        return
      }
    } else {
      const response = await promotionService.create(promotionData)
      if (response.success) {
        notification.success('Thêm khuyến mãi thành công')
      } else {
        notification.error(response.message || 'Thêm khuyến mãi thất bại')
        return
      }
    }
    closeModal()
    loadPromotions()
  } catch (error) {
    console.error('Error saving promotion:', error)
    const errorMessage = error.response?.data?.message || error.message || 'Không thể lưu khuyến mãi'
    notification.error(errorMessage)
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('vi-VN')
}

function refreshData() {
  loadPromotions()
}
</script>
