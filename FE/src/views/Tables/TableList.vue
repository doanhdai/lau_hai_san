<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Bàn</h1>
        <p class="text-gray-600 mt-1">Danh sách và trạng thái bàn ăn</p>
      </div>
      <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
        <span class="text-lg">➕</span>
        Thêm bàn mới
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-green-100 text-sm">Bàn trống</p>
            <p class="text-3xl font-bold mt-1">{{ availableCount }}</p>
          </div>
          <span class="text-4xl opacity-50">✅</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-red-500 to-red-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-red-100 text-sm">Đang phục vụ</p>
            <p class="text-3xl font-bold mt-1">{{ occupiedCount }}</p>
          </div>
          <span class="text-4xl opacity-50">👥</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-yellow-500 to-yellow-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-yellow-100 text-sm">Đã đặt</p>
            <p class="text-3xl font-bold mt-1">{{ reservedCount }}</p>
          </div>
          <span class="text-4xl opacity-50">⏰</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-gray-500 to-gray-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-100 text-sm">Tổng số bàn</p>
            <p class="text-3xl font-bold mt-1">{{ tables.length }}</p>
          </div>
          <span class="text-4xl opacity-50">🪑</span>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="input-field">
            <option value="">Tất cả</option>
            <option value="AVAILABLE">Trống</option>
            <option value="OCCUPIED">Đang phục vụ</option>
            <option value="RESERVED">Đã đặt</option>
            <option value="MAINTENANCE">Bảo trì</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Sức chứa tối thiểu</label>
          <input v-model.number="minCapacity" type="number" min="0" class="input-field" placeholder="0" />
        </div>
        <div class="flex items-end">
          <button @click="loadTables" class="btn-secondary w-full flex items-center justify-center gap-2">
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

    <!-- Tables Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
      <div 
        v-for="table in filteredTables" 
        :key="table.id"
        class="card cursor-pointer transform transition-all duration-300 hover:scale-105"
        :class="getTableCardClass(table.status)"
        @click="selectTable(table)"
      >
        <div class="text-center space-y-3">
          <!-- Table Icon -->
          <div class="flex justify-center">
            <div class="w-16 h-16 rounded-full flex items-center justify-center" :class="getTableIconBg(table.status)">
              <span class="text-3xl">🪑</span>
            </div>
          </div>

          <!-- Table Number -->
          <div>
            <h3 class="text-xl font-bold text-gray-900">{{ table.tableNumber }}</h3>
            <p class="text-sm text-gray-600">Sức chứa: {{ table.capacity }} người</p>
          </div>

          <!-- Status Badge -->
          <div>
            <span :class="getStatusBadgeClass(table.status)">
              {{ getStatusLabel(table.status) }}
            </span>
          </div>

          <!-- Room -->
          <div v-if="table.roomName">
            <p class="text-xs text-gray-500">{{ table.roomName }}</p>
          </div>

          <!-- Location -->
          <div v-if="table.location">
            <p class="text-xs text-gray-500">{{ table.location }}</p>
          </div>

          <!-- Quick Actions -->
          <div class="flex gap-2 pt-2 border-t">
            <button 
              @click.stop="changeStatus(table)"
              class="flex-1 text-xs btn-secondary py-1"
              :disabled="table.status === 'MAINTENANCE'"
            >
              Đổi TT
            </button>
            <button @click.stop="editTable(table)" class="flex-1 text-xs btn-secondary py-1">
              Sửa
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredTables.length === 0" class="card text-center py-12">
      <span class="text-8xl text-gray-300 block mb-4">🪑</span>
      <p class="text-gray-500 text-lg">Không tìm thấy bàn nào</p>
    </div>

    <!-- Table Modal -->
    <TableModal
      v-if="showCreateModal || selectedTable"
      :table="selectedTable"
      @close="closeModal"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { tableService } from '@/services/tableService'
import { useNotificationStore } from '@/stores/notification'
// Heroicons replaced with emojis
import TableModal from '@/components/modals/TableModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const tables = ref([])
const filterStatus = ref('')
const minCapacity = ref(0)
const showCreateModal = ref(false)
const selectedTable = ref(null)

