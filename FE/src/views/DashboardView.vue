<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p class="text-gray-600 mt-1">Tổng quan hệ thống quản lý nhà hàng</p>
      </div>
      <button @click="loadDashboard" class="btn-primary flex items-center gap-2">
        <ArrowPathIcon class="w-5 h-5" :class="{ 'animate-spin': loading }" />
        Làm mới
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading && !stats" class="flex items-center justify-center h-64">
      <div class="loading-spinner"></div>
    </div>

    <!-- Dashboard Content -->
    <template v-else-if="stats">
      <!-- Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- Total Customers -->
        <div class="card bg-gradient-to-br from-blue-500 to-blue-600 text-white transform hover:scale-105 transition-transform cursor-pointer">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-blue-100 text-sm font-medium">Tổng khách hàng</p>
              <p class="text-3xl font-bold mt-2">{{ stats.totalCustomers }}</p>
              <p class="text-blue-100 text-xs mt-2">VIP: {{ stats.vipCustomers }}</p>
            </div>
            <UsersIcon class="w-16 h-16 text-blue-200 opacity-50" />
          </div>
        </div>

        <!-- Total Tables -->
        <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white transform hover:scale-105 transition-transform cursor-pointer">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-green-100 text-sm font-medium">Tổng số bàn</p>
              <p class="text-3xl font-bold mt-2">{{ stats.totalTables }}</p>
              <p class="text-green-100 text-xs mt-2">Trống: {{ stats.availableTables }}</p>
            </div>
            <TableCellsIcon class="w-16 h-16 text-green-200 opacity-50" />
          </div>
        </div>

        <!-- Total Dishes -->
        <div class="card bg-gradient-to-br from-orange-500 to-orange-600 text-white transform hover:scale-105 transition-transform cursor-pointer">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-orange-100 text-sm font-medium">Tổng món ăn</p>
              <p class="text-3xl font-bold mt-2">{{ stats.totalDishes }}</p>
              <p class="text-orange-100 text-xs mt-2">Sẵn có: {{ stats.availableDishes }}</p>
            </div>
            <CakeIcon class="w-16 h-16 text-orange-200 opacity-50" />
          </div>
        </div>

        <!-- Total Orders -->
        <div class="card bg-gradient-to-br from-purple-500 to-purple-600 text-white transform hover:scale-105 transition-transform cursor-pointer">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-purple-100 text-sm font-medium">Tổng đơn hàng</p>
              <p class="text-3xl font-bold mt-2">{{ stats.totalOrders }}</p>
              <p class="text-purple-100 text-xs mt-2">Hoàn thành: {{ stats.completedOrders }}</p>
            </div>
            <ShoppingBagIcon class="w-16 h-16 text-purple-200 opacity-50" />
          </div>
        </div>
      </div>

      <!-- Revenue Stats -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="card bg-gradient-to-br from-sky-500 to-sky-600 text-white">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold">Doanh thu hôm nay</h3>
            <CurrencyDollarIcon class="w-8 h-8 text-sky-200" />
          </div>
          <p class="text-3xl font-bold">{{ formatCurrency(stats.todayRevenue) }}</p>
        </div>

        <div class="card bg-gradient-to-br from-sky-600 to-sky-700 text-white">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold">Doanh thu tháng này</h3>
            <CurrencyDollarIcon class="w-8 h-8 text-sky-200" />
          </div>
          <p class="text-3xl font-bold">{{ formatCurrency(stats.monthRevenue) }}</p>
        </div>

        <div class="card bg-gradient-to-br from-sky-700 to-sky-800 text-white">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold">Doanh thu năm nay</h3>
            <CurrencyDollarIcon class="w-8 h-8 text-sky-200" />
          </div>
          <p class="text-3xl font-bold">{{ formatCurrency(stats.yearRevenue) }}</p>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Table Status Chart -->
        <div class="card">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Trạng thái bàn</h3>
          <div class="space-y-3">
            <div v-for="(count, status) in stats.tableStatusCount" :key="status" class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 rounded-full" :class="getStatusColor(status)"></div>
                <span class="text-sm font-medium text-gray-700">{{ translateStatus(status) }}</span>
              </div>
              <span class="text-lg font-bold text-gray-900">{{ count }}</span>
            </div>
          </div>
        </div>

        <!-- Order Status Chart -->
        <div class="card">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Trạng thái đơn hàng</h3>
          <div class="space-y-3">
            <div v-for="(count, status) in stats.orderStatusCount" :key="status" class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 rounded-full" :class="getOrderStatusColor(status)"></div>
                <span class="text-sm font-medium text-gray-700">{{ translateOrderStatus(status) }}</span>
              </div>
              <span class="text-lg font-bold text-gray-900">{{ count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Best Selling Dishes -->
      <div class="card">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-semibold text-gray-900">Món ăn bán chạy nhất</h3>
          <router-link to="/reports" class="text-sm text-sky-600 hover:text-sky-700 font-medium">
            Xem tất cả →
          </router-link>
        </div>
        
        <div v-if="bestSelling.length > 0" class="space-y-4">
          <div 
            v-for="(dish, index) in bestSelling.slice(0, 5)" 
            :key="dish.dishId"
            class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition"
          >
            <div class="flex items-center gap-4">
              <div class="flex items-center justify-center w-10 h-10 bg-sky-500 text-white rounded-full font-bold">
                {{ index + 1 }}
              </div>
              <div>
                <p class="font-medium text-gray-900">{{ dish.dishName }}</p>
                <p class="text-sm text-gray-600">{{ dish.categoryName }}</p>
              </div>
            </div>
            <div class="text-right">
              <p class="font-bold text-gray-900">{{ dish.totalQuantitySold }} phần</p>
              <p class="text-sm text-green-600">{{ formatCurrency(dish.totalRevenue) }}</p>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-8 text-gray-500">
          Chưa có dữ liệu món ăn bán chạy
        </div>
      </div>

      <!-- Rating -->
      <div class="card bg-gradient-to-r from-yellow-400 to-orange-500 text-white">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold mb-2">Đánh giá trung bình</h3>
            <div class="flex items-center gap-2">
              <span class="text-4xl font-bold">{{ stats.averageRating.toFixed(1) }}</span>
              <div class="flex">
                <StarIcon v-for="i in 5" :key="i" class="w-6 h-6" :class="i <= Math.round(stats.averageRating) ? 'text-yellow-200' : 'text-yellow-600'" />
              </div>
            </div>
          </div>
          <ChatBubbleLeftRightIcon class="w-20 h-20 text-orange-300 opacity-50" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dashboardService } from '@/services/dashboardService'
import { useNotificationStore } from '@/stores/notification'
import {
  UsersIcon,
  TableCellsIcon,
  CakeIcon,
  ShoppingBagIcon,
  CurrencyDollarIcon,
  ArrowPathIcon,
  ChatBubbleLeftRightIcon,
  StarIcon
} from '@heroicons/vue/24/outline'

const notification = useNotificationStore()

const loading = ref(false)
const stats = ref(null)
const bestSelling = ref([])

onMounted(() => {
  loadDashboard()
})

async function loadDashboard() {
  loading.value = true
  try {
    const [statsResponse, dishesResponse] = await Promise.all([
      dashboardService.getStats(),
      dashboardService.getBestSellingDishes(5)
    ])
    
    stats.value = statsResponse.data || statsResponse
    bestSelling.value = dishesResponse.data || dishesResponse
  } catch (error) {
    console.error('Dashboard error:', error)
    notification.error('Không thể tải dữ liệu dashboard: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value || 0)
}

function getStatusColor(status) {
  const colors = {
    AVAILABLE: 'bg-green-500',
    OCCUPIED: 'bg-red-500',
    RESERVED: 'bg-yellow-500',
    MAINTENANCE: 'bg-gray-500'
  }
  return colors[status] || 'bg-gray-500'
}

function translateStatus(status) {
  const translations = {
    AVAILABLE: 'Trống',
    OCCUPIED: 'Đang phục vụ',
    RESERVED: 'Đã đặt',
    MAINTENANCE: 'Bảo trì'
  }
  return translations[status] || status
}

function getOrderStatusColor(status) {
  const colors = {
    PENDING: 'bg-yellow-500',
    CONFIRMED: 'bg-blue-500',
    PREPARING: 'bg-orange-500',
    READY: 'bg-purple-500',
    SERVED: 'bg-green-500',
    COMPLETED: 'bg-emerald-500',
    CANCELLED: 'bg-red-500'
  }
  return colors[status] || 'bg-gray-500'
}

function translateOrderStatus(status) {
  const translations = {
    PENDING: 'Chờ xử lý',
    CONFIRMED: 'Đã xác nhận',
    PREPARING: 'Đang chuẩn bị',
    READY: 'Sẵn sàng',
    SERVED: 'Đã phục vụ',
    COMPLETED: 'Hoàn thành',
    CANCELLED: 'Đã hủy'
  }
  return translations[status] || status
}
</script>
