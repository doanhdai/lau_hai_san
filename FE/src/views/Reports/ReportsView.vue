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
      <div class="space-y-4">
        <!-- Quick Filter Buttons -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-3">Lọc nhanh</label>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in quickFilterOptions"
              :key="option.value"
              @click="selectQuickFilter(option.value)"
              :class="[
                'px-4 py-2 rounded-lg font-medium text-sm transition-all',
                filterType === option.value
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              <i :class="['mr-2', option.icon]"></i>
              {{ option.label }}
            </button>
            <button
              @click="selectQuickFilter('CUSTOM')"
              :class="[
                'px-4 py-2 rounded-lg font-medium text-sm transition-all',
                filterType === 'CUSTOM'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              <i class="fas fa-calendar-alt mr-2"></i>
              Tùy chọn
            </button>
          </div>
        </div>

        <!-- Date Range Picker - Hiển thị cho tất cả các option -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-gray-200">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              <i class="fas fa-calendar-alt mr-2 text-blue-600"></i>
              Từ ngày
            </label>
            <div class="relative">
              <input
                v-model="filters.startDate"
                type="date"
                :min="getMinDate()"
                :max="getMaxDate()"
                @change="handleDateChange"
                class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition shadow-sm"
                placeholder="Chọn ngày bắt đầu"
              />
              <i class="fas fa-calendar absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none"></i>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              <i class="fas fa-calendar-check mr-2 text-green-600"></i>
              Đến ngày
            </label>
            <div class="relative">
              <input
                v-model="filters.endDate"
                type="date"
                :min="getMinEndDate()"
                :max="getMaxEndDate()"
                @change="handleDateChange"
                class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition shadow-sm"
                placeholder="Chọn ngày kết thúc"
              />
              <i class="fas fa-calendar absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none"></i>
            </div>
          </div>

          <div class="flex items-end">
            <button
              @click="loadReports"
              :disabled="!canLoadReport"
              class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors shadow-md"
            >
              <i class="fas fa-chart-bar"></i>
              <span>Tạo báo cáo</span>
            </button>
          </div>
        </div>

        <!-- Date Range Info -->
        <div v-if="filters.startDate && filters.endDate" class="flex items-center gap-2 text-sm text-gray-600 bg-blue-50 px-4 py-2 rounded-lg">
          <i class="fas fa-info-circle text-blue-600"></i>
          <span>
            Khoảng thời gian: <strong>{{ formatDateDisplay(filters.startDate) }}</strong> đến <strong>{{ formatDateDisplay(filters.endDate) }}</strong>
            <span class="ml-2 text-blue-600">({{ getDateRangeDays() }} ngày)</span>
          </span>
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
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Số đơn</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Doanh thu</th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Đã giảm</th>
                <th class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Thao tác</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <template v-for="day in revenueReport.dailyRevenue" :key="day.date">
                <tr 
                  @click="toggleDayOrders(day.date)"
                  class="cursor-pointer hover:bg-blue-50 transition-colors"
                >
                  <td class="px-6 py-4 text-sm text-gray-900 font-medium">
                    <div class="flex items-center gap-2">
                      <i :class="['fas', expandedDays.has(day.date) ? 'fa-chevron-down' : 'fa-chevron-right', 'text-blue-600']"></i>
                      {{ formatDateForTable(day.date) }}
                    </div>
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900 text-right">{{ day.orderCount }}</td>
                  <td class="px-6 py-4 text-sm font-medium text-primary-600 text-right">
                    {{ formatCurrency(day.revenue) }}
                  </td>
                  <td class="px-6 py-4 text-sm font-medium text-purple-600 text-right">
                    {{ formatCurrency(day.discount) }}
                  </td>
                  <td class="px-6 py-4 text-center">
                    <button
                      @click.stop="viewDayOrders(day.date)"
                      class="text-blue-600 hover:text-blue-800 font-medium text-sm flex items-center justify-center gap-1"
                    >
                      <i class="fas fa-eye"></i>
                      <span>Xem đơn</span>
                    </button>
                  </td>
                </tr>
                <!-- Expanded Orders List -->
                <tr v-if="expandedDays.has(day.date)">
                  <td colspan="5" class="px-6 py-4 bg-gray-50">
                    <div v-if="loadingOrders[day.date]" class="text-center py-4">
                      <div class="inline-block w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                      <p class="mt-2 text-sm text-gray-600">Đang tải đơn hàng...</p>
                    </div>
                    <div v-else-if="dayOrders[day.date] && dayOrders[day.date].length > 0" class="space-y-3">
                      <h4 class="font-semibold text-gray-900 mb-3">
                        Danh sách đơn hàng ngày {{ formatDateForTable(day.date) }} ({{ dayOrders[day.date].length }} đơn)
                      </h4>
                      <div class="overflow-x-auto">
                        <table class="min-w-full divide-y divide-gray-200 bg-white rounded-lg">
                          <thead class="bg-gray-100">
                            <tr>
                              <th class="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Mã đơn</th>
                              <th class="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Khách hàng</th>
                              <th class="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Bàn</th>
                              <th class="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Trạng thái</th>
                              <th class="px-4 py-2 text-right text-xs font-medium text-gray-700 uppercase">Tổng tiền</th>
                              <th class="px-4 py-2 text-left text-xs font-medium text-gray-700 uppercase">Thời gian</th>
                            </tr>
                          </thead>
                          <tbody class="divide-y divide-gray-200">
                            <tr 
                              v-for="order in dayOrders[day.date]" 
                              :key="order.id"
                              class="hover:bg-gray-50"
                            >
                              <td class="px-4 py-3 text-sm font-mono font-semibold text-blue-600">
                                #{{ order.orderNumber || order.id }}
                              </td>
                              <td class="px-4 py-3 text-sm text-gray-900">
                                {{ order.customerName || 'Khách vãng lai' }}
                              </td>
                              <td class="px-4 py-3 text-sm text-gray-700">
                                <span v-if="order.tableNumber">Bàn {{ order.tableNumber }}</span>
                                <span v-else-if="order.tableId">Đã phân bàn</span>
                                <span v-else class="text-gray-400">-</span>
                              </td>
                              <td class="px-4 py-3 text-sm">
                                <span :class="getStatusBadgeClass(order.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                                  {{ getStatusText(order.status) }}
                                </span>
                              </td>
                              <td class="px-4 py-3 text-sm font-semibold text-green-600 text-right">
                                {{ formatCurrency(order.total) }}
                              </td>
                              <td class="px-4 py-3 text-sm text-gray-600">
                                {{ formatDateTime(order.createdAt) }}
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </div>
                    <div v-else class="text-center py-4 text-gray-500 text-sm">
                      Không có đơn hàng nào trong ngày này
                    </div>
                  </td>
                </tr>
              </template>
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
import { orderService } from '@/services/orderService'
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

