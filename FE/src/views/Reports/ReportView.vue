<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Báo cáo & Thống kê</h1>
        <p class="text-slate-600 mt-1 text-sm">Báo cáo doanh thu và đơn hàng</p>
      </div>
    </div>

    <!-- Filter Section -->
    <div class="bg-white border border-gray-200 rounded-lg p-4">
      <div class="flex flex-wrap items-end gap-4">
        <!-- Combobox -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-2">Loại lọc</label>
          <select 
            v-model="filterType" 
            @change="handleFilterChange"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
          >
            <option value="TODAY">Hôm nay</option>
            <option value="THIS_MONTH">Tháng hiện tại</option>
            <option value="THIS_YEAR">Năm nay</option>
            <option value="CUSTOM">Tùy chọn</option>
          </select>
        </div>
        
        <!-- Date pickers - chỉ hiện khi chọn "Tùy chọn" -->
        <template v-if="filterType === 'CUSTOM'">
          <div class="flex-1 min-w-[200px]">
            <label class="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
            <div class="relative">
              <input
                v-model="customStartDate"
                type="date"
                class="w-full px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
              />
              <i class="fas fa-calendar absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none"></i>
              <div v-if="!customStartDate" class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none">
                DD/MM/YYYY
              </div>
            </div>
          </div>
          
          <div class="flex-1 min-w-[200px]">
            <label class="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
            <div class="relative">
              <input
                v-model="customEndDate"
                type="date"
                class="w-full px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
              />
              <i class="fas fa-calendar absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none"></i>
              <div v-if="!customEndDate" class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none">
                DD/MM/YYYY
              </div>
            </div>
          </div>
        </template>
        
        <!-- Button Tạo báo cáo -->
        <div v-if="filterType === 'CUSTOM'" class="flex items-end">
          <button 
            @click="loadReport" 
            class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors"
          >
            <i class="fas fa-chart-bar"></i>
            <span>Tạo báo cáo</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <!-- Tổng doanh thu -->
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Tổng doanh thu</p>
            <p class="text-2xl font-bold text-slate-900">{{ formatCurrency(stats.totalRevenue) }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-search text-blue-600 text-xl"></i>
          </div>
        </div>
      </div>

      <!-- Tổng thực nhận -->
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Tổng thực nhận</p>
            <p class="text-2xl font-bold text-slate-900">{{ formatCurrency(stats.totalReceived) }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-search text-purple-600 text-xl"></i>
          </div>
        </div>
      </div>

      <!-- Số đơn hàng -->
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Số đơn hàng</p>
            <p class="text-2xl font-bold text-slate-900">{{ stats.totalOrders || 0 }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-search text-green-600 text-xl"></i>
          </div>
        </div>
      </div>

      <!-- Doanh thu trung bình -->
      <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-slate-500 text-xs font-medium mb-1">Doanh thu trung bình</p>
            <p class="text-2xl font-bold text-slate-900">{{ formatCurrency(stats.averageRevenue) }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-search text-orange-600 text-xl"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Charts -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Doanh thu theo ngày -->
      <div class="bg-white border border-gray-200 rounded-lg p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Doanh thu theo ngày</h3>
        <Line :data="revenueChartData" :options="chartOptions" />
      </div>

      <!-- Đơn hàng theo ngày -->
      <div class="bg-white border border-gray-200 rounded-lg p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Đơn hàng theo ngày</h3>
        <Bar :data="ordersChartData" :options="chartOptions" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Line, Bar } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend, Filler } from 'chart.js'
import { reportService } from '@/services/reportService'
import { useNotificationStore } from '@/stores/notification'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

const notification = useNotificationStore()

const loading = ref(false)
const filterType = ref('TODAY')
const customStartDate = ref('')
const customEndDate = ref('')
const stats = ref({
  totalRevenue: 0,
  totalReceived: 0,
  totalOrders: 0,
  averageRevenue: 0,
  revenueByDay: [],
  ordersByDay: []
})

// Chart options
const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      mode: 'index',
      intersect: false
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: function(value) {
          if (value >= 1000000) {
            return (value / 1000000).toFixed(1) + 'M'
          } else if (value >= 1000) {
            return (value / 1000).toFixed(0) + 'K'
          }
          return value
        }
      }
    }
  }
}

// Revenue chart data
const revenueChartData = computed(() => {
  const labels = stats.value.revenueByDay?.map(item => {
    const date = new Date(item.date)
    return `${date.getDate()}/${date.getMonth() + 1}`
  }) || []
  
  const data = stats.value.revenueByDay?.map(item => parseFloat(item.value) || 0) || []
  
  return {
    labels,
    datasets: [{
      label: 'Doanh thu',
      data,
      borderColor: 'rgb(59, 130, 246)',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      fill: true,
      tension: 0.4
    }]
  }
})

// Orders chart data
const ordersChartData = computed(() => {
  const labels = stats.value.ordersByDay?.map(item => {
    const date = new Date(item.date)
    return `${date.getDate()}/${date.getMonth() + 1}`
  }) || []
  
  const data = stats.value.ordersByDay?.map(item => parseFloat(item.value) || 0) || []
  
  return {
    labels,
    datasets: [{
      label: 'Số đơn hàng',
      data,
      backgroundColor: 'rgba(34, 197, 94, 0.6)',
      borderColor: 'rgb(34, 197, 94)',
      borderWidth: 1
    }]
  }
})

function formatCurrency(value) {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}

function handleFilterChange() {
  if (filterType.value !== 'CUSTOM') {
    // Tự động load khi chọn các option khác
    loadReport()
  } else {
    // Reset custom dates khi chọn "Tùy chọn"
    customStartDate.value = ''
    customEndDate.value = ''
  }
}

async function loadReport() {
  if (filterType.value === 'CUSTOM' && (!customStartDate.value || !customEndDate.value)) {
    notification.error('Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc')
    return
  }
  
  loading.value = true
  try {
    const response = await reportService.getReportStats(
      filterType.value,
      customStartDate.value || null,
      customEndDate.value || null
    )
    
    if (response.success && response.data) {
      stats.value = response.data
    } else {
      notification.error(response.message || 'Không thể tải báo cáo')
    }
  } catch (error) {
    console.error('Error loading report:', error)
    notification.error('Không thể tải báo cáo')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReport()
})
</script>
