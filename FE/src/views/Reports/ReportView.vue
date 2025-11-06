<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Báo cáo & Thống kê</h1>
        <p class="text-gray-600 mt-1">Báo cáo chi tiết và phân tích dữ liệu</p>
      </div>
    </div>

    <!-- Date Filter -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
          <input v-model="dateFrom" type="date" class="input-field" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
          <input v-model="dateTo" type="date" class="input-field" />
        </div>
        <div class="flex items-end">
          <button @click="loadReports" class="btn-primary w-full flex items-center justify-center gap-2">
            <span class="text-lg">📊</span>
            Tạo báo cáo
          </button>
        </div>
        <div class="flex items-end">
          <button @click="exportCSV" class="btn-secondary w-full flex items-center justify-center gap-2">
            <span class="text-lg">📥</span>
            Xuất CSV
          </button>
        </div>
        <div class="flex items-end">
          <button @click="exportPDF" class="btn-secondary w-full flex items-center justify-center gap-2 bg-red-600 hover:bg-red-700 text-white">
            <span class="text-lg">📄</span>
            Xuất PDF
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="loading-spinner"></div>
    </div>

    <div v-else class="space-y-6">
      <!-- Revenue Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="card bg-gradient-to-br from-green-500 to-green-600 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-green-100 text-sm">Tổng doanh thu</p>
              <p class="text-3xl font-bold mt-1">{{ formatCurrency(revenue.total) }}</p>
            </div>
            <span class="text-4xl opacity-50">💰</span>
          </div>
        </div>
        <div class="card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-blue-100 text-sm">Số đơn hàng</p>
              <p class="text-3xl font-bold mt-1">{{ revenue.orderCount || 0 }}</p>
            </div>
            <span class="text-4xl opacity-50">🛒</span>
          </div>
        </div>
        <div class="card bg-gradient-to-br from-purple-500 to-purple-600 text-white">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-purple-100 text-sm">Giá trị trung bình</p>
              <p class="text-3xl font-bold mt-1">{{ formatCurrency(revenue.average) }}</p>
            </div>
            <span class="text-4xl opacity-50">📊</span>
          </div>
        </div>
      </div>

      <!-- Top Dishes -->
      <div class="card">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-900">Top 10 món bán chạy</h3>
          <span class="text-sm text-gray-500">{{ topDishes.length }} món</span>
        </div>
        
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Hạng</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Món ăn</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Số lượng bán</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Doanh thu</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">% Tổng DT</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(dish, index) in topDishes" :key="dish.dishId" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center justify-center w-8 h-8 rounded-full" :class="getRankClass(index)">
                    {{ index + 1 }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-gradient-to-br from-red-500 to-red-600 rounded-lg flex items-center justify-center text-white">
                      🍲
                    </div>
                    <div>
                      <p class="text-sm font-medium text-gray-900">{{ dish.dishName }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-bold">
                  {{ dish.quantitySold }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-green-600">
                  {{ formatCurrency(dish.totalRevenue) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center gap-2">
                    <div class="flex-1 bg-gray-200 rounded-full h-2">
                      <div 
                        class="bg-red-600 h-2 rounded-full"
                        :style="{ width: getPercentage(dish.totalRevenue) + '%' }"
                      ></div>
                    </div>
                    <span class="text-sm text-gray-600 w-12 text-right">{{ getPercentage(dish.totalRevenue).toFixed(1) }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Empty state -->
        <div v-if="topDishes.length === 0" class="text-center py-12">
          <span class="text-8xl text-gray-300 block mb-4">📊</span>
          <p class="text-gray-500 text-lg">Không có dữ liệu trong khoảng thời gian này</p>
        </div>
      </div>

      <!-- Customer Stats -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="card">
          <h3 class="text-lg font-bold text-gray-900 mb-4">Thống kê khách hàng</h3>
          <div class="space-y-3">
            <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
              <span class="text-gray-700">Tổng khách hàng:</span>
              <span class="text-xl font-bold text-gray-900">{{ customerStats.total || 0 }}</span>
            </div>
            <div class="flex justify-between items-center p-3 bg-yellow-50 rounded-lg">
              <span class="text-gray-700">Khách VIP:</span>
              <span class="text-xl font-bold text-yellow-600">{{ customerStats.vip || 0 }}</span>
            </div>
            <div class="flex justify-between items-center p-3 bg-green-50 rounded-lg">
              <span class="text-gray-700">Khách thường:</span>
              <span class="text-xl font-bold text-green-600">{{ customerStats.regular || 0 }}</span>
            </div>
          </div>
        </div>

        <div class="card">
          <h3 class="text-lg font-bold text-gray-900 mb-4">Sử dụng bàn</h3>
          <div class="space-y-3">
            <div class="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
              <span class="text-gray-700">Tổng số bàn:</span>
              <span class="text-xl font-bold text-gray-900">{{ tableUsage.total || 0 }}</span>
            </div>
            <div class="flex justify-between items-center p-3 bg-red-50 rounded-lg">
              <span class="text-gray-700">Đang sử dụng:</span>
              <span class="text-xl font-bold text-red-600">{{ tableUsage.occupied || 0 }}</span>
            </div>
            <div class="flex justify-between items-center p-3 bg-green-50 rounded-lg">
              <span class="text-gray-700">Bàn trống:</span>
              <span class="text-xl font-bold text-green-600">{{ tableUsage.available || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reportService } from '@/services/reportService'
import { useNotificationStore } from '@/stores/notification'
// Heroicons replaced with emojis

const notification = useNotificationStore()

const loading = ref(false)
const dateFrom = ref('')
const dateTo = ref('')
const revenue = ref({ total: 0, average: 0, orderCount: 0 })
const topDishes = ref([])
const customerStats = ref({ total: 0, vip: 0, regular: 0 })
const tableUsage = ref({ total: 0, occupied: 0, available: 0 })

onMounted(() => {
  // Set default date range (last 30 days)
  const today = new Date()
  const thirtyDaysAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000)
  
  dateTo.value = today.toISOString().split('T')[0]
  dateFrom.value = thirtyDaysAgo.toISOString().split('T')[0]
  
  loadReports()
})

async function loadReports() {
  loading.value = true
  try {
    const [revenueRes, dishesRes, customerRes, tableRes] = await Promise.all([
      reportService.getRevenueSummary(dateFrom.value, dateTo.value),
      reportService.getTopDishes(dateFrom.value, dateTo.value, 10),
      reportService.getCustomerStats(),
      reportService.getTableUsage()
    ])

    if (revenueRes.success) revenue.value = revenueRes.data
    if (dishesRes.success) topDishes.value = dishesRes.data
    if (customerRes.success) customerStats.value = customerRes.data
    if (tableRes.success) tableUsage.value = tableRes.data
  } catch (error) {
    notification.error('Không thể tải báo cáo')
  } finally {
    loading.value = false
  }
}

function exportCSV() {
  try {
    // Create CSV content
    let csv = 'Hạng,Món ăn,Số lượng bán,Doanh thu,% Tổng DT\n'
    topDishes.value.forEach((dish, index) => {
      const percentage = getPercentage(dish.totalRevenue).toFixed(1)
      csv += `${index + 1},"${dish.dishName}",${dish.quantitySold},${dish.totalRevenue},${percentage}%\n`
    })
    
    // Add revenue summary
    csv += `\n\nTổng doanh thu,${revenue.value.total}\n`
    csv += `Số đơn hàng,${revenue.value.orderCount}\n`
    csv += `Giá trị trung bình,${revenue.value.average}\n`
    
    // Download file
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `bao-cao-${dateFrom.value}-${dateTo.value}.csv`
    link.click()
    
    notification.success('Đã xuất báo cáo CSV')
  } catch (error) {
    notification.error('Không thể xuất CSV')
  }
}

function exportPDF() {
  try {
    // Create HTML content for PDF
    let html = `
      <html>
      <head>
        <title>Báo cáo doanh thu</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; }
          h1 { color: #333; }
          table { width: 100%; border-collapse: collapse; margin-top: 20px; }
          th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
          th { background-color: #4CAF50; color: white; }
          .summary { margin-top: 30px; }
          .summary p { font-size: 16px; margin: 10px 0; }
        </style>
      </head>
      <body>
        <h1>Báo cáo doanh thu</h1>
        <p>Từ ngày: ${dateFrom.value} - Đến ngày: ${dateTo.value}</p>
        
        <div class="summary">
          <p><strong>Tổng doanh thu:</strong> ${formatCurrency(revenue.value.total)}</p>
          <p><strong>Số đơn hàng:</strong> ${revenue.value.orderCount}</p>
          <p><strong>Giá trị trung bình:</strong> ${formatCurrency(revenue.value.average)}</p>
        </div>
        
        <table>
          <thead>
            <tr>
              <th>Hạng</th>
              <th>Món ăn</th>
              <th>Số lượng bán</th>
              <th>Doanh thu</th>
              <th>% Tổng DT</th>
            </tr>
          </thead>
          <tbody>
    `
    
    topDishes.value.forEach((dish, index) => {
      const percentage = getPercentage(dish.totalRevenue).toFixed(1)
      html += `
        <tr>
          <td>${index + 1}</td>
          <td>${dish.dishName}</td>
          <td>${dish.quantitySold}</td>
          <td>${formatCurrency(dish.totalRevenue)}</td>
          <td>${percentage}%</td>
        </tr>
      `
    })
    
    html += `
          </tbody>
        </table>
      </body>
      </html>
    `
    
    // Open print dialog
    const printWindow = window.open('', '', 'height=600,width=800')
    printWindow.document.write(html)
    printWindow.document.close()
    printWindow.print()
    
    notification.success('Đã mở cửa sổ in PDF')
  } catch (error) {
    notification.error('Không thể xuất PDF')
  }
}

function getRankClass(index) {
  if (index === 0) return 'bg-yellow-500 text-white font-bold'
  if (index === 1) return 'bg-gray-400 text-white font-bold'
  if (index === 2) return 'bg-orange-600 text-white font-bold'
  return 'bg-gray-200 text-gray-700'
}

function getPercentage(value) {
  if (revenue.value.total === 0) return 0
  return (value / revenue.value.total) * 100
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value || 0)
}
</script>