// Orders by day
const expandedDays = ref(new Set())
const dayOrders = ref({})
const loadingOrders = ref({})

// Quick filter options
const quickFilterOptions = [
  { value: 'TODAY', label: 'Hôm nay', icon: 'fas fa-calendar-day' },
  // { value: 'YESTERDAY', label: 'Hôm qua', icon: 'fas fa-calendar-minus' },
  { value: 'LAST_7_DAYS', label: '7 ngày qua', icon: 'fas fa-calendar-week' },
  { value: 'LAST_30_DAYS', label: '30 ngày qua', icon: 'fas fa-calendar-alt' },
  { value: 'THIS_WEEK', label: 'Tuần này', icon: 'fas fa-calendar' },
  { value: 'LAST_WEEK', label: 'Tuần trước', icon: 'fas fa-calendar' },
  { value: 'THIS_MONTH', label: 'Tháng này', icon: 'fas fa-calendar' },
  { value: 'LAST_MONTH', label: 'Tháng trước', icon: 'fas fa-calendar' },
  { value: 'THIS_YEAR', label: 'Năm nay', icon: 'fas fa-calendar' }
]

// Computed để kiểm tra có thể load report không
const canLoadReport = computed(() => {
  if (filterType.value === 'CUSTOM') {
    return filters.startDate && filters.endDate && validateDateRangeSilent()
  }
  return true
})


