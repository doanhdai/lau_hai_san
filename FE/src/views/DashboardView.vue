<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Dashboard</h1>
        <p class="text-slate-600 mt-1 text-sm">Tổng quan hệ thống quản lý nhà hàng</p>
      </div>
      <button @click="loadDashboard" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
        <i class="fas fa-sync-alt" :class="{ 'animate-spin': loading }"></i>
        <span>Làm mới</span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading && !stats" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Dashboard Content -->
    <template v-else-if="stats">
      <!-- Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Total Customers -->
        <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-500 text-xs font-medium mb-1">Tổng khách hàng</p>
              <p class="text-2xl font-bold text-slate-900">{{ stats.totalCustomers }}</p>
              <p class="text-slate-500 text-xs mt-1">VIP: {{ stats.vipCustomers }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-users text-blue-600 text-xl"></i>
            </div>
          </div>
        </div>

        <!-- Total Tables -->
        <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-500 text-xs font-medium mb-1">Tổng số bàn</p>
              <p class="text-2xl font-bold text-slate-900">{{ stats.totalTables }}</p>
              <p class="text-slate-500 text-xs mt-1">Trống: {{ stats.availableTables }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-chair text-green-600 text-xl"></i>
            </div>
          </div>
        </div>

        <!-- Total Dishes -->
        <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-500 text-xs font-medium mb-1">Tổng món ăn</p>
              <p class="text-2xl font-bold text-slate-900">{{ stats.totalDishes }}</p>
              <p class="text-slate-500 text-xs mt-1">Sẵn có: {{ stats.availableDishes }}</p>
            </div>
            <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-utensils text-orange-600 text-xl"></i>
            </div>
          </div>
        </div>

        <!-- Total Orders -->
        <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-500 text-xs font-medium mb-1">Tổng đơn hàng</p>
              <p class="text-2xl font-bold text-slate-900">{{ stats.totalOrders }}</p>
              <p class="text-slate-500 text-xs mt-1">Hoàn thành: {{ stats.completedOrders }}</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-shopping-bag text-purple-600 text-xl"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- Revenue Stats -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-slate-900 text-white rounded-lg p-5 border border-slate-800">
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-sm font-medium text-slate-300">Doanh thu hôm nay</h3>
            <i class="fas fa-dollar-sign text-slate-400"></i>
          </div>
          <p class="text-2xl font-bold">{{ formatCurrency(stats.todayRevenue) }}</p>
        </div>

        <div class="bg-slate-900 text-white rounded-lg p-5 border border-slate-800">
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-sm font-medium text-slate-300">Doanh thu tháng này</h3>
            <i class="fas fa-dollar-sign text-slate-400"></i>
          </div>
          <p class="text-2xl font-bold">{{ formatCurrency(stats.monthRevenue) }}</p>
        </div>

        <div class="bg-slate-900 text-white rounded-lg p-5 border border-slate-800">
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-sm font-medium text-slate-300">Doanh thu năm nay</h3>
            <i class="fas fa-dollar-sign text-slate-400"></i>
          </div>
          <p class="text-2xl font-bold">{{ formatCurrency(stats.yearRevenue) }}</p>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <!-- Table Status Chart -->
        <div class="bg-white border border-gray-200 rounded-lg p-5">
          <h3 class="text-base font-semibold text-slate-900 mb-4">Trạng thái bàn</h3>
          <div class="space-y-3">
            <div v-for="(count, status) in stats.tableStatusCount" :key="status" class="flex items-center justify-between py-2">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 rounded-full" :class="getStatusColor(status)"></div>
                <span class="text-sm font-medium text-slate-700">{{ translateStatus(status) }}</span>
              </div>
              <span class="text-base font-bold text-slate-900">{{ count }}</span>
            </div>
          </div>
        </div>

        <!-- Order Status Chart -->
        <div class="bg-white border border-gray-200 rounded-lg p-5">
          <h3 class="text-base font-semibold text-slate-900 mb-4">Trạng thái đơn hàng</h3>
          <div class="space-y-3">
            <div v-for="(count, status) in stats.orderStatusCount" :key="status" class="flex items-center justify-between py-2">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 rounded-full" :class="getOrderStatusColor(status)"></div>
                <span class="text-sm font-medium text-slate-700">{{ translateOrderStatus(status) }}</span>
              </div>
              <span class="text-base font-bold text-slate-900">{{ count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Best Selling Dishes -->
      <div class="bg-white border border-gray-200 rounded-lg p-5">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-base font-semibold text-slate-900">Món ăn bán chạy nhất</h3>
          <router-link to="/reports" class="text-sm text-slate-600 hover:text-slate-900 font-medium flex items-center gap-1">
            <span>Xem tất cả</span>
            <i class="fas fa-arrow-right text-xs"></i>
          </router-link>
        </div>
        
        <div v-if="bestSelling.length > 0" class="space-y-3">
          <div 
            v-for="(dish, index) in bestSelling.slice(0, 5)" 
            :key="dish.dishId"
            class="flex items-center justify-between p-3 bg-slate-50 rounded-lg hover:bg-slate-100 transition border border-gray-100"
          >
            <div class="flex items-center gap-3">
              <div class="flex items-center justify-center w-8 h-8 bg-slate-900 text-white rounded-lg font-bold text-sm">
                {{ index + 1 }}
              </div>
              <div>
                <p class="font-medium text-slate-900 text-sm">{{ dish.dishName }}</p>
                <p class="text-xs text-slate-500">{{ dish.categoryName }}</p>
              </div>
            </div>
            <div class="text-right">
              <p class="font-bold text-slate-900 text-sm">{{ dish.totalQuantitySold }} phần</p>
              <p class="text-xs text-green-600">{{ formatCurrency(dish.totalRevenue) }}</p>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-8 text-slate-500 text-sm">
          Chưa có dữ liệu món ăn bán chạy
        </div>
      </div>

      <!-- Rating -->
      <div class="bg-amber-50 border border-amber-200 rounded-lg p-5">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-base font-semibold text-slate-900 mb-2">Đánh giá trung bình</h3>
            <div class="flex items-center gap-2">
              <span class="text-3xl font-bold text-slate-900">{{ stats.averageRating.toFixed(1) }}</span>
              <div class="flex gap-1">
                <i v-for="i in 5" :key="i" :class="['text-sm', i <= Math.round(stats.averageRating) ? 'fas fa-star text-amber-500' : 'far fa-star text-amber-300']"></i>
              </div>
            </div>
          </div>
          <div class="w-16 h-16 bg-amber-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-comments text-amber-600 text-2xl"></i>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dashboardService } from '@/services/dashboardService'
import { useNotificationStore } from '@/stores/notification'

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

