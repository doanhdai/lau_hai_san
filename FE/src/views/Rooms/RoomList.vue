<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Phòng</h1>
        <p class="text-gray-600 mt-1">Danh sách phòng VIP và phòng riêng</p>
      </div>
      <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
        <span class="text-lg">➕</span>
        Thêm phòng mới
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-green-100 text-sm">Phòng trống</p>
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
      <div class="card bg-gradient-to-br from-purple-500 to-purple-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-purple-100 text-sm">Tổng số phòng</p>
            <p class="text-3xl font-bold mt-1">{{ rooms.length }}</p>
          </div>
          <span class="text-4xl opacity-50">🏢</span>
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
          <button @click="loadRooms" class="btn-secondary w-full flex items-center justify-center gap-2">
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

    <!-- Rooms Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="room in filteredRooms" 
        :key="room.id"
        class="card cursor-pointer transform transition-all duration-300 hover:scale-105"
        :class="getRoomCardClass(room.status)"
        @click="selectRoom(room)"
      >
        <div class="space-y-4">
          <!-- Room Icon & Status -->
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-3">
              <div class="w-14 h-14 rounded-full flex items-center justify-center" :class="getRoomIconBg(room.status)">
                <span class="text-2xl">🏢</span>
              </div>
              <div>
                <h3 class="text-xl font-bold text-gray-900">{{ room.roomName }}</h3>
                <p class="text-sm text-gray-600">Sức chứa: {{ room.capacity }} người</p>
              </div>
            </div>
            <span :class="getStatusBadgeClass(room.status)">
              {{ getStatusLabel(room.status) }}
            </span>
          </div>

          <!-- Description -->
          <div v-if="room.description" class="text-sm text-gray-600 line-clamp-2">
            {{ room.description }}
          </div>

          <!-- Price -->
          <div v-if="room.price" class="flex items-center justify-between pt-3 border-t">
            <span class="text-sm text-gray-600">Giá thuê:</span>
            <span class="text-lg font-bold text-purple-600">{{ formatCurrency(room.price) }}</span>
          </div>

          <!-- Quick Actions -->
          <div class="flex gap-2 pt-2 border-t">
            <button 
              @click.stop="changeStatus(room)"
              class="flex-1 text-sm btn-secondary py-2"
              :disabled="room.status === 'MAINTENANCE'"
            >
              Đổi trạng thái
            </button>
            <button @click.stop="editRoom(room)" class="flex-1 text-sm btn-secondary py-2">
              Chỉnh sửa
            </button>
            <button @click.stop="confirmDelete(room)" class="text-sm btn-secondary py-2 px-3 text-red-600 hover:bg-red-50">
              Xóa
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && filteredRooms.length === 0" class="card text-center py-12">
      <span class="text-8xl text-gray-300 block mb-4">🏢</span>
      <p class="text-gray-500 text-lg">Không tìm thấy phòng nào</p>
    </div>

    <!-- Room Modal -->
    <RoomModal
      v-if="showCreateModal || selectedRoom"
      :room="selectedRoom"
      @close="closeModal"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { roomService } from '@/services/roomService'
import { useNotificationStore } from '@/stores/notification'
// Heroicons replaced with emojis
import RoomModal from '@/components/modals/RoomModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const rooms = ref([])
const filterStatus = ref('')
const minCapacity = ref(0)
const showCreateModal = ref(false)
const selectedRoom = ref(null)

const availableCount = computed(() => rooms.value.filter(r => r.status === 'AVAILABLE').length)
const occupiedCount = computed(() => rooms.value.filter(r => r.status === 'OCCUPIED').length)
const reservedCount = computed(() => rooms.value.filter(r => r.status === 'RESERVED').length)

const filteredRooms = computed(() => {
  let result = rooms.value

  if (filterStatus.value) {
    result = result.filter(r => r.status === filterStatus.value)
  }

  if (minCapacity.value > 0) {
    result = result.filter(r => r.capacity >= minCapacity.value)
  }

  return result
})

onMounted(() => {
  loadRooms()
})

async function loadRooms() {
  loading.value = true
  try {
    const response = await roomService.getAll()
    if (response.success) {
      rooms.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách phòng')
  } finally {
    loading.value = false
  }
}

function selectRoom(room) {
  selectedRoom.value = { ...room }
}

function editRoom(room) {
  selectedRoom.value = { ...room }
}

async function changeStatus(room) {
  const statusMap = {
    'AVAILABLE': 'OCCUPIED',
    'OCCUPIED': 'AVAILABLE',
    'RESERVED': 'AVAILABLE'
  }
  
  const newStatus = statusMap[room.status]
  if (!newStatus) return

  try {
    await roomService.updateStatus(room.id, newStatus)
    notification.success('Đã cập nhật trạng thái phòng')
    loadRooms()
  } catch (error) {
    notification.error('Không thể cập nhật trạng thái')
  }
}

async function confirmDelete(room) {
  if (confirm(`Bạn có chắc muốn xóa phòng "${room.roomName}"?`)) {
    try {
      await roomService.delete(room.id)
      notification.success('Xóa phòng thành công')
      loadRooms()
    } catch (error) {
      notification.error('Không thể xóa phòng')
    }
  }
}

function closeModal() {
  showCreateModal.value = false
  selectedRoom.value = null
}

async function handleSave(roomData) {
  try {
    if (selectedRoom.value && selectedRoom.value.id) {
      await roomService.update(selectedRoom.value.id, roomData)
      notification.success('Cập nhật phòng thành công')
    } else {
      await roomService.create(roomData)
      notification.success('Thêm phòng thành công')
    }
    closeModal()
    loadRooms()
  } catch (error) {
    notification.error('Không thể lưu thông tin phòng')
  }
}

function getRoomCardClass(status) {
  const classes = {
    'AVAILABLE': 'border-2 border-green-200 hover:border-green-400',
    'OCCUPIED': 'border-2 border-red-200 hover:border-red-400',
    'RESERVED': 'border-2 border-yellow-200 hover:border-yellow-400',
    'MAINTENANCE': 'border-2 border-gray-200 hover:border-gray-400 opacity-60'
  }
  return classes[status] || 'border-2 border-gray-200'
}

function getRoomIconBg(status) {
  const classes = {
    'AVAILABLE': 'bg-green-100',
    'OCCUPIED': 'bg-red-100',
    'RESERVED': 'bg-yellow-100',
    'MAINTENANCE': 'bg-gray-100'
  }
  return classes[status] || 'bg-gray-100'
}

function getRoomIconColor(status) {
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

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}
</script>
