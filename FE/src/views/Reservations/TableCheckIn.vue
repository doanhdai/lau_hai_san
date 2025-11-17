<template>
  <div class="space-y-3 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between mb-3">
      <div>
        <h1 class="text-xl font-bold text-slate-900">Sơ đồ bàn - Check-in</h1>
        <p class="text-slate-600 mt-1 text-xs">Quản lý và check-in bàn</p>
      </div>
      
      <!-- Legend - Chú thích màu sắc -->
      <div class="bg-white border border-gray-200 rounded-lg p-3">
        <p class="text-xs font-semibold text-slate-700 mb-2">Chú thích:</p>
        <div class="flex flex-wrap gap-3">
          <div class="flex items-center gap-2">
            <div class="w-4 h-4 rounded border-2 bg-green-500 border-green-300"></div>
            <span class="text-xs text-slate-600">Trống</span>
          </div>
          <div class="flex items-center gap-2">
            <div class="w-4 h-4 rounded border-2 bg-red-400 border-red-400"></div>
            <span class="text-xs text-slate-600">Có khách</span>
          </div>
          <div class="flex items-center gap-2">
            <div class="w-4 h-4 rounded border-2 bg-amber-500 border-amber-600"></div>
            <span class="text-xs text-slate-600">Đã đặt</span>
          </div>
          <div class="flex items-center gap-2">
            <div class="w-4 h-4 rounded border-2 bg-gray-600 border-gray-700"></div>
            <span class="text-xs text-slate-600">Đang dọn</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Content -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-3">
      <!-- Table Map (Left - 2 columns) -->
      <div class="lg:col-span-2">
        <div class="bg-white border border-gray-200 rounded-lg p-3">
          <div
            ref="mapContainer"
            class="relative bg-gray-50 rounded-lg border-2 border-dashed border-gray-300"
            style="height: calc(100vh - 350px); min-height: 600px; background-image: repeating-linear-gradient(0deg, transparent, transparent 49px, #e2e8f0 49px, #e2e8f0 50px), repeating-linear-gradient(90deg, transparent, transparent 49px, #e2e8f0 49px, #e2e8f0 50px); background-size: 50px 50px;"
          >
            <!-- Tables with reservation info -->
            <div
              v-for="table in tablesWithReservations"
              :key="table.id"
              :style="{
                position: 'absolute',
                left: table.positionX + 'px',
                top: table.positionY + 'px',
                zIndex: selectedTable?.id === table.id ? 100 : 1
              }"
              class="cursor-pointer transition-all duration-200 hover:shadow-xl"
              :class="{ 'ring-2 ring-blue-500 ring-offset-2': selectedTable?.id === table.id }"
              @click="selectTable(table)"
            >
              <!-- Table Block -->
              <div 
                class="relative rounded-lg shadow-md border-2 min-w-[110px] min-h-[90px] flex flex-col items-center justify-center p-3"
                :class="getTableBlockClass(table)"
              >
                <!-- Table Label (Top) -->
                <div class="absolute -top-3 left-1/2 transform -translate-x-1/2 whitespace-nowrap">
                  <span class="bg-white px-2 py-0.5 rounded border-2 font-bold text-sm inline-block" :class="getTableLabelClass(table)">
                    {{ table.tableNumber }}
                  </span>
                </div>

                <!-- Check-in status (OCCUPIED) -->
                <div v-if="table.status === 'OCCUPIED'" class="text-center mt-1">
                  <p class="text-xs font-bold text-white">
                    Có khách
                  </p>
                  <p class="text-xs text-white/90 mt-0.5">
                    ({{ table.capacity }} chỗ)
                  </p>
                </div>
                <!-- Bàn trống hoặc có reservation - chỉ hiển thị sức chứa -->
                <div v-else class="text-center mt-1">
                  <p class="text-xs text-white/90">
                    ({{ table.capacity }} chỗ)
                  </p>
                </div>
              </div>
            </div>

            <!-- Empty state -->
            <div v-if="tablesWithReservations.length === 0" class="absolute inset-0 flex items-center justify-center">
              <div class="text-center text-gray-400">
                <i class="fas fa-chair text-6xl mb-3 opacity-50"></i>
                <p class="text-lg font-medium">Chưa có bàn nào</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Khách chờ check-in (Right - 1 column) -->
      <div class="lg:col-span-1">
        <div class="bg-white border border-gray-200 rounded-lg">
          <div class="p-3 border-b border-gray-200 bg-slate-50">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-base font-bold text-slate-900">
                Khách chờ check-in
              </h3>
            </div>
            <div class="space-y-2">
              <div>
                <label class="block text-xs font-medium text-slate-700 mb-1.5">Lọc theo ngày đến</label>
                <input 
                  v-model="filterDate" 
                  type="date" 
                  :min="minDate"
                  class="w-full px-3 py-1.5 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition" 
                />
              </div>
              <div>
                <label class="block text-xs font-medium text-slate-700 mb-1.5">Tìm kiếm theo tên</label>
                <div class="relative">
                  <input 
                    v-model="searchQuery" 
                    type="text" 
                    placeholder="Nhập tên khách hàng..."
                    class="w-full px-3 py-1.5 pl-9 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition" 
                  />
                  <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs"></i>
                </div>
              </div>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="p-6 text-center">
            <div class="inline-block w-6 h-6 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
          </div>

          <!-- Reservations List -->
          <div v-else class="p-3 space-y-2 max-h-[calc(100vh-350px)] overflow-y-auto">
            <div
              v-for="reservation in upcomingReservations"
              :key="reservation.id"
              class="bg-white border border-gray-200 rounded-lg p-3 hover:shadow-sm transition-all relative group"
            >
              <!-- Customer Name -->
              <div class="flex items-start justify-between mb-1.5">
                <h4 class="text-sm font-bold text-slate-900">{{ reservation.customerName }}</h4>
                <span v-if="reservation.tableNumber" class="text-xs text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded">
                  Bàn {{ reservation.tableNumber }}
                </span>
              </div>

              <!-- Reservation Details -->
              <div class="text-xs text-slate-600 mb-2">
                <p>
                  {{ formatReservationTime(reservation.reservationTime || reservation.reservationDateTime) }} 
                  ({{ reservation.numberOfGuests }} khách)
                </p>
                <!-- Chú thích check-in (chỉ hiện cho ngày hiện tại) -->
                <template v-if="isToday">
                  <p 
                    v-if="!hasTable(reservation)"
                    class="text-xs text-gray-600 mt-1"
                  >
                    Chưa chọn bàn
                  </p>
                  <p 
                    v-else-if="isTableCheckedIn(reservation)"
                    class="text-xs text-green-700 mt-1 font-semibold"
                  >
                    Đã checkin
                  </p>
                  <p 
                    v-else-if="isReservationOverdue(reservation)"
                    class="text-xs text-red-600 mt-1 font-medium"
                  >
                    Khách không tới
                  </p>
                  <p 
                    v-else
                    class="text-xs text-green-600 mt-1 font-medium"
                  >
                    Có thể checkin
                  </p>
                </template>
              </div>

              <!-- Countdown Timer (chỉ hiện nếu chưa check-in, chưa đến thời gian VÀ là ngày hiện tại) -->
              <div v-if="isToday && !isTableCheckedIn(reservation) && !isReservationTimeReached(reservation)" class="flex items-center justify-between">
                <span class="text-xs text-slate-500">Khách hàng sẽ đến sau</span>
                <div class="bg-orange-100 border border-orange-200 rounded px-2 py-1">
                  <span class="text-base font-bold text-orange-600 font-mono">
                    {{ getCountdown(reservation.reservationTime || reservation.reservationDateTime) }}
                  </span>
                </div>
              </div>

              <!-- Check-in Button (chỉ hiện cho ngày hiện tại: hover và chưa check-in và đã có bàn và là reservation gần nhất của table và chưa quá 30 phút) -->
              <div 
                v-if="isToday && hasTable(reservation) && !isTableCheckedIn(reservation) && !isReservationOverdue(reservation) && isNearestReservationForTable(reservation)"
                class="absolute inset-0 bg-white/95 rounded-lg flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <button
                  @click="handleCheckIn(reservation)"
                  class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors"
                >
                  <i class="fas fa-check-circle"></i>
                  <span>Check-in</span>
                </button>
              </div>
            </div>

            <!-- Empty State -->
            <div v-if="upcomingReservations.length === 0" class="text-center py-8">
              <i class="fas fa-calendar-check text-3xl text-gray-300 mb-2"></i>
              <p class="text-xs text-slate-600">Không có khách chờ check-in</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table Management Modal -->
    <Teleport to="body">
      <div
        v-if="showTableModal && tableModalTable"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999] p-4"
        @click.self="closeTableModal"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-md mx-auto" @click.stop>
          <!-- Modal Header -->
          <div class="bg-slate-900 text-white px-6 py-4 rounded-t-lg flex items-center justify-between">
            <h3 class="text-lg font-bold">Quản lý bàn {{ tableModalTable.tableNumber }}</h3>
            <button @click="closeTableModal" class="text-white/80 hover:text-white transition-colors">
              <i class="fas fa-times text-xl"></i>
            </button>
          </div>

          <div class="p-6 space-y-6">
            <!-- Thông tin bàn -->
            <div>
              <h4 class="text-sm font-semibold text-slate-900 mb-3">Thông tin bàn</h4>
              <div class="bg-slate-50 rounded-lg p-4 space-y-2">
                <div class="flex items-center justify-between">
                  <span class="text-sm text-slate-600">Số bàn:</span>
                  <span class="text-sm font-semibold text-slate-900">{{ tableModalTable.tableNumber }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-slate-600">Sức chứa:</span>
                  <span class="text-sm font-semibold text-slate-900">{{ tableModalTable.capacity }} người</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-slate-600">Trạng thái hiện tại:</span>
                  <span :class="getTableStatusBadgeClass(tableModalTable.status)" class="text-xs font-semibold px-2 py-1 rounded-full">
                    {{ getTableStatusLabel(tableModalTable.status) }}
                  </span>
                </div>
                <div v-if="tableModalTable.location" class="flex items-center justify-between">
                  <span class="text-sm text-slate-600">Vị trí:</span>
                  <span class="text-sm font-semibold text-slate-900">{{ tableModalTable.location }}</span>
                </div>
              </div>
            </div>

            <!-- Nút điều hướng đến Order (nếu bàn đã check-in) -->
            <div v-if="tableModalTable.status === 'OCCUPIED'" class="border-t border-gray-200 pt-6 space-y-3">
              <button
                @click="goToOrderPage"
                type="button"
                class="w-full bg-slate-900 hover:bg-slate-800 text-white px-4 py-2.5 rounded-lg font-medium transition-colors flex items-center justify-center gap-2 cursor-pointer"
                style="position: relative; z-index: 10;"
              >
                <i class="fas fa-plus"></i>
                <span>Thêm món</span>
              </button>
              <button
                @click.stop="viewOrderList"
                class="w-full bg-blue-500 hover:bg-blue-600 text-white px-4 py-2.5 rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
              >
                <i class="fas fa-utensils"></i>
                <span>Xem đơn hàng</span>
              </button>
            </div>

            <!-- Chỉnh trạng thái bàn -->
            <div class="border-t border-gray-200 pt-6">
              <h4 class="text-sm font-semibold text-slate-900 mb-3">Chỉnh trạng thái bàn</h4>
              <div class="space-y-3">
                <div class="flex gap-2">
                  <label
                    :class="[
                      'flex-1 px-4 py-2 rounded-lg text-sm font-medium cursor-pointer transition-all flex items-center justify-center gap-2',
                      tableStatus === 'AVAILABLE'
                        ? 'bg-green-500 text-white'
                        : 'bg-gray-100 text-slate-700 hover:bg-gray-200'
                    ]"
                  >
                    <input
                      type="radio"
                      value="AVAILABLE"
                      v-model="tableStatus"
                      class="w-3 h-3 text-green-600 focus:ring-1 focus:ring-green-500"
                    />
                    <span>Trống</span>
                  </label>
                  <label
                    :class="[
                      'flex-1 px-4 py-2 rounded-lg text-sm font-medium cursor-pointer transition-all flex items-center justify-center gap-2',
                      tableStatus === 'OCCUPIED'
                        ? 'bg-red-500 text-white'
                        : 'bg-gray-100 text-slate-700 hover:bg-gray-200'
                    ]"
                  >
                    <input
                      type="radio"
                      value="OCCUPIED"
                      v-model="tableStatus"
                      class="w-3 h-3 text-red-600 focus:ring-1 focus:ring-red-500"
                    />
                    <span>Check-in</span>
                  </label>
                  <label
                    :class="[
                      'flex-1 px-4 py-2 rounded-lg text-sm font-medium cursor-pointer transition-all flex items-center justify-center gap-2',
                      tableStatus === 'CLEANING'
                        ? 'bg-gray-500 text-white'
                        : 'bg-gray-100 text-slate-700 hover:bg-gray-200'
                    ]"
                  >
                    <input
                      type="radio"
                      value="CLEANING"
                      v-model="tableStatus"
                      class="w-3 h-3 text-gray-600 focus:ring-1 focus:ring-gray-500"
                    />
                    <span>Đang dọn</span>
                  </label>
                </div>
                <button
                  @click="handleUpdateStatus"
                  :disabled="updatingStatus || tableStatus === tableModalTable.status"
                  class="w-full bg-orange-100 hover:bg-orange-200 text-orange-700 px-4 py-2.5 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span v-if="!updatingStatus">Cập nhật trạng thái</span>
                  <span v-else class="flex items-center justify-center gap-2">
                    <i class="fas fa-spinner fa-spin"></i>
                    Đang cập nhật...
                  </span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Reservation Detail Modal -->
    <Teleport to="body">
      <div
        v-if="showDetailModal && selectedReservation"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]"
        @click.self="showDetailModal = false"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
          <div class="p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-xl font-bold text-slate-900">Chi tiết đặt bàn</h3>
              <button @click="showDetailModal = false" class="text-gray-400 hover:text-gray-600">
                <i class="fas fa-times"></i>
              </button>
            </div>
            <div class="space-y-4">
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <p class="text-xs text-slate-500 mb-1">Khách hàng</p>
                  <p class="text-sm font-semibold text-slate-900">{{ selectedReservation.customerName }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Số điện thoại</p>
                  <p class="text-sm text-slate-700">{{ selectedReservation.customerPhone }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Ngày giờ</p>
                  <p class="text-sm text-slate-700">{{ formatDateTime(selectedReservation.reservationTime || selectedReservation.reservationDateTime) }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Số người</p>
                  <p class="text-sm text-slate-700">{{ selectedReservation.numberOfGuests }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Bàn</p>
                  <p class="text-sm text-slate-700">{{ selectedReservation.tableNumber || 'Chưa chọn' }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Trạng thái</p>
                  <span :class="getStatusBadgeClass(selectedReservation.status)">
                    {{ getStatusLabel(selectedReservation.status) }}
                  </span>
                </div>
              </div>
              <div v-if="selectedReservation.notes">
                <p class="text-xs text-slate-500 mb-1">Ghi chú</p>
                <p class="text-sm text-slate-700">{{ selectedReservation.notes }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Order Detail Modal -->
    <TableOrderDetailModal
      :show="showOrderModal"
      :reservation-id="orderModalReservationId"
      :table-id="orderModalTableId"
      :reservations="reservations"
      :tables-with-reservations="tablesWithReservations"
      @close="closeOrderModal"
      @order-updated="handleOrderUpdated"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { reservationService } from '@/services/reservationService'
import { tableService } from '@/services/tableService'
import { useNotificationStore } from '@/stores/notification'
import TableOrderDetailModal from '@/components/modals/TableOrderDetailModal.vue'

const router = useRouter()
const notification = useNotificationStore()

const loading = ref(false)
const reservations = ref([])
const tables = ref([])
const selectedTable = ref(null)
const selectedReservation = ref(null)
const showDetailModal = ref(false)
const showTableModal = ref(false)
const showOrderModal = ref(false)
const tableModalTable = ref(null)
const tableStatus = ref('AVAILABLE')
const updatingStatus = ref(false)
const mapContainer = ref(null)
const currentTime = ref(new Date())
const filterDate = ref('')
const orderModalReservationId = ref(null)
const orderModalTableId = ref(null)
const tableModalReservationId = ref(null) // Lưu reservationId khi mở modal bàn
const searchQuery = ref('') // Search query for customer name

// Min date for filter (today)
const minDate = computed(() => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

// Check if filter date is today
const isToday = computed(() => {
  if (!filterDate.value) return true // Default is today
  const today = new Date()
  const selectedDate = new Date(filterDate.value + 'T00:00:00')
  
  return today.getFullYear() === selectedDate.getFullYear() &&
         today.getMonth() === selectedDate.getMonth() &&
         today.getDate() === selectedDate.getDate()
})

// Merge tables with reservations
const tablesWithReservations = computed(() => {
  if (!Array.isArray(tables.value) || tables.value.length === 0) {
    return []
  }
  
  if (!Array.isArray(reservations.value)) {
    return tables.value.map(table => ({
      ...table,
      reservation: null
    }))
  }
  
  const now = new Date()
  
  return tables.value.map(table => {
    if (!table || !table.id) {
      return null
    }
    
    // Find active reservation for this table that is not in the past
    const reservation = reservations.value.find(r => {
      if (!r || r.tableId !== table.id) {
        return false
      }
      
      // Only show PENDING or CONFIRMED reservations
      if (r.status !== 'PENDING' && r.status !== 'CONFIRMED') {
        return false
      }
      
      // Check if reservation time is in the future or today
      const reservationTime = r.reservationTime || r.reservationDateTime
      if (!reservationTime) {
        return false
      }
      
      try {
        const reservationDate = new Date(reservationTime)
        // Only show reservations that are today or in the future
        return reservationDate >= now
      } catch (error) {
        console.error('Error parsing reservation time:', error)
        return false
      }
    })
    
    return {
      ...table,
      reservation: reservation || null
    }
  }).filter(Boolean) // Remove any null entries
})

// Get upcoming reservations (future reservations)
const upcomingReservations = computed(() => {
  if (!Array.isArray(reservations.value)) {
    return []
  }
  
  const now = currentTime.value
  const selectedDate = filterDate.value ? new Date(filterDate.value + 'T00:00:00') : null
  const selectedDateEnd = filterDate.value ? new Date(filterDate.value + 'T23:59:59') : null
  const query = (searchQuery.value || '').trim().toLowerCase()
  
  return reservations.value
    .filter(r => {
      if (!r) return false
      
      // Only show CONFIRMED reservations
      if (r.status !== 'CONFIRMED') {
        return false
      }
      
      const reservationTime = r.reservationTime || r.reservationDateTime
      if (!reservationTime) return false
      
      try {
        const reservationDate = new Date(reservationTime)
        
        // Filter by selected date if provided
        if (selectedDate && selectedDateEnd) {
          const isInSelectedDate = reservationDate >= selectedDate && reservationDate <= selectedDateEnd
          if (!isInSelectedDate) return false
        } else {
          // Otherwise, only show future reservations
          if (reservationDate < now) return false
        }
        
        // Filter by search query (customer name)
        if (query) {
          const customerName = (r.customerName || '').toLowerCase()
          if (!customerName.includes(query)) {
            return false
          }
        }
        
        return true
      } catch (error) {
        return false
      }
    })
    .sort((a, b) => {
      const now = currentTime.value
      const timeA = new Date(a.reservationTime || a.reservationDateTime)
      const timeB = new Date(b.reservationTime || b.reservationDateTime)
      
      // Kiểm tra xem reservation có thể check-in ngay không
      const canCheckInA = (() => {
        if (!a) return false
        const tableIdA = a.tableId || a.table?.id
        const hasTableA = tableIdA != null && tableIdA !== undefined
        const isCheckedInA = a.status === 'CHECKED_IN'
        const isTimeReachedA = now >= timeA
        const isOverdueA = (() => {
          const overdueTime = new Date(timeA.getTime() + 30 * 60 * 1000) // +30 phút
          return now > overdueTime
        })()
        return hasTableA && !isCheckedInA && isTimeReachedA && !isOverdueA
      })()
      
      const canCheckInB = (() => {
        if (!b) return false
        const tableIdB = b.tableId || b.table?.id
        const hasTableB = tableIdB != null && tableIdB !== undefined
        const isCheckedInB = b.status === 'CHECKED_IN'
        const isTimeReachedB = now >= timeB
        const isOverdueB = (() => {
          const overdueTime = new Date(timeB.getTime() + 30 * 60 * 1000) // +30 phút
          return now > overdueTime
        })()
        return hasTableB && !isCheckedInB && isTimeReachedB && !isOverdueB
      })()
      
      // Ưu tiên các reservation có thể check-in ngay lên đầu
      if (canCheckInA && !canCheckInB) return -1
      if (!canCheckInA && canCheckInB) return 1
      
      // Nếu cả hai đều có thể check-in hoặc không thể check-in, sắp xếp theo thời gian gần nhất
      return timeA - timeB
    })
})

onMounted(() => {
  // Set default filter date to today
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  filterDate.value = `${year}-${month}-${day}`
  
  loadData()
  
  // Update current time every second for countdown
  const timer = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
  
  // Cleanup on unmount
  onUnmounted(() => {
    clearInterval(timer)
  })
})

async function loadData() {
  loading.value = true
  try {
    const [reservationsRes, tablesRes] = await Promise.all([
      reservationService.getAll(),
      tableService.getAll()
    ])
    
    // Handle reservations response - ensure it's always an array
    if (reservationsRes && reservationsRes.success && reservationsRes.data) {
      reservations.value = Array.isArray(reservationsRes.data) ? reservationsRes.data : []
    } else if (Array.isArray(reservationsRes?.data)) {
      reservations.value = reservationsRes.data
    } else {
      reservations.value = []
    }
    
    // Handle tables response - ensure it's always an array
    let tablesData = []
    if (tablesRes && tablesRes.success && tablesRes.data) {
      tablesData = Array.isArray(tablesRes.data) ? tablesRes.data : []
    } else if (Array.isArray(tablesRes?.data)) {
      tablesData = tablesRes.data
    } else if (tablesRes?.data?.data && Array.isArray(tablesRes.data.data)) {
      tablesData = tablesRes.data.data
    } else {
      tablesData = []
    }
    
    // Initialize table positions if not set
    tables.value = tablesData.map((table, index) => {
      if (!table || !table.id) {
        return null
      }
      
      let positionX = table.positionX
      let positionY = table.positionY
      
      if (positionX === null || positionX === undefined || positionY === null || positionY === undefined) {
        const cols = 5
        const row = Math.floor(index / cols)
        const col = index % cols
        const spacing = 140
        positionX = 50 + (col * spacing)
        positionY = 50 + (row * spacing)
      }
      
      return {
        ...table,
        positionX,
        positionY,
        type: table.type || 'OFFLINE'
      }
    }).filter(Boolean) // Remove any null entries
  } catch (error) {
    console.error('Error loading data in TableCheckIn:', error)
    notification.error('Không thể tải dữ liệu: ' + (error.response?.data?.message || error.message))
    // Set empty arrays on error to prevent blank screen
    reservations.value = []
    tables.value = []
  } finally {
    loading.value = false
  }
}

function selectTable(table) {
  // Open table management modal when clicking on table
  openTableModal(table)
}

/**
 * Tìm reservation_id theo table_id và status = CHECKED_IN
 * @param {number} tableId - ID của bàn
 * @returns {number|null} - reservation_id nếu tìm thấy, null nếu không
 */
function findCheckedInReservationIdByTableId(tableId) {
  if (!tableId || !Array.isArray(reservations.value)) {
    return null
  }
  
  const reservation = reservations.value.find(r => 
    r && 
    r.tableId === tableId && 
    r.status === 'CHECKED_IN'
  )
  
  return reservation?.id || null
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

function createOrderForTable(table) {
  if (!table.reservation) {
    notification.error('Bàn chưa có đặt bàn')
    return
  }
  createOrder(table.reservation)
}

function createOrder(reservation) {
  // Navigate to create order page with reservation pre-selected
  router.push({
    path: '/admin/orders/create',
    query: { reservationId: reservation.id }
  })
}

function goToOrderPage() {
  if (!tableModalTable.value) {
    notification.error('Không tìm thấy thông tin bàn')
    return
  }
  
  // Tìm reservation_id theo table_id và status = CHECKED_IN
  const reservationId = findCheckedInReservationIdByTableId(tableModalTable.value.id)
  
  if (!reservationId) {
    notification.error('Bàn chưa có đặt bàn đã check-in')
    return
  }
  
  // Navigate to create order page with reservationId
  router.push({
    path: '/admin/orders/create',
    query: { reservationId: reservationId }
  })
  
  // Close modal after navigation
  closeTableModal()
}

async function viewOrderList() {
  if (!tableModalTable.value) {
    notification.error('Không tìm thấy thông tin bàn')
    return
  }
  
  // Tìm reservation_id theo table_id và status = CHECKED_IN
  const reservationId = findCheckedInReservationIdByTableId(tableModalTable.value.id)
  
  if (!reservationId) {
    notification.error('Bàn chưa có đặt bàn đã check-in')
    return
  }
  
  // Set props for modal
  orderModalReservationId.value = reservationId
  orderModalTableId.value = tableModalTable.value.id || null
  
  // Close table modal and open order modal
  closeTableModal()
  showOrderModal.value = true
}

function closeOrderModal() {
  showOrderModal.value = false
  orderModalReservationId.value = null
  orderModalTableId.value = null
}

function handleOrderUpdated() {
  // Reload data when order is updated
  loadData()
}

function openTableModal(table) {
  tableModalTable.value = { ...table }
  tableStatus.value = table.status || 'AVAILABLE'
  
  // Lưu reservationId: ưu tiên từ table.reservation, nếu không có thì tìm từ tablesWithReservations
  let reservationId = table.reservation?.id || null
  
  if (!reservationId && table.id) {
    const tableWithReservation = tablesWithReservations.value.find(t => 
      t && t.id === table.id && t.reservation
    )
    reservationId = tableWithReservation?.reservation?.id || null
  }
  
  tableModalReservationId.value = reservationId
  showTableModal.value = true
}

function closeTableModal() {
  showTableModal.value = false
  tableModalTable.value = null
  tableStatus.value = 'AVAILABLE'
  tableModalReservationId.value = null
}

async function handleUpdateStatus() {
  if (!tableModalTable.value) {
    notification.error('Không tìm thấy thông tin bàn')
    return
  }

  if (tableStatus.value === tableModalTable.value.status) {
    notification.info('Trạng thái bàn không thay đổi')
    return
  }

  updatingStatus.value = true
  try {
    await tableService.updateStatus(tableModalTable.value.id, tableStatus.value)
    
    const statusLabel = getTableStatusLabel(tableStatus.value)
    notification.success(`Đã cập nhật trạng thái bàn thành "${statusLabel}"`)
    closeTableModal()
    loadData()
  } catch (error) {
    console.error('Update status error:', error)
    notification.error('Không thể cập nhật trạng thái: ' + (error.response?.data?.message || error.message))
  } finally {
    updatingStatus.value = false
  }
}

function getTableStatusLabel(status) {
  const labels = {
    'AVAILABLE': 'Trống',
    'RESERVED': 'Đã đặt',
    'OCCUPIED': 'Check-in',
    'CLEANING': 'Đang dọn',
    'ONLINE': 'Online'
  }
  return labels[status] || status
}

function getTableStatusBadgeClass(status) {
  const classes = {
    'AVAILABLE': 'bg-green-100 text-green-800',
    'RESERVED': 'bg-amber-100 text-amber-800',
    'OCCUPIED': 'bg-red-100 text-red-800',
    'CLEANING': 'bg-gray-100 text-gray-800',
    'ONLINE': 'bg-blue-100 text-blue-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

async function checkInTable(table) {
  if (!table.reservation) {
    notification.error('Bàn chưa có đặt bàn')
    return
  }
  
  if (table.reservation.status !== 'CONFIRMED') {
    notification.error('Chỉ có thể check-in bàn đã được xác nhận')
    return
  }
  
  if (!confirm(`Check-in bàn ${table.tableNumber} cho khách "${table.reservation.customerName}"?`)) return
  
  try {
    // Update table status to OCCUPIED
    await tableService.updateStatus(table.id, 'OCCUPIED')
    
    notification.success(`Đã check-in bàn ${table.tableNumber}`)
    loadData()
  } catch (error) {
    console.error('Check-in error:', error)
    notification.error('Không thể check-in bàn')
  }
}

/**
 * Table styling based on table status
 * Mỗi status có một màu riêng để dễ phân biệt:
 * - AVAILABLE (Trống): Xanh lá - Bàn trống, chưa có khách
 * - OCCUPIED (Có khách/Check-in): Đỏ nhạt - Bàn đã check-in, đang có khách
 * - RESERVED (Đã đặt): Vàng - Bàn đã được đặt nhưng chưa check-in
 * - CLEANING (Đang dọn): Xám đậm - Bàn đang được dọn dẹp
 * - ONLINE (Online): Xanh dương - Bàn cho phép đặt online
 */
function getTableBlockClass(table) {
  // Kiểm tra status của bàn trước
  const tableStatus = table.status
  
  // AVAILABLE: Trống - Màu xanh lá
  if (tableStatus === 'AVAILABLE') {
    return 'bg-green-500 border-green-300'
  }
  
  // OCCUPIED: Có khách/Check-in - Màu đỏ nhạt
  if (tableStatus === 'OCCUPIED') {
    return 'bg-red-400 border-red-400'
  }
  
  // RESERVED: Đã đặt - Màu vàng
  if (tableStatus === 'RESERVED') {
    return 'bg-amber-500 border-amber-600'
  }
  
  // CLEANING: Đang dọn - Màu xám đậm
  if (tableStatus === 'CLEANING') {
    return 'bg-gray-600 border-gray-700'
  }
  
  // Nếu bàn có reservation nhưng chưa có status rõ ràng, dựa vào reservation status
  if (table.reservation) {
    const reservationStatus = table.reservation.status
    const classes = {
      PENDING: 'bg-amber-500 border-amber-600',    // Chờ xác nhận - Vàng
      CONFIRMED: 'bg-blue-500 border-blue-600',    // Đã xác nhận - Xanh dương
      COMPLETED: 'bg-green-500 border-green-600',   // Hoàn thành - Xanh lá
      CANCELLED: 'bg-gray-400 border-gray-500'      // Đã hủy - Xám
    }
    return classes[reservationStatus] || 'bg-green-700 border-green-300'
  }
  
  // Mặc định: Bàn trống - Xanh lá nhạt
  return 'bg-green-700 border-green-300'
}

/**
 * Table label styling based on table status
 * Màu label phải khớp với màu block của bàn
 */
function getTableLabelClass(table) {
  // Kiểm tra status của bàn trước
  const tableStatus = table.status
  
  // AVAILABLE: Trống - Xanh lá
  if (tableStatus === 'AVAILABLE') {
    return 'border-green-400 text-green-700'
  }
  
  // OCCUPIED: Có khách/Check-in - Đỏ nhạt
  if (tableStatus === 'OCCUPIED') {
    return 'border-red-400 text-red-700'
  }
  
  // RESERVED: Đã đặt - Vàng
  if (tableStatus === 'RESERVED') {
    return 'border-amber-600 text-amber-700'
  }
  
  // CLEANING: Đang dọn - Xám đậm
  if (tableStatus === 'CLEANING') {
    return 'border-gray-700 text-gray-800'
  }
  
  // Nếu bàn có reservation nhưng chưa có status rõ ràng, dựa vào reservation status
  if (table.reservation) {
    const reservationStatus = table.reservation.status
    const classes = {
      PENDING: 'border-amber-600 text-amber-700',    // Chờ xác nhận - Vàng
      CONFIRMED: 'border-blue-600 text-blue-700',    // Đã xác nhận - Xanh dương
      COMPLETED: 'border-green-600 text-green-700',  // Hoàn thành - Xanh lá
      CANCELLED: 'border-gray-500 text-gray-700'     // Đã hủy - Xám
    }
    return classes[reservationStatus] || 'border-green-400 text-green-700'
  }
  
  // Mặc định: Xanh lá
  return 'border-green-400 text-green-700'
}

function getReservationBadgeColor(status) {
  const colors = {
    PENDING: 'text-amber-700',
    CONFIRMED: 'text-blue-700',
    COMPLETED: 'text-green-700',
    CANCELLED: 'text-gray-700'
  }
  return colors[status] || 'text-gray-700'
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

function formatTime(datetime) {
  if (!datetime) return ''
  try {
    const date = new Date(datetime)
    if (isNaN(date.getTime())) return ''
    return date.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('Error formatting time:', error)
    return ''
  }
}

function formatReservationTime(datetime) {
  if (!datetime) return ''
  try {
    const date = new Date(datetime)
    if (isNaN(date.getTime())) return ''
    const timeStr = date.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit'
    })
    const dateStr = date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
    return `${timeStr} ${dateStr}`
  } catch (error) {
    console.error('Error formatting reservation time:', error)
    return ''
  }
}

function getCountdown(reservationTime) {
  if (!reservationTime) return '00:00:00'
  
  try {
    const target = new Date(reservationTime)
    const now = currentTime.value
    const diff = target - now
    
    if (diff <= 0) {
      return '00:00:00'
    }
    
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)
    
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  } catch (error) {
    console.error('Error calculating countdown:', error)
    return '00:00:00'
  }
}

function getCountdownHours(reservationTime) {
  if (!reservationTime) return 0
  
  try {
    const target = new Date(reservationTime)
    const now = currentTime.value
    const diff = target - now
    
    if (diff <= 0) {
      return 0
    }
    
    // Trả về số giờ (có thể là số thập phân)
    return diff / (1000 * 60 * 60)
  } catch (error) {
    console.error('Error calculating countdown hours:', error)
    return 0
  }
}

function hasTable(reservation) {
  if (!reservation) return false
  
  // Lấy tableId từ reservation
  const tableId = reservation.tableId || reservation.table?.id
  return tableId != null && tableId !== undefined
}

function isTableCheckedIn(reservation) {
  if (!reservation) return false
  
  // Kiểm tra status của reservation
  return reservation.status === 'CHECKED_IN'
}

/**
 * Kiểm tra xem thời gian reservation đã đến chưa
 * @param {Object} reservation - Reservation object
 * @returns {boolean} - true nếu thời gian reservation đã đến hoặc đã qua
 */
function isReservationTimeReached(reservation) {
  if (!reservation) return false
  
  const reservationTime = reservation.reservationTime || reservation.reservationDateTime
  if (!reservationTime) return false
  
  try {
    const targetTime = new Date(reservationTime)
    const now = currentTime.value
    return now >= targetTime
  } catch (error) {
    console.error('Error checking reservation time:', error)
    return false
  }
}

/**
 * Kiểm tra xem reservation đã quá 30 phút so với thời gian hẹn chưa
 * @param {Object} reservation - Reservation object
 * @returns {boolean} - true nếu đã quá 30 phút
 */
function isReservationOverdue(reservation) {
  if (!reservation) return false
  
  const reservationTime = reservation.reservationTime || reservation.reservationDateTime
  if (!reservationTime) return false
  
  try {
    const targetTime = new Date(reservationTime)
    const now = currentTime.value
    const diffMinutes = (now - targetTime) / (1000 * 60) // Chênh lệch tính bằng phút
    
    // Quá 30 phút
    return diffMinutes > 30
  } catch (error) {
    console.error('Error checking reservation overdue:', error)
    return false
  }
}

function isNearestReservationForTable(reservation) {
  if (!reservation) return false
  
  // Lấy tableId từ reservation
  const tableId = reservation.tableId || reservation.table?.id
  if (!tableId) return true // Nếu không có tableId, cho phép hiển thị
  
  // Tìm tất cả reservations cùng tableId
  const sameTableReservations = upcomingReservations.value.filter(r => {
    const rTableId = r.tableId || r.table?.id
    return rTableId === tableId && r.id !== reservation.id
  })
  
  // Nếu không có reservation nào khác cùng table, cho phép hiển thị
  if (sameTableReservations.length === 0) return true
  
  // Lấy reservationTime của reservation hiện tại
  const currentReservationTime = new Date(reservation.reservationTime || reservation.reservationDateTime)
  
  // Kiểm tra xem có reservation nào có thời gian gần hơn không
  for (const otherReservation of sameTableReservations) {
    const otherReservationTime = new Date(otherReservation.reservationTime || otherReservation.reservationDateTime)
    
    // Nếu có reservation khác có thời gian gần hơn (nhỏ hơn), thì reservation này không phải là gần nhất
    if (otherReservationTime < currentReservationTime) {
      return false
    }
  }
  
  // Nếu không có reservation nào gần hơn, đây là reservation gần nhất
  return true
}

async function handleCheckIn(reservation) {
  
  if (!reservation) {
    notification.error('Không tìm thấy thông tin đặt bàn')
    return
  }

  // Lấy reservation_id trực tiếp từ reservation object
  const reservationId = reservation.id
  console.log('reservationId', reservationId)
  if (!reservationId) {
    notification.error('Không tìm thấy ID đặt bàn')
    return
  }

  // Lấy tableId từ reservation
  const tableId = reservation.tableId || reservation.table?.id
  
  if (!tableId) {
    notification.error('Đặt bàn này chưa có bàn được gán')
    return
  }

  // Kiểm tra status của bàn - chỉ cho phép check-in nếu bàn không có status OCCUPIED
  const table = tables.value.find(t => t && t.id === tableId)
  if (table && table.status === 'OCCUPIED') {
    notification.error('Bàn này đang có khách, không thể check-in thêm')
    return
  }

  if (!confirm(`Check-in bàn ${reservation.tableNumber || ''} cho khách "${reservation.customerName}"?`)) {
    return
  }

  try {
    // 1. Update reservation status thành CHECKED_IN và confirmed_by
    await reservationService.checkin(reservationId)
    
    // 2. Update table status thành OCCUPIED
    await tableService.updateStatus(tableId, 'OCCUPIED')
    
    notification.success('Đã check-in thành công')
    loadData()
  } catch (error) {
    console.error('Check-in error:', error)
    notification.error('Không thể check-in: ' + (error.response?.data?.message || error.message))
  }
}
</script>

