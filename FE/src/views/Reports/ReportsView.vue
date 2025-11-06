<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Báo cáo thống kê</h1>
        <p class="text-gray-600 mt-1">Xem báo cáo doanh thu và bán hàng</p>
      </div>
    </div>

    <!-- Date Range Filter -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
          <input
            v-model="filters.startDate"
            type="date"
            class="input-field"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
          <input
            v-model="filters.endDate"
            type="date"
            class="input-field"
          />
        </div>
        <div class="flex items-end">
          <button @click="loadReports" class="btn-primary w-full">
            <span class="mr-2">📊</span>
            Tạo báo cáo
          </button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">Đang tải báo cáo...</p>
    </div>

    <!-- Revenue Report -->
    <div v-else-if="revenueReport" class="space-y-6">
      <!-- Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-blue-100 text-sm">Tổng doanh thu</p>
              <p class="text-3xl font-bold mt-1">{{ formatCurrency(revenueReport.totalRevenue) }}</p>
            </div>
            <div class="text-5xl opacity-50">💰</div>
          </div>
        </div>

        <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-green-100 text-sm">Tổng đơn hàng</p>
              <p class="text-3xl font-bold mt-1">{{ revenueReport.totalOrders }}</p>
            </div>
            <div class="text-5xl opacity-50">🛒</div>
          </div>
        </div>

        <div class="card bg-gradient-to-br from-purple-500 to-purple-600 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-purple-100 text-sm">Trung bình/Đơn</p>
              <p class="text-3xl font-bold mt-1">{{ formatCurrency(revenueReport.averageOrderValue) }}</p>
            </div>
            <div class="text-5xl opacity-50">📈</div>
          </div>
        </div>
      </div>

      <!-- Daily Revenue Chart -->
      <div class="card">
        <h2 class="text-xl font-bold text-gray-900 mb-4">Doanh thu theo ngày</h2>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Số đơn</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Doanh thu</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="day in revenueReport.dailyRevenue" :key="day.date">
                <td class="px-6 py-4 text-sm text-gray-900">{{ formatDate(day.date) }}</td>
                <td class="px-6 py-4 text-sm text-gray-900 text-right">{{ day.orderCount }}</td>
                <td class="px-6 py-4 text-sm font-medium text-primary-600 text-right">
                  {{ formatCurrency(day.revenue) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Sales Report -->
      <div v-if="salesReport" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Top Selling Dishes -->
        <div class="card">
          <h2 class="text-xl font-bold text-gray-900 mb-4">Món bán chạy nhất</h2>
          <div class="space-y-3">
            <div
              v-for="(dish, index) in salesReport.topSellingDishes"
              :key="dish.dishId"
              class="flex items-center gap-4 p-3 bg-gray-50 rounded-lg"
            >
              <div class="w-8 h-8 flex items-center justify-center bg-primary-600 text-white rounded-full font-bold">
                {{ index + 1 }}
              </div>
              <div class="flex-1">
                <p class="font-medium text-gray-900">{{ dish.dishName }}</p>
                <p class="text-xs text-gray-500">{{ dish.categoryName }}</p>
              </div>
              <div class="text-right">
                <p class="font-bold text-gray-900">{{ dish.quantitySold }} phần</p>
                <p class="text-sm text-primary-600">{{ formatCurrency(dish.revenue) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Sales by Category -->
        <div class="card">
          <h2 class="text-xl font-bold text-gray-900 mb-4">Doanh thu theo danh mục</h2>
          <div class="space-y-3">
            <div
              v-for="category in salesReport.salesByCategory"
              :key="category.categoryName"
              class="p-3 bg-gray-50 rounded-lg"
            >
              <div class="flex justify-between items-center mb-2">
                <span class="font-medium text-gray-900">{{ category.categoryName }}</span>
                <span class="text-sm text-gray-600">{{ category.itemsSold }} món</span>
              </div>
              <div class="flex justify-between items-center">
                <div class="flex-1 bg-gray-200 rounded-full h-2 mr-3">
                  <div
                    class="bg-primary-600 h-2 rounded-full"
                    :style="{ width: getCategoryPercentage(category.revenue) + '%' }"
                  ></div>
                </div>
                <span class="font-bold text-primary-600">{{ formatCurrency(category.revenue) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { reportService } from '@/services/reportService'
import { useNotificationStore } from '@/stores/notification'

const notification = useNotificationStore()

const loading = ref(false)
const revenueReport = ref(null)
const salesReport = ref(null)

const filters = reactive({
  startDate: new Date(new Date().setDate(1)).toISOString().split('T')[0], // First day of month
  endDate: new Date().toISOString().split('T')[0] // Today
})

const maxCategoryRevenue = computed(() => {
  if (!salesReport.value?.salesByCategory?.length) return 0
  return Math.max(...salesReport.value.salesByCategory.map(c => c.revenue))
})

async function loadReports() {
  if (!filters.startDate || !filters.endDate) {
    notification.error('Vui lòng chọn khoảng thời gian')
    return
  }

  loading.value = true
  try {
    const [revenueRes, salesRes] = await Promise.all([
      reportService.getRevenueReport(filters.startDate, filters.endDate),
      reportService.getSalesReport(filters.startDate, filters.endDate)
    ])

    if (revenueRes.success) {
      revenueReport.value = revenueRes.data
    }

    if (salesRes.success) {
      salesReport.value = salesRes.data
    }

    notification.success('Đã tải báo cáo thành công')
  } catch (error) {
    console.error('Error loading reports:', error)
    notification.error('Không thể tải báo cáo')
  } finally {
    loading.value = false
  }
}

function getCategoryPercentage(revenue) {
  if (maxCategoryRevenue.value === 0) return 0
  return (revenue / maxCategoryRevenue.value) * 100
}

function formatCurrency(amount) {
  if (!amount) return '0 đ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}
</script>
