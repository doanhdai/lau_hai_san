<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Khuyến mãi</h1>
        <p class="text-gray-600 mt-1">Chương trình khuyến mãi và ưu đãi</p>
      </div>
      <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
        <span class="text-lg">➕</span>
        Thêm khuyến mãi
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
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
      <div class="card bg-gradient-to-br from-pink-500 to-pink-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-pink-100 text-sm">Tổng khuyến mãi</p>
            <p class="text-3xl font-bold mt-1">{{ promotions.length }}</p>
          </div>
          <span class="text-4xl opacity-50">🎁</span>
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
            placeholder="Tên chương trình..."
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
          <button @click="loadPromotions" class="btn-secondary w-full flex items-center justify-center gap-2">
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

    <!-- Promotions Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="promotion in filteredPromotions" 
        :key="promotion.id"
        class="card hover:shadow-xl transition-all duration-300"
        :class="promotion.active ? 'border-2 border-pink-200' : 'opacity-70'"
      >
        <div class="space-y-4">
          <!-- Header -->
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <h3 class="text-lg font-bold text-gray-900 flex items-center gap-2">
                <span class="text-lg">🎁</span>
                {{ promotion.promotionName }}
              </h3>
              <p class="text-sm text-gray-600 mt-1 line-clamp-2">{{ promotion.description || '-' }}</p>
            </div>
            <span :class="promotion.active ? 'badge-success' : 'badge bg-gray-100 text-gray-800'">
              {{ promotion.active ? 'Hoạt động' : 'Tắt' }}
            </span>
          </div>

          <!-- Discount Value -->
          <div class="bg-pink-50 rounded-lg p-4 text-center">
            <p class="text-sm text-pink-600 mb-1">Giảm giá</p>
            <p class="text-2xl font-bold text-pink-600">
              {{ promotion.discountType === 'PERCENTAGE' ? promotion.discountValue + '%' : formatCurrency(promotion.discountValue) }}
            </p>
          </div>

          <!-- Details -->
          <div class="space-y-2 text-sm">
            <div v-if="promotion.minOrderValue" class="flex justify-between">
              <span class="text-gray-600">Đơn tối thiểu:</span>
              <span class="font-medium text-gray-900">{{ formatCurrency(promotion.minOrderValue) }}</span>
            </div>
            <div v-if="promotion.maxDiscount" class="flex justify-between">
              <span class="text-gray-600">Giảm tối đa:</span>
              <span class="font-medium text-gray-900">{{ formatCurrency(promotion.maxDiscount) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Bắt đầu:</span>
              <span class="font-medium text-gray-900">{{ formatDate(promotion.startDate) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Kết thúc:</span>
              <span class="font-medium text-gray-900">{{ formatDate(promotion.endDate) }}</span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-2 pt-3 border-t">
            <button @click="editPromotion(promotion)" class="flex-1 text-sm btn-secondary py-2">
              Sửa
            </button>
            <button 
              @click="toggleStatus(promotion)" 
              :class="promotion.active ? 'bg-yellow-50 text-yellow-600 hover:bg-yellow-100' : 'bg-green-50 text-green-600 hover:bg-green-100'"
              class="flex-1 text-sm py-2 px-3 rounded-lg font-medium transition"
            >
              {{ promotion.active ? 'Tắt' : 'Bật' }}
            </button>
            <button @click="confirmDelete(promotion)" class="text-sm py-2 px-3 rounded-lg font-medium text-red-600 hover:bg-red-50 transition">
              Xóa
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredPromotions.length === 0" class="card text-center py-12">
      <span class="text-8xl text-gray-300 block mb-4">🎁</span>
      <p class="text-gray-500 text-lg">Không tìm thấy khuyến mãi nào</p>
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
import { ref, computed, onMounted } from 'vue'
import { promotionService } from '@/services/promotionService'
import { useNotificationStore } from '@/stores/notification'
// Heroicons replaced with emojis
import PromotionModal from '@/components/modals/PromotionModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const promotions = ref([])
const searchQuery = ref('')
const filterStatus = ref('')
const showCreateModal = ref(false)
const selectedPromotion = ref(null)

const activeCount = computed(() => promotions.value.filter(p => p.active).length)
const inactiveCount = computed(() => promotions.value.filter(p => !p.active).length)

const filteredPromotions = computed(() => {
  let result = promotions.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(p => 
      p.promotionName.toLowerCase().includes(query) ||
      (p.description && p.description.toLowerCase().includes(query))
    )
  }

  if (filterStatus.value === 'active') {
    result = result.filter(p => p.active)
  } else if (filterStatus.value === 'inactive') {
    result = result.filter(p => !p.active)
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
  if (confirm(`Bạn có chắc muốn xóa khuyến mãi "${promotion.promotionName}"?`)) {
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
  try {
    if (selectedPromotion.value && selectedPromotion.value.id) {
      await promotionService.update(selectedPromotion.value.id, promotionData)
      notification.success('Cập nhật khuyến mãi thành công')
    } else {
      await promotionService.create(promotionData)
      notification.success('Thêm khuyến mãi thành công')
    }
    closeModal()
    loadPromotions()
  } catch (error) {
    notification.error('Không thể lưu khuyến mãi')
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
</script>
