<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Báo cáo thống kê</h1>
        <p class="text-gray-600 mt-1">Xem báo cáo doanh thu và bán hàng</p>
      </div>
    </div>

    <!-- Filter Section -->
    <div class="card">
      <div class="flex flex-wrap items-end gap-4">
        <!-- Combobox -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-2">Loại lọc</label>
          <select v-model="filterType" @change="handleFilterChange"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
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
              <input v-model="filters.startDate" type="date" :min="getMinDate()" :max="getMaxDate()"
                @change="validateDateRange"
                class="w-full px-4 py-2 pr-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition" />
              <!-- <i class="fas fa-calendar absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none"></i>
              <div v-if="!filters.startDate" class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none">
                DD/MM/YYYY
              </div> -->
            </div>
          </div>

          <div class="flex-1 min-w-[200px]">
            <label class="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
            <div class="relative">
              <input v-model="filters.endDate" type="date" :min="getMinEndDate()" :max="getMaxEndDate()"
                @change="validateDateRange"
                class="w-full px-4 py-2 pr-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition" />
              <!-- <i class="fas fa-calendar absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none"></i>
              <div v-if="!filters.endDate" class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none">
                DD/MM/YYYY
              </div> -->
            </div>
          </div>
        </template>

        <!-- Button Tạo báo cáo -->
        <div v-if="filterType === 'CUSTOM'" class="flex items-end">
          <button @click="loadReports"
            class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors">
            <i class="fas fa-chart-bar"></i>
            <span>Tạo báo cáo</span>
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
      <div v-if="(filterType !== 'CUSTOM' || (filters.startDate && filters.endDate))" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
        <div class="relative card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
          <div class="flex items-center justify-between">
            <div class="overflow-hidden overflow-x-auto">
              <p class="text-blue-100 text-sm">Tổng doanh thu</p>
              <p class="text-3xl font-bold mt-1 z-1!">{{ formatCurrency(revenueReport.totalRevenue) }}</p>
            </div>
            <!-- <div class="text-5xl opacity-50 absolute left-1/2 top-1/2 translate-x-1/2 -translate-y-1/2">💰</div> -->
          </div>
        </div>

        <div class="relative card bg-gradient-to-br from-purple-500 to-purple-600 text-white">
          <div class="flex items-center justify-between">
            <div class="overflow-hidden overflow-x-auto">
              <p class="text-purple-100 text-sm">Tổng đã giảm</p>
              <p class="text-3xl font-bold mt-1">{{ formatCurrency(stats.totalDiscount) }}</p>
            </div>
            <!-- <div class="text-5xl opacity-50  absolute left-1/2 top-1/2 translate-x-1/2 -translate-y-1/2">💵</div> -->
          </div>
        </div>

        <div class="relative card bg-gradient-to-br from-green-500 to-green-600 text-white">
          <div class="flex items-center justify-between">
            <div class="overflow-hidden overflow-x-auto">
              <p class="text-green-100 text-sm">Số đơn hàng</p>
              <p class="text-3xl font-bold mt-1">{{ revenueReport.totalOrders }}</p>
            </div>
            <!-- <div class="text-5xl opacity-50  absolute left-1/2 top-1/2 translate-x-1/2 -translate-y-1/2">🛒</div> -->
          </div>
        </div>

        <div class="relative card bg-gradient-to-br from-orange-500 to-orange-600 text-white">
          <div class="flex items-center justify-between">
            <div class="overflow-hidden overflow-x-auto">
              <p class="text-orange-100 text-sm">Doanh thu trung bình</p>
              <p class="text-3xl font-bold mt-1">{{ formatCurrency(revenueReport.averageOrderValue) }}</p>
            </div>
            <!-- <div class="text-5xl opacity-50  absolute left-1/2 top-1/2 translate-x-1/2 -translate-y-1/2">📈</div> -->
          </div>
        </div>
      </div>

      <!-- Charts - chỉ hiện khi không phải TODAY -->
      <div v-if="
        filterType !== 'TODAY' &&
        (filterType !== 'CUSTOM' || (filters.startDate && filters.endDate))" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Doanh thu chart -->
        <div class="card">
          <h2 class="text-xl font-bold text-gray-900 mb-4">
            {{ filterType === 'THIS_YEAR' ? 'Doanh thu theo tháng' : 'Doanh thu theo ngày' }}
          </h2>
          <div style="height: 300px;">
            <Line :data="revenueChartData" :options="chartOptions" />
          </div>
        </div>

        <!-- Đơn hàng chart -->
        <div class="card">
          <h2 class="text-xl font-bold text-gray-900 mb-4">
            {{ filterType === 'THIS_YEAR' ? 'Đơn hàng theo tháng' : 'Đơn hàng theo ngày' }}
          </h2>
          <div style="height: 300px;">
            <Bar :data="ordersChartData" :options="chartOptions" />
          </div>
        </div>
      </div>

      <!-- Daily Revenue Chart -->
      <div v-if="(filterType !== 'CUSTOM' || (filters.startDate && filters.endDate))" class="card">
        <h2 class="text-xl font-bold text-gray-900 mb-4">Chi tiết</h2>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase"></th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Số đơn</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Doanh thu</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Đã giảm</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="day in revenueReport.dailyRevenue" :key="day.date">
                <td class="px-6 py-4 text-sm text-gray-900">{{ formatDateForTable(day.date) }}</td>
                <td class="px-6 py-4 text-sm text-gray-900 text-right">{{ day.orderCount }}</td>
                <td class="px-6 py-4 text-sm font-medium text-primary-600 text-right">
                  {{ formatCurrency(day.revenue) }}
                </td>
                <td class="px-6 py-4 text-sm font-medium text-purple-600 text-right">
                  {{ formatCurrency(day.discount) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
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
const revenueReport = ref(null)
const filterType = ref('TODAY')
const stats = ref({
  totalRevenue: 0,
  totalDiscount: 0,
  totalOrders: 0,
  averageRevenue: 0,
  revenueByDay: [],
  ordersByDay: [],
  discountByDay: []
})

const filters = reactive({
  startDate: '',
  endDate: ''
})


function handleFilterChange() {
  if (filterType.value !== 'CUSTOM') {
    // Tự động load khi chọn các option khác
    loadReports()
  } else {
    // Reset custom dates khi chọn "Tùy chọn"
    console.log("acbcb")
    filters.startDate = ''
    filters.endDate = ''
  }
}

function getMinDate() {
  // Không giới hạn min date
  return null
}

function getMaxDate() {
  // Max date là 31 ngày từ hôm nay
  const maxDate = new Date()
  maxDate.setDate(maxDate.getDate() + 31)
  return maxDate.toISOString().split('T')[0]
}

function getMinEndDate() {
  // Min end date là startDate + 1 ngày
  if (!filters.startDate) return null
  const minDate = new Date(filters.startDate)
  minDate.setDate(minDate.getDate() + 1)
  return minDate.toISOString().split('T')[0]
}

function getMaxEndDate() {
  // Max end date là startDate + 31 ngày
  if (!filters.startDate) return getMaxDate()
  const maxDate = new Date(filters.startDate)
  maxDate.setDate(maxDate.getDate() + 31)
  return maxDate.toISOString().split('T')[0]
}

function validateDateRange() {
  if (!filters.startDate || !filters.endDate) return

  const start = new Date(filters.startDate)
  const end = new Date(filters.endDate)
  const diffTime = Math.abs(end - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays < 1) {
    notification.error('Khoảng ngày phải tối thiểu 1 ngày')
    filters.endDate = new Date(start.getTime() + 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    return
  }

  if (diffDays > 31) {
    notification.error('Khoảng ngày không được vượt quá 31 ngày')
    const maxEndDate = new Date(start.getTime() + 31 * 24 * 60 * 60 * 1000)
    filters.endDate = maxEndDate.toISOString().split('T')[0]
    return
  }
}

async function loadReports() {
  // Nếu là CUSTOM và chưa chọn đủ ngày
  if (filterType.value === 'CUSTOM' && (!filters.startDate || !filters.endDate)) {
    notification.error('Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc')
    return
  }

  // Validate date range
  if (filterType.value === 'CUSTOM' && filters.startDate && filters.endDate) {
    const start = new Date(filters.startDate)
    const end = new Date(filters.endDate)
    const diffTime = Math.abs(end - start)
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

    if (diffDays < 1) {
      notification.error('Khoảng ngày phải tối thiểu 1 ngày')
      return
    }

    if (diffDays > 31) {
      notification.error('Khoảng ngày không được vượt quá 31 ngày')
      return
    }
  }

  loading.value = true
  try {
    // Lấy stats từ API /reports/stats
    const statsRes = await reportService.getReportStats(
      filterType.value,
      filters.startDate || null,
      filters.endDate || null
    )

    if (statsRes.success && statsRes.data) {
      // Debug: Kiểm tra toàn bộ response
      console.log('Full statsRes:', JSON.parse(JSON.stringify(statsRes)))
      console.log('statsRes.data:', JSON.parse(JSON.stringify(statsRes.data)))

      stats.value = statsRes.data

      // Debug: Kiểm tra dữ liệu từ backend
      console.log('Stats from backend:', JSON.parse(JSON.stringify(stats.value)))
      console.log('discountByDay:', stats.value.discountByDay)
      console.log('totalDiscount:', stats.value.totalDiscount)

      // Đảm bảo discountByDay và totalDiscount có giá trị mặc định nếu undefined
      if (!stats.value.discountByDay) {
        stats.value.discountByDay = []
        console.warn('discountByDay is missing, setting to empty array')
      }
      if (stats.value.totalDiscount === undefined || stats.value.totalDiscount === null) {
        stats.value.totalDiscount = 0
        console.warn('totalDiscount is missing, setting to 0')
      }

      // Xử lý dữ liệu theo filterType
      // Normalize date format từ backend (có thể là string hoặc date object)
      const normalizeDate = (dateInput) => {
        if (!dateInput) return null
        if (typeof dateInput === 'string') {
          return dateInput.split('T')[0] // Lấy phần YYYY-MM-DD
        }
        return new Date(dateInput).toISOString().split('T')[0]
      }

      let processedRevenueData = (stats.value.revenueByDay || []).map(item => ({
        date: normalizeDate(item.date),
        value: parseFloat(item.value || 0)
      }))
      let processedOrdersData = (stats.value.ordersByDay || []).map(item => ({
        date: normalizeDate(item.date),
        value: parseFloat(item.value || 0)
      }))

      // Nếu là THIS_YEAR, group theo tháng
      if (filterType.value === 'THIS_YEAR') {
        const revenueByMonth = new Map()
        const ordersByMonth = new Map()

        processedRevenueData.forEach(item => {
          const date = new Date(item.date)
          const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
          const monthLabel = `${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`

          if (!revenueByMonth.has(monthKey)) {
            revenueByMonth.set(monthKey, { date: monthKey, label: monthLabel, value: 0 })
          }
          revenueByMonth.get(monthKey).value = parseFloat(revenueByMonth.get(monthKey).value) + parseFloat(item.value || 0)
        })

        processedOrdersData.forEach(item => {
          const date = new Date(item.date)
          const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
          const monthLabel = `${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`

          if (!ordersByMonth.has(monthKey)) {
            ordersByMonth.set(monthKey, { date: monthKey, label: monthLabel, value: 0 })
          }
          ordersByMonth.get(monthKey).value = parseFloat(ordersByMonth.get(monthKey).value) + parseFloat(item.value || 0)
        })

        processedRevenueData = Array.from(revenueByMonth.values()).sort((a, b) => a.date.localeCompare(b.date))
        processedOrdersData = Array.from(ordersByMonth.values()).sort((a, b) => a.date.localeCompare(b.date))
      }

      // Xử lý discountByDay - normalize date và tạo map để dễ tìm kiếm
      const discountByDateMap = new Map()
      const processedDiscountData = []

      if (filterType.value === 'THIS_YEAR') {
        // Group discount theo tháng cho THIS_YEAR
        const discountByMonth = new Map()
        stats.value.discountByDay?.forEach(item => {
          const dateStr = normalizeDate(item.date)
          if (!dateStr) return
          const date = new Date(dateStr)
          const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
          const monthLabel = `${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`

          if (!discountByMonth.has(monthKey)) {
            discountByMonth.set(monthKey, { date: monthKey, label: monthLabel, value: 0 })
          }
          discountByMonth.get(monthKey).value = parseFloat(discountByMonth.get(monthKey).value) + parseFloat(item.value || 0)
        })
        processedDiscountData.push(...Array.from(discountByMonth.values()).sort((a, b) => a.date.localeCompare(b.date)))
      } else {
        // Với các filter khác, normalize date và tạo map
        stats.value.discountByDay?.forEach(item => {
          const dateStr = normalizeDate(item.date)
          if (!dateStr) return
          const discountValue = parseFloat(item.value || 0)
          discountByDateMap.set(dateStr, discountValue)
          processedDiscountData.push({ date: dateStr, value: discountValue })
        })
      }

      // Debug: Kiểm tra mapping
      console.log('discountByDateMap:', Array.from(discountByDateMap.entries()))
      console.log('processedRevenueData:', processedRevenueData.slice(0, 5))

      // Cập nhật revenueReport từ stats
      revenueReport.value = {
        totalRevenue: stats.value.totalRevenue,
        totalOrders: stats.value.totalOrders,
        averageOrderValue: stats.value.averageRevenue,
        dailyRevenue: processedRevenueData.map(item => {
          // Tìm discount tương ứng
          let discount = 0
          if (filterType.value === 'THIS_YEAR') {
            // Với THIS_YEAR, match theo monthKey
            discount = processedDiscountData.find(d => d.date === item.date)?.value || 0
          } else {
            // Với các filter khác, match theo date string
            discount = discountByDateMap.get(item.date) || 0
          }

          // Tìm orderCount
          const orderCount = processedOrdersData.find(o => o.date === item.date)?.value || 0

          return {
            date: item.date,
            revenue: item.value,
            discount: discount,
            orderCount: orderCount
          }
        })
      }

      // Debug: Kiểm tra kết quả
      console.log('dailyRevenue with discount:', revenueReport.value.dailyRevenue.slice(0, 5))
    }


    notification.success('Đã tải báo cáo thành công')
  } catch (error) {
    console.error('Error loading reports:', error)
    notification.error('Không thể tải báo cáo')
  } finally {
    loading.value = false
  }
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
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

function formatDateForTable(dateString) {
  const date = new Date(dateString)
  // Nếu là THIS_YEAR, chỉ hiện tháng/năm
  if (filterType.value === 'THIS_YEAR') {
    return `${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`
  }
  // Các trường hợp khác hiện ngày đầy đủ
  return formatDate(dateString)
}

// Chart options
const chartOptions = computed(() => ({
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
        callback: function (value) {
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
}))

// Revenue chart data
const revenueChartData = computed(() => {
  if (!stats.value.revenueByDay || stats.value.revenueByDay.length === 0) {
    return { labels: [], datasets: [] }
  }

  let labels = []
  let data = []

  if (filterType.value === 'THIS_YEAR') {
    // Group by month
    const revenueByMonth = new Map()
    stats.value.revenueByDay.forEach(item => {
      const date = new Date(item.date)
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      const monthLabel = `${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`

      if (!revenueByMonth.has(monthKey)) {
        revenueByMonth.set(monthKey, { label: monthLabel, value: 0 })
      }
      revenueByMonth.get(monthKey).value = revenueByMonth.get(monthKey).value + parseFloat(item.value || 0)
    })

    labels = Array.from(revenueByMonth.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([key, item]) => item.label)
    data = Array.from(revenueByMonth.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([key, item]) => item.value)
  } else {
    // By day
    labels = stats.value.revenueByDay.map(item => {
      const date = new Date(item.date)
      return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}`
    })
    data = stats.value.revenueByDay.map(item => parseFloat(item.value) || 0)
  }

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
  if (!stats.value.ordersByDay || stats.value.ordersByDay.length === 0) {
    return { labels: [], datasets: [] }
  }

  let labels = []
  let data = []

  if (filterType.value === 'THIS_YEAR') {
    // Group by month
    const ordersByMonth = new Map()
    stats.value.ordersByDay.forEach(item => {
      const date = new Date(item.date)
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      const monthLabel = `${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`

      if (!ordersByMonth.has(monthKey)) {
        ordersByMonth.set(monthKey, { label: monthLabel, value: 0 })
      }
      ordersByMonth.get(monthKey).value = ordersByMonth.get(monthKey).value + parseFloat(item.value || 0)
    })

    labels = Array.from(ordersByMonth.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([key, item]) => item.label)
    data = Array.from(ordersByMonth.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([key, item]) => item.value)
  } else {
    // By day
    labels = stats.value.ordersByDay.map(item => {
      const date = new Date(item.date)
      return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}`
    })
    data = stats.value.ordersByDay.map(item => parseFloat(item.value) || 0)
  }

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

onMounted(() => {
  // Tự động load báo cáo khi vào trang với filterType mặc định
  loadReports()
})
</script>
