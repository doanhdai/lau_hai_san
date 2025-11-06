<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Sơ đồ Bàn ăn</h1>
        <p class="text-gray-600 mt-1">Quản lý vị trí và trạng thái bàn trực quan</p>
      </div>
      <div class="flex items-center gap-3">
        <button @click="resetView" class="btn-secondary">
          <ArrowPathIcon class="w-5 h-5 mr-2" />
          Reset View
        </button>
        <button @click="saveLayout" class="btn-primary">
          <CheckIcon class="w-5 h-5 mr-2" />
          Lưu bố cục
        </button>
      </div>
    </div>

    <!-- Legend & Stats -->
    <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
      <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center">
            <CheckCircleIcon class="w-6 h-6" />
          </div>
          <div>
            <p class="text-sm text-green-100">Trống</p>
            <p class="text-2xl font-bold">{{ availableCount }}</p>
          </div>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-red-500 to-red-600 text-white">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center">
            <UsersIcon class="w-6 h-6" />
          </div>
          <div>
            <p class="text-sm text-red-100">Phục vụ</p>
            <p class="text-2xl font-bold">{{ occupiedCount }}</p>
          </div>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-yellow-500 to-yellow-600 text-white">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center">
            <ClockIcon class="w-6 h-6" />
          </div>
          <div>
            <p class="text-sm text-yellow-100">Đã đặt</p>
            <p class="text-2xl font-bold">{{ reservedCount }}</p>
          </div>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-gray-500 to-gray-600 text-white">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center">
            <WrenchIcon class="w-6 h-6" />
          </div>
          <div>
            <p class="text-sm text-gray-100">Bảo trì</p>
            <p class="text-2xl font-bold">{{ maintenanceCount }}</p>
          </div>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center">
            <TableCellsIcon class="w-6 h-6" />
          </div>
          <div>
            <p class="text-sm text-blue-100">Tổng số</p>
            <p class="text-2xl font-bold">{{ tables.length }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Instructions -->
    <div class="card bg-blue-50 border-l-4 border-blue-500">
      <div class="flex items-start gap-3">
        <InformationCircleIcon class="w-6 h-6 text-blue-500 flex-shrink-0 mt-0.5" />
        <div>
          <h3 class="font-semibold text-blue-900 mb-1">Hướng dẫn sử dụng</h3>
          <ul class="text-sm text-blue-700 space-y-1">
            <li>• <strong>Kéo thả</strong> bàn để sắp xếp lại vị trí</li>
            <li>• <strong>Click</strong> vào bàn để xem chi tiết và thay đổi trạng thái</li>
            <li>• <strong>Nhấn "Lưu bố cục"</strong> để lưu vị trí mới</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Restaurant Floor Map -->
    <div class="card p-0 overflow-hidden">
      <div class="bg-gradient-to-br from-gray-50 to-gray-100 p-6">
        <div
          ref="mapContainer"
          class="relative bg-white rounded-lg shadow-inner"
          style="height: 600px; border: 2px dashed #cbd5e1"
          @dragover.prevent
          @drop="onDrop"
        >
          <!-- Grid lines -->
          <div class="absolute inset-0 pointer-events-none" style="background-image: repeating-linear-gradient(0deg, transparent, transparent 49px, #e2e8f0 49px, #e2e8f0 50px), repeating-linear-gradient(90deg, transparent, transparent 49px, #e2e8f0 49px, #e2e8f0 50px); background-size: 50px 50px;" />

          <!-- Tables -->
          <div
            v-for="table in tables"
            :key="table.id"
            :style="{
              position: 'absolute',
              left: table.positionX + 'px',
              top: table.positionY + 'px',
              transform: isDragging && draggedTable?.id === table.id ? 'scale(1.1)' : 'scale(1)',
              zIndex: isDragging && draggedTable?.id === table.id ? 1000 : 1
            }"
            :class="[
              'table-item cursor-move transition-all duration-200',
              getTableClass(table.status),
              isDragging && draggedTable?.id === table.id ? 'opacity-75 shadow-2xl' : 'hover:shadow-xl'
            ]"
            draggable="true"
            @dragstart="onDragStart(table, $event)"
            @dragend="onDragEnd"
            @click="selectTable(table)"
          >
            <!-- Table visual -->
            <div class="relative">
              <!-- Table number badge -->
              <div class="absolute -top-2 -right-2 w-8 h-8 bg-white rounded-full shadow-md flex items-center justify-center border-2" :class="getBorderClass(table.status)">
                <span class="text-xs font-bold" :class="getTextClass(table.status)">{{ table.tableNumber }}</span>
              </div>

              <!-- Table icon -->
              <div class="w-20 h-20 rounded-lg flex items-center justify-center" :class="getTableBgClass(table.status)">
                <TableCellsIcon class="w-10 h-10" :class="getTableIconClass(table.status)" />
              </div>

              <!-- Capacity -->
              <div class="mt-2 text-center">
                <div class="flex items-center justify-center gap-1 text-xs font-semibold" :class="getTextClass(table.status)">
                  <UsersIcon class="w-3 h-3" />
                  <span>{{ table.capacity }}</span>
                </div>
              </div>

              <!-- Status badge -->
              <div class="mt-1">
                <span class="text-xs px-2 py-0.5 rounded-full font-medium" :class="getStatusBadgeClass(table.status)">
                  {{ getStatusText(table.status) }}
                </span>
              </div>
            </div>
          </div>

          <!-- Empty state -->
          <div v-if="tables.length === 0" class="absolute inset-0 flex items-center justify-center">
            <div class="text-center text-gray-400">
              <TableCellsIcon class="w-16 h-16 mx-auto mb-3 opacity-50" />
              <p class="text-lg font-medium">Chưa có bàn nào</p>
              <p class="text-sm">Thêm bàn từ trang Quản lý Bàn</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table Detail Modal -->
    <Teleport to="body">
      <div
        v-if="selectedTable"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        @click.self="selectedTable = null"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-md p-6 animate-slide-up">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-2xl font-bold text-gray-900">Bàn {{ selectedTable.tableNumber }}</h3>
            <button @click="selectedTable = null" class="text-gray-400 hover:text-gray-600">
              <XMarkIcon class="w-6 h-6" />
            </button>
          </div>

          <div class="space-y-4">
            <!-- Status -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
              <select v-model="selectedTable.status" class="input-field" @change="updateTableStatus">
                <option value="AVAILABLE">Trống</option>
                <option value="OCCUPIED">Đang phục vụ</option>
                <option value="RESERVED">Đã đặt</option>
                <option value="MAINTENANCE">Bảo trì</option>
              </select>
            </div>

            <!-- Info -->
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gray-50 rounded-lg p-3">
                <p class="text-xs text-gray-600 mb-1">Sức chứa</p>
                <div class="flex items-center gap-1 text-lg font-bold text-gray-900">
                  <UsersIcon class="w-5 h-5" />
                  <span>{{ selectedTable.capacity }} người</span>
                </div>
              </div>
              <div class="bg-gray-50 rounded-lg p-3">
                <p class="text-xs text-gray-600 mb-1">Vị trí</p>
                <div class="text-lg font-bold text-gray-900">
                  <span>{{ selectedTable.location || 'Chưa rõ' }}</span>
                </div>
              </div>
            </div>

            <!-- Notes -->
            <div v-if="selectedTable.notes" class="bg-yellow-50 border-l-4 border-yellow-400 p-3">
              <p class="text-sm text-yellow-700">
                <strong>Ghi chú:</strong> {{ selectedTable.notes }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  TableCellsIcon,
  UsersIcon,
  CheckCircleIcon,
  ClockIcon,
  WrenchIcon,
  XMarkIcon,
  InformationCircleIcon,
  ArrowPathIcon,
  CheckIcon
} from '@heroicons/vue/24/outline'
import tableService from '@/services/tableService'
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()