function selectQuickFilter(type) {
  filterType.value = type
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  switch (type) {
    case 'TODAY':
      filters.startDate = today.toISOString().split('T')[0]
      filters.endDate = today.toISOString().split('T')[0]
      break
    case 'YESTERDAY':
      const yesterday = new Date(today)
      yesterday.setDate(yesterday.getDate() - 1)
      filters.startDate = yesterday.toISOString().split('T')[0]
      filters.endDate = yesterday.toISOString().split('T')[0]
      break
    case 'LAST_7_DAYS':
      const last7Days = new Date(today)
      last7Days.setDate(last7Days.getDate() - 6)
      filters.startDate = last7Days.toISOString().split('T')[0]
      filters.endDate = today.toISOString().split('T')[0]
      break
    case 'LAST_30_DAYS':
      const last30Days = new Date(today)
      last30Days.setDate(last30Days.getDate() - 29)
      filters.startDate = last30Days.toISOString().split('T')[0]
      filters.endDate = today.toISOString().split('T')[0]
      break
    case 'THIS_WEEK':
      const thisWeekStart = new Date(today)
      thisWeekStart.setDate(today.getDate() - today.getDay())
      filters.startDate = thisWeekStart.toISOString().split('T')[0]
      filters.endDate = today.toISOString().split('T')[0]
      break
    case 'LAST_WEEK':
      const lastWeekStart = new Date(today)
      lastWeekStart.setDate(today.getDate() - today.getDay() - 7)
      const lastWeekEnd = new Date(lastWeekStart)
      lastWeekEnd.setDate(lastWeekStart.getDate() + 6)
      filters.startDate = lastWeekStart.toISOString().split('T')[0]
      filters.endDate = lastWeekEnd.toISOString().split('T')[0]
      break
    case 'THIS_MONTH':
      const thisMonthStart = new Date(today.getFullYear(), today.getMonth(), 1)
      filters.startDate = thisMonthStart.toISOString().split('T')[0]
      filters.endDate = today.toISOString().split('T')[0]
      break
    case 'LAST_MONTH':
      const lastMonthStart = new Date(today.getFullYear(), today.getMonth() - 1, 1)
      const lastMonthEnd = new Date(today.getFullYear(), today.getMonth(), 0)
      filters.startDate = lastMonthStart.toISOString().split('T')[0]
      filters.endDate = lastMonthEnd.toISOString().split('T')[0]
      break
    case 'THIS_YEAR':
      const thisYearStart = new Date(today.getFullYear(), 0, 1)
      filters.startDate = thisYearStart.toISOString().split('T')[0]
      filters.endDate = today.toISOString().split('T')[0]
      break
    case 'CUSTOM':
      // Không tự động set dates, để user tự chọn
      if (!filters.startDate || !filters.endDate) {
        filters.startDate = ''
        filters.endDate = ''
      }
      return // Không tự động load
  }

  // Tự động load sau khi set dates (trừ CUSTOM)
  if (type !== 'CUSTOM') {
    setTimeout(() => {
      loadReports()
    }, 100)
  }
}

function handleDateChange() {
  if (filters.startDate && filters.endDate) {
    validateDateRange()
    // Nếu đã chọn đủ 2 ngày và hợp lệ, tự động load
    if (validateDateRangeSilent()) {
      filterType.value = 'CUSTOM'
    }
  }
}

function getMinDate() {
  // Cho phép chọn từ 1 năm trước
  const minDate = new Date()
  minDate.setFullYear(minDate.getFullYear() - 1)
  return minDate.toISOString().split('T')[0]
}

function getMaxDate() {
  // Cho phép chọn đến hôm nay
  return new Date().toISOString().split('T')[0]
}

function getMinEndDate() {
  // Min end date là startDate (cho phép chọn cùng ngày)
  if (!filters.startDate) return getMinDate()
  return filters.startDate
}

function getMaxEndDate() {
  // Max end date là hôm nay hoặc startDate + 365 ngày (tùy cái nào nhỏ hơn)
  const today = new Date().toISOString().split('T')[0]
  if (!filters.startDate) return today
  
  const maxDate = new Date(filters.startDate)
  maxDate.setDate(maxDate.getDate() + 365)
  const maxDateStr = maxDate.toISOString().split('T')[0]
  
  return maxDateStr < today ? maxDateStr : today
}

