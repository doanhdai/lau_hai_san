<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Quản lý Đơn hàng</h1>
        <p class="text-slate-600 mt-1 text-sm">Danh sách và trạng thái đơn hàng</p>
      </div>
      <!-- <router-link to="/admin/orders/create" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
        <i class="fas fa-plus"></i>
        <span>Tạo đơn hàng mới</span>
      </router-link> -->
    </div>

    <!-- Filters -->
    <div class="bg-white border border-gray-200 rounded-lg p-4">
      <div class="items-center grid grid-cols-1 md:grid-cols-[1fr_1fr_1fr_auto] gap-4 items-end">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
            <option value="">Tất cả</option>
            <option value="PENDING">Chờ xử lý</option>
            <option value="CONFIRMED">Đã xác nhận</option>
            <option value="PREPARING">Đang chuẩn bị</option>
            <option value="SERVED">Đã phục vụ</option>
            <option value="COMPLETED">Hoàn thành</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Từ ngày</label>
          <input v-model="fromDate" type="date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition" />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Đến ngày</label>
          <input v-model="toDate" type="date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition" />
        </div>
        <div class="flex gap-2 flex-col">
          <button @click="applyFilter" class="bg-slate-900 hover:bg-slate-800 text-white px-2 py-1 rounded-lg font-medium flex items-center gap-2 transition-colors flex-1">
            <i class="fas fa-filter"></i>
            <span>Lọc</span>
          </button>
          <button @click="resetFilter" class="bg-gray-100 hover:bg-gray-200 text-slate-700 px-2 py-1 rounded-lg font-medium flex items-center gap-2 transition-colors flex-1">
            <i class="fas fa-redo"></i>
            <span>Reset</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Orders Table -->
    <div v-else class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-slate-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Mã đơn</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Khách hàng</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Bàn</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Tổng tiền</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Trạng thái</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Thời gian</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="order in filteredOrders" :key="order.id" class="hover:bg-slate-50 transition">
              <td class="px-4 py-3 whitespace-nowrap">
                <span class="font-mono font-medium text-slate-900 text-sm">{{ order.orderNumber }}</span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap">
                <div v-if="order.customerName">
                  <p class="text-sm font-medium text-slate-900">{{ order.customerName }}</p>
                </div>
                <span v-else class="text-sm text-slate-500">-</span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">
                {{ order.tableNumber || '-' }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm font-bold text-slate-900">
                {{ formatCurrency(order.total) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap">
                <span :class="getStatusBadgeClass(order.status)">
                  {{ getStatusLabel(order.status) }}
                </span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-600">
                {{ formatDate(order.createdAt) }}
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                  <button @click="viewOrder(order)" class="p-2 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors" title="Xem chi tiết">
                    <i class="fas fa-eye text-sm"></i>
                  </button>
                  <button @click="printOrder(order)" class="p-2 text-purple-600 hover:bg-purple-50 rounded-lg transition-colors" title="Xuất hóa đơn PDF">
                    <i class="fas fa-print text-sm"></i>
                  </button>
                  <button 
                    v-if="order.status !== 'COMPLETED' && order.status !== 'CANCELLED'"
                    @click="updateOrderStatus(order)"
                    class="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                    title="Cập nhật trạng thái"
                  >
                    <i class="fas fa-check-circle text-sm"></i>
                  </button>
                  <button 
                    v-if="order.status === 'PENDING'"
                    @click="cancelOrder(order)"
                    class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Hủy đơn"
                  >
                    <i class="fas fa-times-circle text-sm"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <OrderDetailModal
      v-if="selectedOrder"
      :order="selectedOrder"
      @close="selectedOrder = null"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderService } from '@/services/orderService'
import { useNotificationStore } from '@/stores/notification'
import OrderDetailModal from '@/components/modals/OrderDetailModal.vue'
import { printInvoice } from '@/utils/invoicePrinter'

const notification = useNotificationStore()

const loading = ref(false)
const orders = ref([])
const filterStatus = ref('')
const fromDate = ref('')
const toDate = ref('')
const selectedOrder = ref(null)

// Active filters (sẽ được áp dụng khi bấm nút Lọc)
const activeFilterStatus = ref('')
const activeFromDate = ref('')
const activeToDate = ref('')

const filteredOrders = computed(() => {
  let result = orders.value

  // Filter by status
  if (activeFilterStatus.value) {
    result = result.filter(o => o.status === activeFilterStatus.value)
  }

  // Filter by date range
  if (activeFromDate.value) {
    const from = new Date(activeFromDate.value + 'T00:00:00')
    result = result.filter(o => {
      if (!o.createdAt) return false
      const orderDate = new Date(o.createdAt)
      return orderDate >= from
    })
  }

  if (activeToDate.value) {
    const to = new Date(activeToDate.value + 'T23:59:59')
    result = result.filter(o => {
      if (!o.createdAt) return false
      const orderDate = new Date(o.createdAt)
      return orderDate <= to
    })
  }

  // Sort by createdAt descending (newest first)
  result = result.sort((a, b) => {
    if (!a.createdAt) return 1
    if (!b.createdAt) return -1
    return new Date(b.createdAt) - new Date(a.createdAt)
  })

  return result
})

onMounted(() => {
  loadOrders()
  setDefaultDateRange()
})

function setDefaultDateRange() {
  const today = new Date()
  const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
  
  // Format: YYYY-MM-DD
  fromDate.value = formatDateForInput(firstDayOfMonth)
  toDate.value = formatDateForInput(today)
  
  // Set active filters to default
  activeFromDate.value = fromDate.value
  activeToDate.value = toDate.value
}

function formatDateForInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function applyFilter() {
  // Áp dụng filter từ input vào active filters
  activeFilterStatus.value = filterStatus.value
  activeFromDate.value = fromDate.value
  activeToDate.value = toDate.value
}

function resetFilter() {
  // Reset tất cả về mặc định
  filterStatus.value = ''
  setDefaultDateRange()
  // Áp dụng ngay filter mặc định
  activeFilterStatus.value = ''
  activeFromDate.value = fromDate.value
  activeToDate.value = toDate.value
}

async function loadOrders() {
  loading.value = true
  try {
    const response = await orderService.getAll()
    if (response.success) {
      orders.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách đơn hàng')
  } finally {
    loading.value = false
  }
}

function viewOrder(order) {
  selectedOrder.value = order
}

function printOrder(order) {
  printInvoice(order)
}

async function updateOrderStatus(order) {
  const statusMap = {
    'PENDING': 'CONFIRMED',
    'CONFIRMED': 'PREPARING',
    'PREPARING': 'SERVED',
    'SERVED': 'COMPLETED'
  }
  
  const newStatus = statusMap[order.status]
  if (!newStatus) return

  try {
    await orderService.updateStatus(order.id, newStatus)
    notification.success('Cập nhật trạng thái thành công')
    loadOrders()
  } catch (error) {
    notification.error('Không thể cập nhật trạng thái')
  }
}

async function cancelOrder(order) {
  if (!confirm(`Hủy đơn hàng ${order.orderNumber}?`)) return

  try {
    await orderService.updateStatus(order.id, 'CANCELLED')
    notification.success('Đã hủy đơn hàng')
    loadOrders()
  } catch (error) {
    notification.error('Không thể hủy đơn hàng')
  }
}

function getStatusBadgeClass(status) {
  const classes = {
    'PENDING': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800',
    'CONFIRMED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-800',
    'PREPARING': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-orange-100 text-orange-800',
    'SERVED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-purple-100 text-purple-800',
    'COMPLETED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800',
    'CANCELLED': 'px-2.5 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800'
  }
  return classes[status] || 'px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800'
}

function getStatusLabel(status) {
  const labels = {
    'PENDING': 'Chờ xử lý',
    'CONFIRMED': 'Đã xác nhận',
    'PREPARING': 'Đang chuẩn bị',
    'SERVED': 'Đã phục vụ',
    'COMPLETED': 'Hoàn thành',
    'CANCELLED': 'Đã hủy'
  }
  return labels[status] || status
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value || 0)
}

function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN')
}
</script>
