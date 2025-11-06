<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b">
      <div class="px-6 py-4">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">🏨 Lễ Tân Dashboard</h1>
            <p class="text-gray-600">Quản lý đặt bàn và phục vụ khách hàng</p>
          </div>
          <div class="text-right">
            <p class="text-sm text-gray-500">{{ currentTime }}</p>
            <p class="text-sm font-medium text-sky-600">{{ user?.fullName }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="p-6">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- Đặt bàn hôm nay -->
        <div class="bg-gradient-to-r from-blue-500 to-blue-600 rounded-xl p-6 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-blue-100 text-sm">Đặt bàn hôm nay</p>
              <p class="text-3xl font-bold">{{ stats.todayReservations }}</p>
            </div>
            <div class="text-4xl opacity-80">📅</div>
          </div>
        </div>

        <!-- Bàn đang phục vụ -->
        <div class="bg-gradient-to-r from-green-500 to-green-600 rounded-xl p-6 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-green-100 text-sm">Bàn đang phục vụ</p>
              <p class="text-3xl font-bold">{{ stats.activeTables }}</p>
            </div>
            <div class="text-4xl opacity-80">🍽️</div>
          </div>
        </div>

        <!-- Chờ xác nhận -->
        <div class="bg-gradient-to-r from-yellow-500 to-yellow-600 rounded-xl p-6 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-yellow-100 text-sm">Chờ xác nhận</p>
              <p class="text-3xl font-bold">{{ stats.pendingReservations }}</p>
            </div>
            <div class="text-4xl opacity-80">⏳</div>
          </div>
        </div>

        <!-- Khách hàng -->
        <div class="bg-gradient-to-r from-purple-500 to-purple-600 rounded-xl p-6 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-purple-100 text-sm">Tổng khách hàng</p>
              <p class="text-3xl font-bold">{{ stats.totalCustomers }}</p>
            </div>
            <div class="text-4xl opacity-80">👥</div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <!-- Đặt bàn nhanh -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <span class="text-2xl mr-2">⚡</span>
            Thao tác nhanh
          </h3>
          <div class="grid grid-cols-2 gap-4">
            <router-link 
              to="/staff/reservations/new"
              class="bg-sky-50 hover:bg-sky-100 border border-sky-200 rounded-lg p-4 text-center transition-colors"
            >
              <div class="text-2xl mb-2">📝</div>
              <p class="text-sm font-medium text-sky-700">Đặt bàn mới</p>
            </router-link>
            <router-link 
              to="/staff/tables"
              class="bg-green-50 hover:bg-green-100 border border-green-200 rounded-lg p-4 text-center transition-colors"
            >
              <div class="text-2xl mb-2">🪑</div>
              <p class="text-sm font-medium text-green-700">Quản lý bàn</p>
            </router-link>
            <router-link 
              to="/staff/customers"
              class="bg-purple-50 hover:bg-purple-100 border border-purple-200 rounded-lg p-4 text-center transition-colors"
            >
              <div class="text-2xl mb-2">👤</div>
              <p class="text-sm font-medium text-purple-700">Khách hàng</p>
            </router-link>
            <router-link 
              to="/staff/orders"
              class="bg-orange-50 hover:bg-orange-100 border border-orange-200 rounded-lg p-4 text-center transition-colors"
            >
              <div class="text-2xl mb-2">🛒</div>
              <p class="text-sm font-medium text-orange-700">Đơn hàng</p>
            </router-link>
          </div>
        </div>

        <!-- Đặt bàn gần đây -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <span class="text-2xl mr-2">📋</span>
            Đặt bàn gần đây
          </h3>
          <div class="space-y-3">
            <div 
              v-for="reservation in recentReservations" 
              :key="reservation.id"
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <div class="flex items-center space-x-3">
                <div class="w-8 h-8 bg-sky-100 rounded-full flex items-center justify-center">
                  <span class="text-sky-600 font-medium text-sm">{{ reservation.customerName.charAt(0) }}</span>
                </div>
                <div>
                  <p class="font-medium text-gray-900">{{ reservation.customerName }}</p>
                  <p class="text-sm text-gray-500">{{ reservation.tableNumber }} - {{ reservation.time }}</p>
                </div>
              </div>
              <span 
                class="px-2 py-1 rounded-full text-xs font-medium"
                :class="getStatusClass(reservation.status)"
              >
                {{ getStatusText(reservation.status) }}
              </span>
            </div>
          </div>
          <router-link 
            to="/staff/reservations"
            class="block text-center text-sky-600 hover:text-sky-700 text-sm font-medium mt-4"
          >
            Xem tất cả →
          </router-link>
        </div>
      </div>

      <!-- Bàn trống hiện tại -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
          <span class="text-2xl mr-2">🪑</span>
          Tình trạng bàn hiện tại
        </h3>
        <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
          <div 
            v-for="table in tables" 
            :key="table.id"
            class="border-2 rounded-lg p-4 text-center transition-colors"
            :class="getTableClass(table.status)"
          >
            <div class="text-2xl mb-2">{{ getTableIcon(table.status) }}</div>
            <p class="font-medium">{{ table.name }}</p>
            <p class="text-xs text-gray-500">{{ table.capacity }} chỗ</p>
            <p class="text-xs mt-1" :class="getTableTextClass(table.status)">
              {{ getTableStatusText(table.status) }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { dashboardService } from '@/services/dashboardService'
import { reservationService } from '@/services/reservationService'
import { tableService } from '@/services/tableService'
import { customerService } from '@/services/customerService'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

// Current time
const currentTime = ref('')
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('vi-VN')
}

// Stats data
const stats = ref({
  todayReservations: 0,
  activeTables: 0,
  pendingReservations: 0,
  totalCustomers: 0
})

// Recent reservations
const recentReservations = ref([])

// Tables data
const tables = ref([])

// Load stats
const loadStats = async () => {
  try {
    const dashboardStats = await dashboardService.getStats()
    stats.value = {
      todayReservations: dashboardStats.todayReservations || 0,
      activeTables: dashboardStats.activeTables || 0,
      pendingReservations: dashboardStats.pendingReservations || 0,
      totalCustomers: dashboardStats.totalCustomers || 0
    }
  } catch (error) {
    console.error('Error loading stats:', error)
  }
}

// Load recent reservations
const loadRecentReservations = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const reservations = await reservationService.getByDate(today, today)
    recentReservations.value = reservations.slice(0, 3).map(r => ({
      id: r.id,
      customerName: r.customerName,
      tableNumber: r.tableNumber || 'Chưa chỉ định',
      time: new Date(r.reservationTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
      status: r.status
    }))
  } catch (error) {
    console.error('Error loading reservations:', error)
  }
}