function validateDateRange() {
  if (!filters.startDate || !filters.endDate) return false

  const start = new Date(filters.startDate)
  const end = new Date(filters.endDate)
  
  // Đảm bảo startDate <= endDate
  if (start > end) {
    notification.error('Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc')
    filters.endDate = filters.startDate
    return false
  }

  const diffTime = Math.abs(end - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1 // +1 vì tính cả 2 ngày

  // Không giới hạn số ngày, chỉ cần đảm bảo startDate <= endDate
  return true
}

function validateDateRangeSilent() {
  if (!filters.startDate || !filters.endDate) return false
  const start = new Date(filters.startDate)
  const end = new Date(filters.endDate)
  return start <= end
}

function getDateRangeDays() {
  if (!filters.startDate || !filters.endDate) return 0
  const start = new Date(filters.startDate)
  const end = new Date(filters.endDate)
  const diffTime = Math.abs(end - start)
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
}

function formatDateDisplay(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

async function loadReports() {
  // Validate: Phải có đủ startDate và endDate
  if (!filters.startDate || !filters.endDate) {
    notification.error('Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc')
    return
  }

  // Validate date range
  if (!validateDateRangeSilent()) {
    notification.error('Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc')
    return
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

function formatDateTime(dateTime) {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return ''
  }
}

function getStatusBadgeClass(status) {
  const classes = {
    PENDING: 'bg-amber-100 text-amber-800',
    CONFIRMED: 'bg-green-100 text-green-800',
    PREPARING: 'bg-blue-100 text-blue-800',
    SERVED: 'bg-purple-100 text-purple-800',
    COMPLETED: 'bg-gray-100 text-gray-800',
    CANCELLED: 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

function getStatusText(status) {
  const texts = {
    PENDING: 'Chờ xác nhận',
    CONFIRMED: 'Đã xác nhận',
    PREPARING: 'Đang chuẩn bị',
    SERVED: 'Đã phục vụ',
    COMPLETED: 'Hoàn thành',
    CANCELLED: 'Đã hủy'
  }
  return texts[status] || status
}

async function toggleDayOrders(date) {
  if (expandedDays.value.has(date)) {
    expandedDays.value.delete(date)
  } else {
    expandedDays.value.add(date)
    await loadDayOrders(date)
  }
}

async function viewDayOrders(date) {
  if (!expandedDays.value.has(date)) {
    expandedDays.value.add(date)
  }
  await loadDayOrders(date)
}

async function loadDayOrders(date) {
  // Nếu đã load rồi thì không load lại
  if (dayOrders.value[date]) {
    return
  }

  loadingOrders.value[date] = true
  try {
    // Tạo startDate và endDate cho ngày đó (00:00:00 đến 23:59:59)
    const dateObj = new Date(date)
    const startDate = new Date(dateObj)
    startDate.setHours(0, 0, 0, 0)
    
    const endDate = new Date(dateObj)
    endDate.setHours(23, 59, 59, 999)

    // Format theo ISO 8601 cho API
    const startDateStr = startDate.toISOString()
    const endDateStr = endDate.toISOString()

    const response = await orderService.getByDate(startDateStr, endDateStr)
    
    let orders = []
    if (response && response.success && response.data) {
      orders = Array.isArray(response.data) ? response.data : []
    } else if (Array.isArray(response)) {
      orders = response
    } else if (response && response.data && Array.isArray(response.data)) {
      orders = response.data
    }

    // Sort by createdAt descending
    orders.sort((a, b) => {
      const dateA = new Date(a.createdAt || 0)
      const dateB = new Date(b.createdAt || 0)
      return dateB - dateA
    })

    dayOrders.value[date] = orders
  } catch (error) {
    console.error(`Error loading orders for date ${date}:`, error)
    notification.error('Không thể tải danh sách đơn hàng')
    dayOrders.value[date] = []
  } finally {
    loadingOrders.value[date] = false
  }
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
  // Tự động set và load báo cáo khi vào trang với filterType mặc định (TODAY)
  selectQuickFilter('TODAY')
})
</script>