const availableCount = computed(() => tables.value.filter(t => t.status === 'AVAILABLE').length)
const occupiedCount = computed(() => tables.value.filter(t => t.status === 'OCCUPIED').length)
const reservedCount = computed(() => tables.value.filter(t => t.status === 'RESERVED').length)

const filteredTables = computed(() => {
  let result = tables.value

  if (filterStatus.value) {
    result = result.filter(t => t.status === filterStatus.value)
  }

  if (minCapacity.value > 0) {
    result = result.filter(t => t.capacity >= minCapacity.value)
  }

  return result
})

onMounted(() => {
  loadTables()
  
  // Auto-refresh every 30 seconds to update table status when reservations change
  const refreshInterval = setInterval(() => {
    loadTables()
  }, 30000)
  
  // Cleanup on unmount
  onUnmounted(() => {
    clearInterval(refreshInterval)
  })
})

async function loadTables() {
  loading.value = true
  try {
    const response = await tableService.getAll()
    if (response.success) {
      tables.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách bàn')
  } finally {
    loading.value = false
  }
}

function selectTable(table) {
  selectedTable.value = { ...table }
}

function editTable(table) {
  selectedTable.value = { ...table }
}

async function changeStatus(table) {
  const statusMap = {
    'AVAILABLE': 'OCCUPIED',
    'OCCUPIED': 'AVAILABLE',
    'RESERVED': 'AVAILABLE'
  }
  
  const newStatus = statusMap[table.status]
  if (!newStatus) return

  try {
    await tableService.updateStatus(table.id, newStatus)
    notification.success('Đã cập nhật trạng thái bàn')
    loadTables()
  } catch (error) {
    notification.error('Không thể cập nhật trạng thái')
  }
}

function closeModal() {
  showCreateModal.value = false
  selectedTable.value = null
}

async function handleSave(tableData) {
  try {
    if (selectedTable.value && selectedTable.value.id) {
      await tableService.update(selectedTable.value.id, tableData)
      notification.success('Cập nhật bàn thành công')
    } else {
      await tableService.create(tableData)
      notification.success('Thêm bàn thành công')
    }
    closeModal()
    loadTables()
  } catch (error) {
    notification.error('Không thể lưu thông tin bàn')
  }
}

function getTableCardClass(status) {
  const classes = {
    'AVAILABLE': 'border-2 border-green-200 hover:border-green-400',
    'OCCUPIED': 'border-2 border-red-200 hover:border-red-400',
    'RESERVED': 'border-2 border-yellow-200 hover:border-yellow-400',
    'MAINTENANCE': 'border-2 border-gray-200 hover:border-gray-400 opacity-60'
  }
  return classes[status] || 'border-2 border-gray-200'
}

function getTableIconBg(status) {
  const classes = {
    'AVAILABLE': 'bg-green-100',
    'OCCUPIED': 'bg-red-100',
    'RESERVED': 'bg-yellow-100',
    'MAINTENANCE': 'bg-gray-100'
  }
  return classes[status] || 'bg-gray-100'
}

function getTableIconColor(status) {
  const classes = {
    'AVAILABLE': 'text-green-600',
    'OCCUPIED': 'text-red-600',
    'RESERVED': 'text-yellow-600',
    'MAINTENANCE': 'text-gray-600'
  }
  return classes[status] || 'text-gray-600'
}

function getStatusBadgeClass(status) {
  const classes = {
    'AVAILABLE': 'badge bg-green-100 text-green-800',
    'OCCUPIED': 'badge bg-red-100 text-red-800',
    'RESERVED': 'badge bg-yellow-100 text-yellow-800',
    'MAINTENANCE': 'badge bg-gray-100 text-gray-800'
  }
  return classes[status] || 'badge bg-gray-100 text-gray-800'
}

function getStatusLabel(status) {
  const labels = {
    'AVAILABLE': 'Trống',
    'OCCUPIED': 'Đang phục vụ',
    'RESERVED': 'Đã đặt',
    'MAINTENANCE': 'Bảo trì'
  }
  return labels[status] || status
}
</script>
