<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Quản lý bàn Bàn</h1>
        <p class="text-slate-600 mt-1 text-sm">Danh sách và trạng thái bàn ăn</p>
      </div>
      <button @click="showCreateModal = true" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
        <i class="fas fa-plus"></i>
        <span>Thêm bàn mới</span>
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Bàn trống</p>
            <p class="text-2xl font-bold text-slate-900">{{ availableCount }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-check-circle text-green-600 text-xl"></i>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Đang phục vụ</p>
            <p class="text-2xl font-bold text-slate-900">{{ occupiedCount }}</p>
          </div>
          <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-users text-red-600 text-xl"></i>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Đã đặt</p>
            <p class="text-2xl font-bold text-slate-900">{{ reservedCount }}</p>
          </div>
          <div class="w-12 h-12 bg-amber-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-clock text-amber-600 text-xl"></i>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Tổng số bàn</p>
            <p class="text-2xl font-bold text-slate-900">{{ tables.length }}</p>
          </div>
          <div class="w-12 h-12 bg-slate-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-chair text-slate-600 text-xl"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white border border-gray-200 rounded-lg p-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
            <option value="">Tất cả</option>
            <option value="AVAILABLE">Trống</option>
            <option value="OCCUPIED">Đang phục vụ</option>
            <option value="RESERVED">Đã đặt</option>
            <option value="MAINTENANCE">Bảo trì</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Sức chứa tối thiểu</label>
          <input v-model.number="minCapacity" type="number" min="0" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition" placeholder="0" />
        </div>
        <div class="flex items-end">
          <button @click="loadTables" class="bg-gray-100 hover:bg-gray-200 text-slate-700 w-full px-4 py-2 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors">
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

    <!-- Tables Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
      <div 
        v-for="table in filteredTables" 
        :key="table.id"
        class="bg-white border rounded-lg p-5 cursor-pointer transition-all duration-200 hover:shadow-lg"
        :class="getTableCardClass(table.status)"
        @click="selectTable(table)"
      >
        <div class="text-center space-y-3">
          <!-- Table Icon -->
          <div class="flex justify-center">
            <div class="w-16 h-16 rounded-lg flex items-center justify-center" :class="getTableIconBg(table.status)">
              <i :class="['fas', 'fa-chair', 'text-2xl', getTableIconColor(table.status)]"></i>
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
            <p class="text-xs text-slate-500">{{ table.roomName }}</p>
          </div>

          <!-- Location -->
          <div v-if="table.location">
            <p class="text-xs text-slate-500">{{ table.location }}</p>
          </div>

          <!-- Quick Actions -->
          <div class="flex gap-2 pt-3 border-t border-gray-100">
            <button 
              @click.stop="changeStatus(table)"
              class="flex-1 text-xs bg-gray-100 hover:bg-gray-200 text-slate-700 py-2 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-1"
              :disabled="table.status === 'MAINTENANCE'"
            >
              <i class="fas fa-sync-alt text-xs"></i>
              <span>Đổi TT</span>
            </button>
            <button @click.stop="editTable(table)" class="flex-1 text-xs bg-gray-100 hover:bg-gray-200 text-slate-700 py-2 rounded-lg font-medium transition-colors flex items-center justify-center gap-1">
              <i class="fas fa-edit text-xs"></i>
              <span>Sửa</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredTables.length === 0" class="bg-white border border-gray-200 rounded-lg text-center py-12">
      <i class="fas fa-chair text-6xl text-slate-300 block mb-4"></i>
      <p class="text-slate-600 text-base">Không tìm thấy bàn nào</p>
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
    'RESERVED': 'border-2 border-amber-200 hover:border-amber-400',
    'MAINTENANCE': 'border-2 border-gray-200 hover:border-gray-400 opacity-60'
  }
  return classes[status] || 'border-2 border-gray-200'
}

function getTableIconBg(status) {
  const classes = {
    'AVAILABLE': 'bg-green-100',
    'OCCUPIED': 'bg-red-100',
    'RESERVED': 'bg-amber-100',
    'MAINTENANCE': 'bg-gray-100'
  }
  return classes[status] || 'bg-gray-100'
}

function getTableIconColor(status) {
  const classes = {
    'AVAILABLE': 'text-green-600',
    'OCCUPIED': 'text-red-600',
    'RESERVED': 'text-amber-600',
    'MAINTENANCE': 'text-gray-600'
  }
  return classes[status] || 'text-gray-600'
}

function getStatusBadgeClass(status) {
  const classes = {
    'AVAILABLE': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800',
    'OCCUPIED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800',
    'RESERVED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800',
    'MAINTENANCE': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800'
  }
  return classes[status] || 'px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800'
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
