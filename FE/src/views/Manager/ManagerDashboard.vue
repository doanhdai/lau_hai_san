<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b">
      <div class="px-6 py-4">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">📊 Manager Dashboard</h1>
            <p class="text-gray-600">Quản lý vận hành nhà hàng</p>
          </div>
          <div class="text-right">
            <p class="text-sm text-gray-500">{{ currentTime }}</p>
            <p class="text-sm font-medium text-sky-600">{{ user?.fullName }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="p-6">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- Doanh thu hôm nay -->
        <div class="bg-gradient-to-r from-emerald-500 to-emerald-600 rounded-xl p-6 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-emerald-100 text-sm">Doanh thu hôm nay</p>
              <p class="text-3xl font-bold">{{ formatCurrency(stats.todayRevenue) }}</p>
              <p class="text-emerald-200 text-xs">+12% so với hôm qua</p>
            </div>
            <div class="text-4xl opacity-80">💰</div>
          </div>
        </div>

        <!-- Đơn hàng -->
        <div class="bg-gradient-to-r from-blue-500 to-blue-600 rounded-xl p-6 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-blue-100 text-sm">Đơn hàng hôm nay</p>
              <p class="text-3xl font-bold">{{ stats.todayOrders }}</p>
              <p class="text-blue-200 text-xs">{{ stats.completedOrders }} hoàn thành</p>
            </div>
            <div class="text-4xl opacity-80">🛒</div>
          </div>
        </div>

        <!-- Khách hàng -->
        <div class="bg-gradient-to-r from-purple-500 to-purple-600 rounded-xl p-6 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-purple-100 text-sm">Khách hàng hôm nay</p>
              <p class="text-3xl font-bold">{{ stats.todayCustomers }}</p>
              <p class="text-purple-200 text-xs">{{ stats.newCustomers }} khách mới</p>
            </div>
            <div class="text-4xl opacity-80">👥</div>
          </div>
        </div>

        <!-- Tỷ lệ lấp đầy -->
        <div class="bg-gradient-to-r from-orange-500 to-orange-600 rounded-xl p-6 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-orange-100 text-sm">Tỷ lệ lấp đầy</p>
              <p class="text-3xl font-bold">{{ stats.occupancyRate }}%</p>
              <p class="text-orange-200 text-xs">{{ stats.occupiedTables }}/{{ stats.totalTables }} bàn</p>
            </div>
            <div class="text-4xl opacity-80">📈</div>
          </div>
        </div>
      </div>

      <!-- Charts & Analytics -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <!-- Doanh thu 7 ngày -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <span class="text-2xl mr-2">📊</span>
            Doanh thu 7 ngày qua
          </h3>
          <div class="space-y-3">
            <div v-for="day in revenueChart" :key="day.date" class="flex items-center">
              <div class="w-20 text-sm text-gray-600">{{ day.date }}</div>
              <div class="flex-1 mx-3">
                <div class="bg-gray-200 rounded-full h-2">
                  <div 
                    class="bg-emerald-500 h-2 rounded-full transition-all duration-500"
                    :style="{ width: `${(day.amount / maxRevenue) * 100}%` }"
                  ></div>
                </div>
              </div>
              <div class="w-24 text-sm font-medium text-right">{{ formatCurrency(day.amount) }}</div>
            </div>
          </div>
        </div>

        <!-- Top món ăn -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <span class="text-2xl mr-2">🍽️</span>
            Món ăn bán chạy
          </h3>
          <div class="space-y-4">
            <div 
              v-for="(dish, index) in topDishes" 
              :key="dish.id"
              class="flex items-center space-x-3"
            >
              <div class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
                   :class="index === 0 ? 'bg-yellow-100 text-yellow-800' : 
                          index === 1 ? 'bg-gray-100 text-gray-800' :
                          index === 2 ? 'bg-orange-100 text-orange-800' :
                          'bg-blue-100 text-blue-800'"
              >
                {{ index + 1 }}
              </div>
              <div class="flex-1">
                <p class="font-medium text-gray-900">{{ dish.name }}</p>
                <p class="text-sm text-gray-500">{{ dish.orders }} đơn hàng</p>
              </div>
              <div class="text-right">
                <p class="font-medium text-gray-900">{{ formatCurrency(dish.revenue) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Management Actions -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <!-- Quản lý nhân viên -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <span class="text-2xl mr-2">👨‍💼</span>
            Quản lý nhân viên
          </h3>
          <div class="space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Tổng nhân viên</span>
              <span class="font-semibold">{{ stats.totalStaff }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Đang làm việc</span>
              <span class="font-semibold text-green-600">{{ stats.activeStaff }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Nghỉ phép</span>
              <span class="font-semibold text-yellow-600">{{ stats.onLeaveStaff }}</span>
            </div>
          </div>
          <router-link 
            to="/manager/staff"
            class="block w-full mt-4 bg-sky-50 hover:bg-sky-100 text-sky-700 text-center py-2 rounded-lg transition-colors"
          >
            Quản lý nhân viên
          </router-link>
        </div>

        <!-- Báo cáo nhanh -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <span class="text-2xl mr-2">📋</span>
            Báo cáo nhanh
          </h3>
          <div class="space-y-3">
            <button class="w-full text-left p-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
              <div class="flex items-center justify-between">
                <span class="text-gray-700">Báo cáo doanh thu</span>
                <span class="text-gray-400">→</span>
              </div>
            </button>
            <button class="w-full text-left p-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
              <div class="flex items-center justify-between">
                <span class="text-gray-700">Báo cáo khách hàng</span>
                <span class="text-gray-400">→</span>
              </div>
            </button>
            <button class="w-full text-left p-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
              <div class="flex items-center justify-between">
                <span class="text-gray-700">Báo cáo tồn kho</span>
                <span class="text-gray-400">→</span>
              </div>
            </button>
          </div>
        </div>

        <!-- Thông báo -->
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <span class="text-2xl mr-2">🔔</span>
            Thông báo
          </h3>
          <div class="space-y-3">
            <div 
              v-for="notification in notifications" 
              :key="notification.id"
              class="p-3 rounded-lg border-l-4"
              :class="getNotificationClass(notification.type)"
            >
              <p class="text-sm font-medium">{{ notification.title }}</p>
              <p class="text-xs text-gray-500 mt-1">{{ notification.time }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
          <span class="text-2xl mr-2">⚡</span>
          Thao tác nhanh
        </h3>
        <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
          <router-link 
            to="/manager/menu"
            class="bg-orange-50 hover:bg-orange-100 border border-orange-200 rounded-lg p-4 text-center transition-colors"
          >
            <div class="text-2xl mb-2">🍽️</div>
            <p class="text-sm font-medium text-orange-700">Thực đơn</p>
          </router-link>
          <router-link 
            to="/manager/categories"
            class="bg-purple-50 hover:bg-purple-100 border border-purple-200 rounded-lg p-4 text-center transition-colors"
          >
            <div class="text-2xl mb-2">🏷️</div>
            <p class="text-sm font-medium text-purple-700">Danh mục</p>
          </router-link>
          <router-link 
            to="/manager/promotions"
            class="bg-red-50 hover:bg-red-100 border border-red-200 rounded-lg p-4 text-center transition-colors"
          >
            <div class="text-2xl mb-2">🎁</div>
            <p class="text-sm font-medium text-red-700">Khuyến mãi</p>
          </router-link>
          <router-link 
            to="/manager/tables"
            class="bg-green-50 hover:bg-green-100 border border-green-200 rounded-lg p-4 text-center transition-colors"
          >
            <div class="text-2xl mb-2">🪑</div>
            <p class="text-sm font-medium text-green-700">Quản lý bàn</p>
          </router-link>
          <router-link 
            to="/manager/orders"
            class="bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-lg p-4 text-center transition-colors"
          >
            <div class="text-2xl mb-2">🛒</div>
            <p class="text-sm font-medium text-blue-700">Đơn hàng</p>
          </router-link>
          <router-link 
            to="/manager/reports"
            class="bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded-lg p-4 text-center transition-colors"
          >
            <div class="text-2xl mb-2">📊</div>
            <p class="text-sm font-medium text-gray-700">Báo cáo</p>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { dashboardService } from '@/services/dashboardService'
import { userService } from '@/services/userService'

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
  todayRevenue: 0,
  todayOrders: 0,
  completedOrders: 0,
  todayCustomers: 0,
  newCustomers: 0,
  occupancyRate: 0,
  occupiedTables: 0,
  totalTables: 0,
  totalStaff: 0,
  activeStaff: 0,
  onLeaveStaff: 0
})

// Revenue chart data
const revenueChart = ref([])
const maxRevenue = computed(() => revenueChart.value.length > 0 ? Math.max(...revenueChart.value.map(d => d.amount)) : 1)

// Top dishes
const topDishes = ref([])

// Notifications
const notifications = ref([])

// Load stats
const loadStats = async () => {
  try {
    const dashboardStats = await dashboardService.getStats()
    stats.value = {
      todayRevenue: dashboardStats.todayRevenue || 0,
      todayOrders: dashboardStats.todayOrders || 0,
      completedOrders: dashboardStats.completedOrders || 0,
      todayCustomers: dashboardStats.todayCustomers || 0,
      newCustomers: dashboardStats.newCustomers || 0,
      occupancyRate: dashboardStats.occupancyRate || 0,
      occupiedTables: dashboardStats.occupiedTables || 0,
      totalTables: dashboardStats.totalTables || 0,
      totalStaff: dashboardStats.totalStaff || 0,
      activeStaff: dashboardStats.activeStaff || 0,
      onLeaveStaff: dashboardStats.onLeaveStaff || 0
    }
  } catch (error) {
    console.error('Error loading stats:', error)
  }
}

// Load revenue chart
const loadRevenueChart = async () => {
  try {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(startDate.getDate() - 6)
    
    const revenueData = await dashboardService.getRevenue(
      startDate.toISOString().split('T')[0],
      endDate.toISOString().split('T')[0]
    )
    
    const dayNames = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7']
    revenueChart.value = revenueData.map(item => ({
      date: dayNames[new Date(item.date).getDay()],
      amount: item.amount || 0
    }))
  } catch (error) {
    console.error('Error loading revenue chart:', error)
  }
}

// Load top dishes
const loadTopDishes = async () => {
  try {
    const dishes = await dashboardService.getBestSellingDishes(4)
    topDishes.value = dishes.map(d => ({
      id: d.id,
      name: d.name,
      orders: d.orderCount || 0,
      revenue: d.revenue || 0
    }))
  } catch (error) {
    console.error('Error loading top dishes:', error)
  }
}

// Helper functions
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const getNotificationClass = (type) => {
  const classes = {
    warning: 'border-yellow-400 bg-yellow-50',
    info: 'border-blue-400 bg-blue-50',
    success: 'border-green-400 bg-green-50',
    error: 'border-red-400 bg-red-50'
  }
  return classes[type] || 'border-gray-400 bg-gray-50'
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
  loadStats()
  loadRevenueChart()
  loadTopDishes()
  
  // Refresh data every 60 seconds
  setInterval(() => {
    loadStats()
    loadRevenueChart()
    loadTopDishes()
  }, 60000)
})
</script>