// Load tables
const loadTables = async () => {
  try {
    const allTables = await tableService.getAll()
    tables.value = allTables.slice(0, 10).map(t => ({
      id: t.id,
      name: t.tableNumber,
      capacity: t.capacity,
      status: t.status.toLowerCase()
    }))
  } catch (error) {
    console.error('Error loading tables:', error)
  }
}

// Status helpers
const getStatusClass = (status) => {
  const classes = {
    pending: 'bg-yellow-100 text-yellow-800',
    confirmed: 'bg-green-100 text-green-800',
    completed: 'bg-blue-100 text-blue-800',
    cancelled: 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status) => {
  const texts = {
    pending: 'Chờ xác nhận',
    confirmed: 'Đã xác nhận',
    completed: 'Hoàn thành',
    cancelled: 'Đã hủy'
  }
  return texts[status] || 'Không xác định'
}

const getTableClass = (status) => {
  const classes = {
    available: 'border-green-300 bg-green-50',
    occupied: 'border-red-300 bg-red-50',
    reserved: 'border-yellow-300 bg-yellow-50',
    cleaning: 'border-blue-300 bg-blue-50'
  }
  return classes[status] || 'border-gray-300 bg-gray-50'
}

const getTableTextClass = (status) => {
  const classes = {
    available: 'text-green-600',
    occupied: 'text-red-600',
    reserved: 'text-yellow-600',
    cleaning: 'text-blue-600'
  }
  return classes[status] || 'text-gray-600'
}

const getTableIcon = (status) => {
  const icons = {
    available: '✅',
    occupied: '🔴',
    reserved: '🟡',
    cleaning: '🧹'
  }
  return icons[status] || '❓'
}

const getTableStatusText = (status) => {
  const texts = {
    available: 'Trống',
    occupied: 'Có khách',
    reserved: 'Đã đặt',
    cleaning: 'Dọn dẹp'
  }
  return texts[status] || 'Không rõ'
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
  loadStats()
  loadRecentReservations()
  loadTables()
  
  // Refresh data every 30 seconds
  setInterval(() => {
    loadStats()
    loadRecentReservations()
    loadTables()
  }, 30000)
})
</script>