const tables = ref([])
const selectedTable = ref(null)
const isDragging = ref(false)
const draggedTable = ref(null)
const dragOffset = ref({ x: 0, y: 0 })

// Computed
const availableCount = computed(() => 
  tables.value.filter(t => t.status === 'AVAILABLE').length
)

const occupiedCount = computed(() => 
  tables.value.filter(t => t.status === 'OCCUPIED').length
)

const reservedCount = computed(() => 
  tables.value.filter(t => t.status === 'RESERVED').length
)

const maintenanceCount = computed(() => 
  tables.value.filter(t => t.status === 'MAINTENANCE').length
)

// Methods
const loadTables = async () => {
  try {
    const response = await tableService.getAll()
    tables.value = response.data.data.map(table => ({
      ...table,
      positionX: table.positionX || Math.random() * 700 + 50,
      positionY: table.positionY || Math.random() * 400 + 50
    }))
  } catch (error) {
    notificationStore.error('Không thể tải danh sách bàn')
  }
}

const onDragStart = (table, event) => {
  isDragging.value = true
  draggedTable.value = table
  
  const rect = event.target.getBoundingClientRect()
  dragOffset.value = {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
}

const onDragEnd = () => {
  isDragging.value = false
  draggedTable.value = null
}

const onDrop = (event) => {
  if (!draggedTable.value) return

  const mapContainer = event.currentTarget
  const rect = mapContainer.getBoundingClientRect()
  
  let newX = event.clientX - rect.left - dragOffset.value.x
  let newY = event.clientY - rect.top - dragOffset.value.y

  // Constrain within bounds
  newX = Math.max(10, Math.min(newX, rect.width - 110))
  newY = Math.max(10, Math.min(newY, rect.height - 110))

  // Snap to grid (50px)
  newX = Math.round(newX / 50) * 50
  newY = Math.round(newY / 50) * 50

  const tableIndex = tables.value.findIndex(t => t.id === draggedTable.value.id)
  if (tableIndex !== -1) {
    tables.value[tableIndex].positionX = newX
    tables.value[tableIndex].positionY = newY
  }
}

const selectTable = (table) => {
  selectedTable.value = { ...table }
}

const updateTableStatus = async () => {
  try {
    await tableService.updateStatus(selectedTable.value.id, selectedTable.value.status)
    const tableIndex = tables.value.findIndex(t => t.id === selectedTable.value.id)
    if (tableIndex !== -1) {
      tables.value[tableIndex].status = selectedTable.value.status
    }
    notificationStore.success('Cập nhật trạng thái thành công')
  } catch (error) {
    notificationStore.error('Cập nhật trạng thái thất bại')
  }
}

const saveLayout = () => {
  const layout = tables.value.map(t => ({
    id: t.id,
    positionX: t.positionX,
    positionY: t.positionY
  }))
  localStorage.setItem('tableLayout', JSON.stringify(layout))
  notificationStore.success('Lưu bố cục thành công!')
}

const resetView = () => {
  localStorage.removeItem('tableLayout')
  loadTables()
  notificationStore.success('Đã reset bố cục')
}

// Styling helpers
const getTableClass = (status) => {
  const classes = {
    AVAILABLE: 'bg-green-50 border-2 border-green-300',
    OCCUPIED: 'bg-red-50 border-2 border-red-300',
    RESERVED: 'bg-yellow-50 border-2 border-yellow-300',
    MAINTENANCE: 'bg-gray-50 border-2 border-gray-300'
  }
  return classes[status] || 'bg-gray-50 border-2 border-gray-300'
}

const getTableBgClass = (status) => {
  const classes = {
    AVAILABLE: 'bg-green-100',
    OCCUPIED: 'bg-red-100',
    RESERVED: 'bg-yellow-100',
    MAINTENANCE: 'bg-gray-100'
  }
  return classes[status] || 'bg-gray-100'
}

const getTableIconClass = (status) => {
  const classes = {
    AVAILABLE: 'text-green-600',
    OCCUPIED: 'text-red-600',
    RESERVED: 'text-yellow-600',
    MAINTENANCE: 'text-gray-600'
  }
  return classes[status] || 'text-gray-600'
}

const getBorderClass = (status) => {
  const classes = {
    AVAILABLE: 'border-green-500',
    OCCUPIED: 'border-red-500',
    RESERVED: 'border-yellow-500',
    MAINTENANCE: 'border-gray-500'
  }
  return classes[status] || 'border-gray-500'
}

const getTextClass = (status) => {
  const classes = {
    AVAILABLE: 'text-green-700',
    OCCUPIED: 'text-red-700',
    RESERVED: 'text-yellow-700',
    MAINTENANCE: 'text-gray-700'
  }
  return classes[status] || 'text-gray-700'
}

const getStatusBadgeClass = (status) => {
  const classes = {
    AVAILABLE: 'bg-green-200 text-green-800',
    OCCUPIED: 'bg-red-200 text-red-800',
    RESERVED: 'bg-yellow-200 text-yellow-800',
    MAINTENANCE: 'bg-gray-200 text-gray-800'
  }
  return classes[status] || 'bg-gray-200 text-gray-800'
}

const getStatusText = (status) => {
  const texts = {
    AVAILABLE: 'Trống',
    OCCUPIED: 'Phục vụ',
    RESERVED: 'Đã đặt',
    MAINTENANCE: 'Bảo trì'
  }
  return texts[status] || status
}

onMounted(() => {
  loadTables().then(() => {
    const savedLayout = localStorage.getItem('tableLayout')
    if (savedLayout) {
      const layout = JSON.parse(savedLayout)
      tables.value.forEach(table => {
        const saved = layout.find(l => l.id === table.id)
        if (saved) {
          table.positionX = saved.positionX
          table.positionY = saved.positionY
        }
      })
    }
  })
})
</script>

<style scoped>
.table-item {
  width: 120px;
  padding: 12px;
  border-radius: 12px;
  user-select: none;
}

.animate-slide-up {
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>