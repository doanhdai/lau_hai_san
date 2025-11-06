<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Đặt bàn</h1>
        <p class="text-gray-600 mt-1">Danh sách đặt bàn khách hàng</p>
      </div>
      <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
        <span class="text-lg">➕</span>
        Thêm đặt bàn
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="card bg-gradient-to-br from-yellow-500 to-yellow-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-yellow-100 text-sm">Chờ xác nhận</p>
            <p class="text-3xl font-bold mt-1">{{ pendingCount }}</p>
          </div>
          <span class="text-4xl opacity-50">⏰</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-green-100 text-sm">Đã xác nhận</p>
            <p class="text-3xl font-bold mt-1">{{ confirmedCount }}</p>
          </div>
          <span class="text-4xl opacity-50">✅</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-blue-100 text-sm">Hoàn thành</p>
            <p class="text-3xl font-bold mt-1">{{ completedCount }}</p>
          </div>
          <span class="text-4xl opacity-50">✔️</span>
        </div>
      </div>
      <div class="card bg-gradient-to-br from-red-500 to-red-600 text-white">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-red-100 text-sm">Đã hủy</p>
            <p class="text-3xl font-bold mt-1">{{ cancelledCount }}</p>
          </div>
          <span class="text-4xl opacity-50">❌</span>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên, SĐT khách hàng..."
            class="input-field"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="input-field">
            <option value="">Tất cả</option>
            <option value="PENDING">Chờ xác nhận</option>
            <option value="CONFIRMED">Đã xác nhận</option>
            <option value="COMPLETED">Hoàn thành</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
          <input v-model="filterDateFrom" type="date" class="input-field" />
        </div>
        <div class="flex items-end">
          <button @click="loadReservations" class="btn-secondary w-full flex items-center justify-center gap-2">
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

    <!-- Reservations Table -->
    <div v-else class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khách hàng</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SĐT</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày giờ</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Số người</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="reservation in filteredReservations" :key="reservation.id" class="hover:bg-gray-50 transition">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ reservation.id }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 bg-gradient-to-br from-indigo-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-bold">
                    {{ reservation.customerName.charAt(0) }}
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-900">{{ reservation.customerName }}</p>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ reservation.customerPhone }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ formatDateTime(reservation.reservationTime) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <span class="flex items-center gap-1">
                  <UsersIcon class="w-4 h-4" />
                  {{ reservation.numberOfGuests }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getStatusBadgeClass(reservation.status)">
                  {{ getStatusLabel(reservation.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                  <button @click="viewReservation(reservation)" class="text-blue-600 hover:text-blue-800 font-medium">
                    Xem
                  </button>
                  <button 
                    v-if="reservation.status === 'PENDING'"
                    @click="confirmReservation(reservation)" 
                    class="text-green-600 hover:text-green-800 font-medium"
                  >
                    Xác nhận
                  </button>
                  <button 
                    v-if="reservation.status === 'PENDING'"
                    @click="cancelReservation(reservation)" 
                    class="text-red-600 hover:text-red-800 font-medium"
                  >
                    Hủy
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty state -->
      <div v-if="filteredReservations.length === 0" class="text-center py-12">
        <CalendarIcon class="w-20 h-20 text-gray-300 mx-auto mb-4" />
        <p class="text-gray-500 text-lg">Không tìm thấy đặt bàn nào</p>
      </div>
    </div>

    <!-- Reservation Modal -->
    <ReservationModal
      v-if="showCreateModal || selectedReservation"
      :reservation="selectedReservation"
      @close="closeModal"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { reservationService } from '@/services/reservationService'
import { useNotificationStore } from '@/stores/notification'
import ReservationModal from '@/components/modals/ReservationModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const reservations = ref([])
const searchQuery = ref('')
const filterStatus = ref('')
const filterDateFrom = ref('')
const showCreateModal = ref(false)
const selectedReservation = ref(null)

const pendingCount = computed(() => reservations.value.filter(r => r.status === 'PENDING').length)
const confirmedCount = computed(() => reservations.value.filter(r => r.status === 'CONFIRMED').length)
const completedCount = computed(() => reservations.value.filter(r => r.status === 'COMPLETED').length)
const cancelledCount = computed(() => reservations.value.filter(r => r.status === 'CANCELLED').length)

const filteredReservations = computed(() => {
  let result = reservations.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(r => 
      r.customerName.toLowerCase().includes(query) ||
      r.customerPhone.includes(query)
    )
  }

  if (filterStatus.value) {
    result = result.filter(r => r.status === filterStatus.value)
  }

  if (filterDateFrom.value) {
    const fromDate = new Date(filterDateFrom.value)
    result = result.filter(r => {
      const reservationTime = r.reservationTime || r.reservationDateTime
      return new Date(reservationTime) >= fromDate
    })
  }

  // Sort by reservationTime (newest first)
  return result.sort((a, b) => {
    const timeA = a.reservationTime || a.reservationDateTime
    const timeB = b.reservationTime || b.reservationDateTime
    return new Date(timeB) - new Date(timeA)
  })
})

onMounted(() => {
  loadReservations()
})

async function loadReservations() {
  loading.value = true
  try {
    const response = await reservationService.getAll()
    if (response.success) {
      reservations.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách đặt bàn')
  } finally {
    loading.value = false
  }
}

function viewReservation(reservation) {
  selectedReservation.value = { ...reservation }
}

async function confirmReservation(reservation) {
  try {
    await reservationService.confirm(reservation.id)
    notification.success('Đã xác nhận đặt bàn')
    loadReservations()
  } catch (error) {
    notification.error('Không thể xác nhận đặt bàn')
  }
}

async function cancelReservation(reservation) {
  if (confirm(`Bạn có chắc muốn hủy đặt bàn cho "${reservation.customerName}"?`)) {
    try {
      await reservationService.cancel(reservation.id)
      notification.success('Đã hủy đặt bàn')
      loadReservations()
    } catch (error) {
      notification.error('Không thể hủy đặt bàn')
    }
  }
}

function closeModal() {
  showCreateModal.value = false
  selectedReservation.value = null
}

async function handleSave(reservationData) {
  try {
    if (selectedReservation.value && selectedReservation.value.id) {
      await reservationService.update(selectedReservation.value.id, reservationData)
      notification.success('Cập nhật đặt bàn thành công')
    } else {
      await reservationService.create(reservationData)
      notification.success('Thêm đặt bàn thành công')
    }
    closeModal()
    loadReservations()
  } catch (error) {
    notification.error('Không thể lưu thông tin đặt bàn')
  }
}

function getStatusBadgeClass(status) {
  const classes = {
    'PENDING': 'badge bg-yellow-100 text-yellow-800',
    'CONFIRMED': 'badge bg-green-100 text-green-800',
    'COMPLETED': 'badge bg-blue-100 text-blue-800',
    'CANCELLED': 'badge bg-red-100 text-red-800'
  }
  return classes[status] || 'badge bg-gray-100 text-gray-800'
}

function getStatusLabel(status) {
  const labels = {
    'PENDING': 'Chờ xác nhận',
    'CONFIRMED': 'Đã xác nhận',
    'COMPLETED': 'Hoàn thành',
    'CANCELLED': 'Đã hủy'
  }
  return labels[status] || status
}

function formatDateTime(datetime) {
  if (!datetime) return '-'
  const date = new Date(datetime)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
