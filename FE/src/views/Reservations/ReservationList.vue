<template>
  <div class="space-y-3 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold text-slate-900">Quản lý đặt bàn</h1>
        <p class="text-slate-600 mt-1 text-xs">Kiểm soát bàn và đặt đơn</p>
      </div>
      <div class="flex items-center gap-2">
        <!-- <button @click="showCreateModal = true" class="bg-slate-900 hover:bg-slate-800 text-white px-3 py-1.5 rounded-lg text-sm font-medium flex items-center gap-1.5 transition-colors">
          <i class="fas fa-plus text-xs"></i>
          <span>Thêm đặt bàn</span>
      </button> -->
      </div>
    </div>


    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-2">
      <div class="bg-white border border-gray-200 rounded-lg p-2">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-amber-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-clock text-amber-600 text-sm"></i>
          </div>
          <div>
            <p class="text-xs text-slate-500 mb-0.5">Chờ xác nhận</p>
            <p class="text-lg font-bold text-slate-900">{{ pendingCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-2">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-check-circle text-green-600 text-sm"></i>
          </div>
          <div>
            <p class="text-xs text-slate-500 mb-0.5">Đã xác nhận</p>
            <p class="text-lg font-bold text-slate-900">{{ confirmedCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-2">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-check-double text-blue-600 text-sm"></i>
          </div>
          <div>
            <p class="text-xs text-slate-500 mb-0.5">Hoàn thành</p>
            <p class="text-lg font-bold text-slate-900">{{ completedCount }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-2">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-red-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-times-circle text-red-600 text-sm"></i>
          </div>
          <div>
            <p class="text-xs text-slate-500 mb-0.5">Đã hủy</p>
            <p class="text-lg font-bold text-slate-900">{{ cancelledCount }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Content: List View -->
    <template v-else>
    <!-- Filters -->
        <div class="bg-white border border-gray-200 rounded-lg p-3 mb-3">
          <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
        <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên, SĐT khách hàng..."
                class="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent text-sm"
          />
        </div>
        <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Trạng thái</label>
              <select v-model="filterStatus" class="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent text-sm">
            <option value="">Tất cả</option>
            <option value="PENDING">Chờ xác nhận</option>
            <option value="CONFIRMED">Đã xác nhận</option>
            <option value="COMPLETED">Hoàn thành</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>
        <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Từ ngày</label>
              <input v-model="filterDateFrom" type="date" class="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent text-sm" />
        </div>
        <div class="flex items-end">
              <button @click="loadData" class="bg-gray-100 hover:bg-gray-200 text-slate-700 w-full px-3 py-1.5 rounded-lg text-sm font-medium flex items-center justify-center gap-2 transition-colors">
                <i class="fas fa-search text-xs"></i>
                <span>Tìm kiếm</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Reservations Table -->
        <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-slate-50">
                <tr>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">ID</th>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Khách hàng</th>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">SĐT</th>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Bàn</th>
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
                    <div class="flex items-center gap-2">
                      <div class="w-8 h-8 bg-slate-900 rounded-full flex items-center justify-center text-white font-semibold text-xs">
                        {{ reservation.customerName ? reservation.customerName.charAt(0).toUpperCase() : '?' }}
                  </div>
                      <p class="text-sm font-medium text-slate-900">{{ reservation.customerName || 'N/A' }}</p>
                </div>
              </td>
                  <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">{{ reservation.customerPhone }}</td>
                  <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">
                    <button
                      @click="openTableSelectModal(reservation)"
                      :disabled="isReservationCheckedIn(reservation)"
                      class="font-mono font-semibold transition-colors"
                      :class="
                        isReservationCheckedIn(reservation)
                          ? 'text-gray-400 cursor-not-allowed opacity-60'
                          : reservation.tableNumber
                          ? 'text-blue-600 hover:text-blue-800 hover:underline'
                          : 'text-orange-600 hover:text-orange-800 hover:underline'
                      "
                      :title="isReservationCheckedIn(reservation) ? 'Đã check-in, không thể thay đổi bàn' : 'Click để chọn bàn'"
                    >
                      {{ reservation.tableNumber || 'Chọn bàn' }}
                    </button>
                  </td>
                  <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">
                    {{ formatDateTime(reservation.reservationTime || reservation.reservationDateTime) }}
              </td>
                  <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">
                <span class="flex items-center gap-1">
                      <i class="fas fa-users text-xs text-slate-500"></i>
                  {{ reservation.numberOfGuests }}
                </span>
              </td>
                  <td class="px-4 py-3 whitespace-nowrap">
                <span :class="getStatusBadgeClass(reservation.status)">
                  {{ getStatusLabel(reservation.status) }}
                </span>
              </td>
                  <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                      <button @click="viewReservationDetail(reservation)" class="text-slate-600 hover:text-slate-900">
                        <i class="fas fa-eye text-xs"></i>
                  </button>
                  <!-- <button 
                    v-if="reservation.status === 'PENDING'"
                    @click="confirmReservation(reservation)" 
                        class="text-blue-600 hover:text-blue-800"
                        title="Xác nhận"
                  >
                        <i class="fas fa-check text-xs"></i>
                  </button> -->
                  <button 
                        v-if="reservation.status === 'PENDING' || reservation.status === 'CONFIRMED'"
                    @click="cancelReservation(reservation)" 
                        class="text-red-600 hover:text-red-800"
                        title="Hủy"
                  >
                        <i class="fas fa-times text-xs"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty state -->
      <div v-if="filteredReservations.length === 0" class="text-center py-12">
            <i class="fas fa-calendar text-5xl text-slate-300 block mb-3"></i>
            <p class="text-slate-600 text-sm">Không tìm thấy đặt bàn nào</p>
      </div>
    </div>
    </template>

    <!-- Reservation Modal -->
    <ReservationModal
      v-if="showCreateModal || showDetailModal"
      :reservation="selectedReservation"
      @close="closeModal"
      @save="handleSave"
    />

    <!-- Table Selection Modal -->
    <Teleport to="body">
      <div
        v-if="showTableSelectModal && selectedReservationForTable"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999] p-4"
        @click.self="closeTableSelectModal"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-2xl mx-auto max-h-[90vh] overflow-y-auto">
          <!-- Modal Header -->
          <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between z-10">
            <h3 class="text-xl font-bold text-slate-900">Xếp bàn cho khách</h3>
            <button @click="closeTableSelectModal" class="text-gray-400 hover:text-gray-600 transition-colors">
              <i class="fas fa-times text-xl"></i>
            </button>
          </div>

          <div class="p-6">
            <!-- Customer Information -->
            <div class="grid grid-cols-2 gap-6 mb-6 pb-6 border-b border-gray-200">
              <div class="space-y-3">
                <div>
                  <p class="text-xs text-slate-500 mb-1">Khách hàng</p>
                  <p class="text-sm font-semibold text-slate-900">{{ selectedReservationForTable.customerName || '-' }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Số khách</p>
                  <p class="text-sm font-semibold text-slate-900">{{ selectedReservationForTable.numberOfGuests || '-' }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Ghi chú</p>
                  <p class="text-sm font-semibold text-slate-900">{{ selectedReservationForTable.notes || selectedReservationForTable.specialRequests || 'Không có' }}</p>
                </div>
              </div>
              <div class="space-y-3">
                <div>
                  <p class="text-xs text-slate-500 mb-1">SĐT</p>
                  <p class="text-sm font-semibold text-slate-900">{{ selectedReservationForTable.customerPhone || '-' }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Thời gian</p>
                  <p class="text-sm font-semibold text-slate-900">
                    {{ formatReservationTime(selectedReservationForTable.reservationTime || selectedReservationForTable.reservationDateTime) }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Table Type Radio Buttons -->
            <div class="flex gap-2 mb-3">
              <label
                :class="[
                  'px-3 py-1.5 rounded-lg text-sm font-medium transition-all cursor-pointer flex items-center gap-2',
                  tableTypeFilter === 'online'
                    ? 'bg-green-500 text-white'
                    : 'bg-gray-100 text-slate-700 hover:bg-gray-200'
                ]"
              >
                <input
                  type="radio"
                  value="online"
                  v-model="tableTypeFilter"
                  class="w-3 h-3 text-slate-900 focus:ring-1 focus:ring-slate-500"
                />
                <span>Bàn Online</span>
              </label>
              <label
                :class="[
                  'px-3 py-1.5 rounded-lg text-sm font-medium transition-all cursor-pointer flex items-center gap-2',
                  tableTypeFilter === 'regular'
                    ? 'bg-green-500 text-white'
                    : 'bg-gray-100 text-slate-700 hover:bg-gray-200'
                ]"
              >
                <input
                  type="radio"
                  value="regular"
                  v-model="tableTypeFilter"
                  class="w-3 h-3 text-slate-900 focus:ring-1 focus:ring-slate-500"
                />
                <span>Bàn Thường</span>
              </label>
            </div>

            <!-- Loading State -->
            <div v-if="loadingTables" class="text-center py-12">
              <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
              <p class="text-sm text-slate-600 mt-3">Đang tải danh sách bàn...</p>
            </div>

            <!-- Tables Grid -->
            <div v-else>
              <div v-if="filteredTablesByType.length === 0" class="text-center py-12 bg-gray-50 rounded-lg border border-gray-200">
                <i class="fas fa-chair text-4xl text-gray-300 mb-3"></i>
                <p class="text-sm text-slate-600">Không có bàn {{ tableTypeFilter === 'online' ? 'online' : 'thường' }} phù hợp</p>
              </div>
              <div v-else class="grid grid-cols-4 gap-3 mb-6">
                <button
                  v-for="table in filteredTablesByType"
                  :key="table.id"
                  @click="selectedTableId = table.id.toString()"
                  :class="[
                    'relative p-4 rounded-lg border-2 transition-all text-left',
                    selectedTableId === table.id.toString()
                      ? 'bg-green-50 border-green-400 shadow-md'
                      : 'bg-white border-gray-200 hover:border-gray-300 hover:shadow-sm'
                  ]"
                >
                  <!-- Selected indicator dot -->
                  <div
                    v-if="selectedTableId === table.id.toString()"
                    class="absolute top-2 right-2 w-3 h-3 bg-green-500 rounded-full"
                  ></div>
                  <div class="pr-6">
                    <p class="font-bold text-slate-900 text-base mb-1">{{ table.tableNumber }}</p>
                    <p class="text-xs text-slate-600">({{ table.capacity }} chỗ)</p>
                  </div>
                </button>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-3 pt-4 border-t border-gray-200">
              <button
                @click="closeTableSelectModal"
                class="flex-1 bg-gray-100 hover:bg-gray-200 text-slate-700 px-4 py-2.5 rounded-lg font-medium transition-colors"
              >
                Huỷ
              </button>
              <button
                @click="assignTable"
                :disabled="!selectedTableId || assigningTable"
                class="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2.5 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="!assigningTable">Xác nhận xếp bàn</span>
                <span v-else class="flex items-center justify-center gap-2">
                  <i class="fas fa-spinner fa-spin"></i>
                  Đang xử lý...
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { reservationService } from '@/services/reservationService'
import { tableService } from '@/services/tableService'
import { useNotificationStore } from '@/stores/notification'
import ReservationModal from '@/components/modals/ReservationModal.vue'

const router = useRouter()
const notification = useNotificationStore()

const loading = ref(false)
const reservations = ref([])
const tables = ref([]) // Store all tables to check status
const searchQuery = ref('')
const filterStatus = ref('')
const filterDateFrom = ref('')
const showCreateModal = ref(false)
const showDetailModal = ref(false)
const selectedReservation = ref(null)
const showTableSelectModal = ref(false)
const selectedReservationForTable = ref(null)
const selectedTableId = ref('')
const availableTables = ref([])
const loadingTables = ref(false)
const assigningTable = ref(false)
const tableTypeFilter = ref('online') // 'online' or 'regular'

const pendingCount = computed(() => {
  if (!Array.isArray(reservations.value)) return 0
  return reservations.value.filter(r => r && r.status === 'PENDING').length
})
const confirmedCount = computed(() => {
  if (!Array.isArray(reservations.value)) return 0
  return reservations.value.filter(r => r && r.status === 'CONFIRMED').length
})
const completedCount = computed(() => {
  if (!Array.isArray(reservations.value)) return 0
  return reservations.value.filter(r => r && r.status === 'COMPLETED').length
})
const cancelledCount = computed(() => {
  if (!Array.isArray(reservations.value)) return 0
  return reservations.value.filter(r => r && r.status === 'CANCELLED').length
})

const filteredReservations = computed(() => {
  if (!Array.isArray(reservations.value)) {
    return []
  }
  
  let result = reservations.value.filter(r => r != null)

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(r => 
      (r.customerName && r.customerName.toLowerCase().includes(query)) ||
      (r.customerPhone && r.customerPhone.includes(query))
    )
  }

  if (filterStatus.value) {
    result = result.filter(r => r.status === filterStatus.value)
  }

  if (filterDateFrom.value) {
    try {
    const fromDate = new Date(filterDateFrom.value)
      result = result.filter(r => {
        const reservationTime = r.reservationTime || r.reservationDateTime
        if (!reservationTime) return false
        const resDate = new Date(reservationTime)
        return !isNaN(resDate.getTime()) && resDate >= fromDate
      })
    } catch (error) {
      console.error('Error filtering by date:', error)
    }
  }

  // Sort by reservationTime (newest first)
  try {
    return result.sort((a, b) => {
      const timeA = a.reservationTime || a.reservationDateTime
      const timeB = b.reservationTime || b.reservationDateTime
      if (!timeA && !timeB) return 0
      if (!timeA) return 1
      if (!timeB) return -1
      const dateA = new Date(timeA)
      const dateB = new Date(timeB)
      if (isNaN(dateA.getTime()) || isNaN(dateB.getTime())) return 0
      return dateB - dateA
    })
  } catch (error) {
    console.error('Error sorting reservations:', error)
    return result
  }
})


onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    // Load both reservations and tables in parallel
    const [reservationsRes, tablesRes] = await Promise.all([
      reservationService.getAll(),
      tableService.getAll()
    ])
    
    // Handle reservations response
    if (reservationsRes && reservationsRes.success && reservationsRes.data) {
      reservations.value = Array.isArray(reservationsRes.data) ? reservationsRes.data : []
    } else if (Array.isArray(reservationsRes?.data)) {
      reservations.value = reservationsRes.data
    } else if (Array.isArray(reservationsRes)) {
      reservations.value = reservationsRes
    } else {
      reservations.value = []
    }
    
    // Handle tables response
    if (tablesRes && tablesRes.success && tablesRes.data) {
      tables.value = Array.isArray(tablesRes.data) ? tablesRes.data : []
    } else if (Array.isArray(tablesRes?.data)) {
      tables.value = tablesRes.data
    } else if (Array.isArray(tablesRes)) {
      tables.value = tablesRes
    } else {
      tables.value = []
    }
  } catch (error) {
    console.error('Error loading data:', error)
    notification.error('Không thể tải dữ liệu: ' + (error.response?.data?.message || error.message))
    reservations.value = []
    tables.value = []
  } finally {
    loading.value = false
  }
}

// Check if reservation is checked in
function isReservationCheckedIn(reservation) {
  if (!reservation) {
    return false
  }
  
  return reservation.status === 'CHECKED_IN'
}

function viewReservationDetail(reservation) {
  selectedReservation.value = { ...reservation }
  showDetailModal.value = true
}

async function confirmReservation(reservation) {
  if (!confirm(`Xác nhận đặt bàn cho "${reservation.customerName}"?`)) return
  
  try {
    await reservationService.confirm(reservation.id)
    notification.success('Đã xác nhận đặt bàn')
    loadData()
  } catch (error) {
    notification.error('Không thể xác nhận đặt bàn')
  }
}

async function cancelReservation(reservation) {
  if (!confirm(`Bạn có chắc muốn hủy đặt bàn cho "${reservation.customerName}"?`)) return
  
    try {
      await reservationService.cancel(reservation.id)
      notification.success('Đã hủy đặt bàn')
    loadData()
    } catch (error) {
      notification.error('Không thể hủy đặt bàn')
    }
  }



function closeModal() {
  showCreateModal.value = false
  showDetailModal.value = false
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
    loadData()
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
  try {
  const date = new Date(datetime)
    if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
  } catch (error) {
    console.error('Error formatting datetime:', error)
    return '-'
  }
}

async function openTableSelectModal(reservation) {
  // Check if reservation is already checked in
  if (isReservationCheckedIn(reservation)) {
    notification.error('Đã check-in, không thể thay đổi bàn')
    return
  }
  
  selectedReservationForTable.value = { ...reservation }
  showTableSelectModal.value = true
  loadingTables.value = true
  availableTables.value = []
  // Initialize selectedTableId based on reservation's tableId or tableNumber
  selectedTableId.value = reservation.tableId ? reservation.tableId.toString() : ''

  try {
    // Get reservation time
    const reservationTime = reservation.reservationTime || reservation.reservationDateTime
    const numberOfGuests = reservation.numberOfGuests || 1

    if (reservationTime) {
      // Format reservation time to ISO string
      let formattedTime = reservationTime
      if (typeof reservationTime === 'string') {
        const date = new Date(reservationTime)
        if (!isNaN(date.getTime())) {
          formattedTime = date.toISOString()
        }
      } else if (reservationTime instanceof Date) {
        formattedTime = reservationTime.toISOString()
      }

      console.log('Loading tables for reservation time:', formattedTime)
      
      // Get available tables for the reservation time using /api/tables/filter endpoint
      const response = await tableService.getAvailableByTime(formattedTime)
      
      console.log('Tables API response:', response)
      
      // Handle different response structures
      let tables = []
      if (response && response.success && response.data) {
        tables = Array.isArray(response.data) ? response.data : []
      } else if (Array.isArray(response.data)) {
        tables = response.data
      } else if (Array.isArray(response)) {
        tables = response
      }
      
      console.log('Parsed tables:', tables)
      
      // Filter by capacity (table capacity should be >= number of guests)
      // Note: We don't filter by status here because API already returns available tables
      availableTables.value = tables.filter(table => 
        table &&
        table.capacity >= numberOfGuests && 
        table.active !== false
      )
      
      console.log('Filtered tables by capacity:', availableTables.value)
      console.log('Tables breakdown:', {
        online: availableTables.value.filter(t => t.type === 'ONLINE').length,
        regular: availableTables.value.filter(t => t.type === 'OFFLINE').length,
        total: availableTables.value.length
      })
    } else {
      // If no reservation time, get all available tables
      const response = await tableService.getAvailable()
      
      let tables = []
      if (response && response.success && response.data) {
        tables = Array.isArray(response.data) ? response.data : []
      } else if (Array.isArray(response.data)) {
        tables = response.data
      } else if (Array.isArray(response)) {
        tables = response
      }
      
      availableTables.value = tables.filter(table => 
        table &&
        table.capacity >= numberOfGuests &&
        table.active !== false
      )
    }

    // If current table is selected, make sure it's in the list
    if (reservation.tableId) {
      const currentTable = availableTables.value.find(t => t.id === reservation.tableId)
      if (!currentTable) {
        // Load the current table info if not in available tables
        try {
          const tableRes = await tableService.getById(reservation.tableId)
          if (tableRes && tableRes.data) {
            availableTables.value.unshift(tableRes.data)
          }
        } catch (error) {
          console.error('Error loading current table:', error)
        }
      }
      
      // Set selectedTableId to ensure it's active (in case it wasn't set correctly before)
      const tableToSelect = availableTables.value.find(t => 
        t.id === reservation.tableId || 
        t.id === parseInt(reservation.tableId) ||
        t.id.toString() === reservation.tableId.toString()
      )
      
      if (tableToSelect) {
        selectedTableId.value = tableToSelect.id.toString()
        
        // Auto-select the correct table type filter based on selected table
        if (tableToSelect.type === 'ONLINE') {
          tableTypeFilter.value = 'online'
        } else {
          tableTypeFilter.value = 'regular'
        }
      }
    } else if (reservation.tableNumber) {
      // If reservation has tableNumber but no tableId, try to find by tableNumber
      const tableByNumber = availableTables.value.find(t => 
        t.tableNumber === reservation.tableNumber
      )
      if (tableByNumber) {
        selectedTableId.value = tableByNumber.id.toString()
        
        // Auto-select the correct table type filter based on selected table
        if (tableByNumber.type === 'ONLINE') {
          tableTypeFilter.value = 'online'
        } else {
          tableTypeFilter.value = 'regular'
        }
      }
    }
  } catch (error) {
    console.error('Error loading tables:', error)
    notification.error('Không thể tải danh sách bàn')
  } finally {
    loadingTables.value = false
  }
}

function closeTableSelectModal() {
  showTableSelectModal.value = false
  selectedReservationForTable.value = null
  selectedTableId.value = ''
  availableTables.value = []
  tableTypeFilter.value = 'online'
}

async function assignTable() {
  if (!selectedTableId.value || !selectedReservationForTable.value) {
    notification.error('Vui lòng chọn bàn')
    return
  }

  assigningTable.value = true
  try {
    const table = availableTables.value.find(t => t.id === parseInt(selectedTableId.value))
    if (!table) {
      notification.error('Bàn không hợp lệ')
      return
    }

    // Use the new API endpoint to assign table
    const response = await reservationService.assignTable(
      selectedReservationForTable.value.id,
      parseInt(selectedTableId.value)
    )

    if (response.success !== false) {
      notification.success(`Đã gán bàn ${table.tableNumber} cho đặt bàn`)
      closeTableSelectModal()
      loadData()
    } else {
      notification.error(response.message || 'Không thể gán bàn')
    }
  } catch (error) {
    console.error('Error assigning table:', error)
    const errorMessage = error.response?.data?.message || error.message || 'Không thể gán bàn'
    notification.error(errorMessage)
  } finally {
    assigningTable.value = false
  }
}

function getTableStatusText(status) {
  const statusMap = {
    'AVAILABLE': 'Trống',
    'OCCUPIED': 'Có khách',
    'RESERVED': 'Đã đặt',
    'MAINTENANCE': 'Đang dọn'
  }
  return statusMap[status] || status
}

const filteredTablesByType = computed(() => {
  if (!Array.isArray(availableTables.value)) return []
  
  return availableTables.value.filter(table => {
    if (!table) return false
    
    // Bàn Online: type === 'ONLINE'
    if (tableTypeFilter.value === 'online') {
      return table.type === 'ONLINE'
    } 
    // Bàn Thường: type === 'OFFLINE'
    else {
      return table.type === 'OFFLINE'
    }
  })
})

function formatReservationTime(dateTime) {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return '-'
    const timeStr = date.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
    const dateStr = date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
    return `${timeStr} ${dateStr}`
  } catch (error) {
    return '-'
  }
}

</script>
