<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý Đơn hàng</h1>
        <p class="text-gray-600 mt-1">Danh sách và trạng thái đơn hàng</p>
      </div>
      <router-link to="/admin/orders/create" class="btn-primary flex items-center gap-2">
        <PlusIcon class="w-5 h-5" />
        Tạo đơn hàng mới
      </router-link>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="input-field">
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
          <label class="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
          <input v-model="fromDate" type="date" class="input-field" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
          <input v-model="toDate" type="date" class="input-field" />
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="loading-spinner"></div>
    </div>

    <!-- Orders Table -->
    <div v-else class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã đơn</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khách hàng</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Bàn/Phòng</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tổng tiền</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thời gian</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="order in filteredOrders" :key="order.id" class="hover:bg-gray-50 transition">
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="font-mono font-medium text-blue-600">{{ order.orderNumber }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div v-if="order.customerName">
                  <p class="text-sm font-medium text-gray-900">{{ order.customerName }}</p>
                </div>
                <span v-else class="text-sm text-gray-500">-</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ order.tableNumber || order.roomName || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-green-600">
                {{ formatCurrency(order.total) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getStatusBadgeClass(order.status)">
                  {{ getStatusLabel(order.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(order.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                  <button @click="viewOrder(order)" class="text-blue-600 hover:text-blue-900" title="Xem chi tiết">
                    <EyeIcon class="w-5 h-5" />
                  </button>
                  <button @click="printOrder(order)" class="text-purple-600 hover:text-purple-900" title="Xuất hóa đơn PDF">
                    <PrinterIcon class="w-5 h-5" />
                  </button>
                  <button 
                    v-if="order.status !== 'COMPLETED' && order.status !== 'CANCELLED'"
                    @click="updateOrderStatus(order)"
                    class="text-green-600 hover:text-green-900"
                    title="Cập nhật trạng thái"
                  >
                    <CheckCircleIcon class="w-5 h-5" />
                  </button>
                  <button 
                    v-if="order.status === 'PENDING'"
                    @click="cancelOrder(order)"
                    class="text-red-600 hover:text-red-900"
                    title="Hủy đơn"
                  >
                    <XCircleIcon class="w-5 h-5" />
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
import {
  PlusIcon,
  EyeIcon,
  CheckCircleIcon,
  XCircleIcon,
  PrinterIcon
} from '@heroicons/vue/24/outline'
import OrderDetailModal from '@/components/modals/OrderDetailModal.vue'
import { printInvoice } from '@/utils/invoicePrinter'

const notification = useNotificationStore()

const loading = ref(false)
const orders = ref([])
const filterStatus = ref('')
const fromDate = ref('')
const toDate = ref('')
const selectedOrder = ref(null)

const filteredOrders = computed(() => {
  let result = orders.value

  if (filterStatus.value) {
    result = result.filter(o => o.status === filterStatus.value)
  }

  return result
})

onMounted(() => {
  loadOrders()
})

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
    'PENDING': 'badge bg-yellow-100 text-yellow-800',
    'CONFIRMED': 'badge bg-blue-100 text-blue-800',
    'PREPARING': 'badge bg-orange-100 text-orange-800',
    'SERVED': 'badge bg-purple-100 text-purple-800',
    'COMPLETED': 'badge bg-green-100 text-green-800',
    'CANCELLED': 'badge bg-red-100 text-red-800'
  }
  return classes[status] || 'badge bg-gray-100 text-gray-800'
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
