<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Quản lý Đặt bàn</h1>
        <p class="text-slate-600 mt-1 text-sm">Danh sách đặt bàn khách hàng</p>
      </div>
      <button @click="showCreateModal = true" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
        <i class="fas fa-plus"></i>
        <span>Thêm đặt bàn</span>
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Chờ xác nhận</p>
            <p class="text-2xl font-bold text-slate-900">{{ pendingCount }}</p>
          </div>
          <div class="w-12 h-12 bg-amber-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-clock text-amber-600 text-xl"></i>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Đã xác nhận</p>
            <p class="text-2xl font-bold text-slate-900">{{ confirmedCount }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-check-circle text-green-600 text-xl"></i>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Hoàn thành</p>
            <p class="text-2xl font-bold text-slate-900">{{ completedCount }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-check-double text-blue-600 text-xl"></i>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Đã hủy</p>
            <p class="text-2xl font-bold text-slate-900">{{ cancelledCount }}</p>
          </div>
          <div class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-times-circle text-red-600 text-xl"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white border border-gray-200 rounded-lg p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên, SĐT khách hàng..."
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
            <option value="">Tất cả</option>
            <option value="PENDING">Chờ xác nhận</option>
            <option value="CONFIRMED">Đã xác nhận</option>
            <option value="COMPLETED">Hoàn thành</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Từ ngày</label>
          <input v-model="filterDateFrom" type="date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition" />
        </div>
        <div class="flex items-end">
          <button @click="loadReservations" class="bg-gray-100 hover:bg-gray-200 text-slate-700 w-full px-4 py-2 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors">
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

    <!-- Reservations Table -->
    <div v-else class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-slate-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">ID</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Khách hàng</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">SĐT</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Ngày giờ</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Số người</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Trạng thái</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="reservation in filteredReservations" :key="reservation.id" class="hover:bg-slate-50 transition">
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">{{ reservation.id }}</td>
              <td class="px-4 py-3 whitespace-nowrap">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 bg-slate-900 rounded-full flex items-center justify-center text-white font-semibold text-sm">
                    {{ reservation.customerName.charAt(0) }}
                  </div>
                  <div>
                    <p class="text-sm font-medium text-slate-900">{{ reservation.customerName }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">{{ reservation.customerPhone }}</td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">
                {{ formatDateTime(reservation.reservationTime) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">
                <span class="flex items-center gap-1.5">
                  <i class="fas fa-users text-xs text-slate-500"></i>
                  <span>{{ reservation.numberOfGuests }}</span>
                </span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap">
                <span :class="getStatusBadgeClass(reservation.status)">
                  {{ getStatusLabel(reservation.status) }}
                </span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                  <button @click="viewReservation(reservation)" class="text-slate-600 hover:text-slate-900 font-medium flex items-center gap-1">
                    <i class="fas fa-eye text-xs"></i>
                    <span>Xem</span>
                  </button>
                  <button 
                    v-if="reservation.status === 'PENDING'"
                    @click="confirmReservation(reservation)" 
                    class="text-green-600 hover:text-green-800 font-medium flex items-center gap-1"
                  >
                    <i class="fas fa-check text-xs"></i>
                    <span>Xác nhận</span>
                  </button>
                  <button 
                    v-if="reservation.status === 'PENDING'"
                    @click="cancelReservation(reservation)" 
                    class="text-red-600 hover:text-red-800 font-medium flex items-center gap-1"
                  >
                    <i class="fas fa-times text-xs"></i>
                    <span>Hủy</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty state -->
      <div v-if="filteredReservations.length === 0" class="text-center py-12">
        <i class="fas fa-calendar text-6xl text-slate-300 block mb-4"></i>
        <p class="text-slate-600 text-base">Không tìm thấy đặt bàn nào</p>
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
    'PENDING': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800',
    'CONFIRMED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800',
    'COMPLETED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-800',
    'CANCELLED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800'
  }
  return classes[status] || 'px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800'
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
