<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl shadow-2xl max-w-3xl w-full mx-4 max-h-[90vh] overflow-y-auto animate-slide-in">
      <!-- Header -->
      <div class="bg-gradient-to-r from-red-500 to-red-600 text-white px-6 py-4 rounded-t-2xl sticky top-0 z-10">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-xl font-bold">Chi tiết đơn hàng</h3>
            <p class="text-sm text-red-100 mt-1">{{ order.orderNumber }}</p>
          </div>
          <button @click="$emit('close')" class="text-white hover:bg-red-700 p-2 rounded-lg transition">
            <XMarkIcon class="w-6 h-6" />
          </button>
        </div>
      </div>

      <!-- Content -->
      <div class="p-6 space-y-6">
        <!-- Status & Info -->
        <div class="grid grid-cols-2 gap-4">
          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="text-sm text-gray-600 mb-1">Trạng thái</p>
            <span :class="getStatusBadgeClass(order.status)">
              {{ getStatusLabel(order.status) }}
            </span>
          </div>
          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="text-sm text-gray-600 mb-1">Thời gian tạo</p>
            <p class="font-medium">{{ formatDate(order.createdAt) }}</p>
          </div>
        </div>

        <!-- Customer Info -->
        <div v-if="order.customerName" class="border-t pt-4">
          <h4 class="font-semibold text-gray-900 mb-3">Thông tin khách hàng</h4>
          <div class="bg-blue-50 p-4 rounded-lg">
            <p class="font-medium text-gray-900">{{ order.customerName }}</p>
          </div>
        </div>

        <!-- Table/Room Info -->
        <div v-if="order.tableNumber || order.roomName" class="border-t pt-4">
          <h4 class="font-semibold text-gray-900 mb-3">Vị trí</h4>
          <div class="bg-green-50 p-4 rounded-lg">
            <p class="font-medium text-gray-900">
              {{ order.tableNumber ? `Bàn ${order.tableNumber}` : `Phòng ${order.roomName}` }}
            </p>
          </div>
        </div>

        <!-- Order Items -->
        <div class="border-t pt-4">
          <h4 class="font-semibold text-gray-900 mb-3">Món ăn đã gọi</h4>
          <div class="space-y-3">
            <div 
              v-for="item in order.items" 
              :key="item.id"
              class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition"
            >
              <div class="flex-1">
                <p class="font-medium text-gray-900">{{ item.dishName }}</p>
                <p class="text-sm text-gray-600 mt-1">{{ formatCurrency(item.price) }} x {{ item.quantity }}</p>
                <p v-if="item.notes" class="text-sm text-gray-500 italic mt-1">{{ item.notes }}</p>
              </div>
              <div class="text-right">
                <p class="font-bold text-gray-900">{{ formatCurrency(item.subtotal) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Total -->
        <div class="border-t pt-4 space-y-3">
          <div class="flex justify-between text-gray-700">
            <span>Tạm tính:</span>
            <span class="font-medium">{{ formatCurrency(order.subtotal) }}</span>
          </div>
          <div class="flex justify-between text-gray-700">
            <span>Thuế (10%):</span>
            <span class="font-medium">{{ formatCurrency(order.tax) }}</span>
          </div>
          <div v-if="order.discount > 0" class="flex justify-between text-green-600">
            <span>Giảm giá:</span>
            <span class="font-medium">-{{ formatCurrency(order.discount) }}</span>
          </div>
          <div class="flex justify-between text-lg font-bold text-red-600 pt-3 border-t-2">
            <span>Tổng cộng:</span>
            <span>{{ formatCurrency(order.total) }}</span>
          </div>
        </div>

        <!-- Notes -->
        <div v-if="order.notes" class="border-t pt-4">
          <h4 class="font-semibold text-gray-900 mb-2">Ghi chú</h4>
          <p class="text-gray-700 bg-yellow-50 p-3 rounded-lg">{{ order.notes }}</p>
        </div>

        <!-- Created By -->
        <div v-if="order.createdByName" class="border-t pt-4">
          <p class="text-sm text-gray-600">
            Người tạo: <span class="font-medium text-gray-900">{{ order.createdByName }}</span>
          </p>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-6 py-4 bg-gray-50 rounded-b-2xl flex justify-between">
        <button @click="exportPDF" class="btn-primary flex items-center gap-2">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6.72 13.829c-.24.03-.48.062-.72.096m.72-.096a42.415 42.415 0 0110.56 0m-10.56 0L6.34 18m10.94-4.171c.24.03.48.062.72.096m-.72-.096L17.66 18m0 0l.229 2.523a1.125 1.125 0 01-1.12 1.227H7.231c-.662 0-1.18-.568-1.12-1.227L6.34 18m11.318 0h1.091A2.25 2.25 0 0021 15.75V9.456c0-1.081-.768-2.015-1.837-2.175a48.055 48.055 0 00-1.913-.247M6.34 18H5.25A2.25 2.25 0 013 15.75V9.456c0-1.081.768-2.015 1.837-2.175a48.041 48.041 0 011.913-.247m10.5 0a48.536 48.536 0 00-10.5 0m10.5 0V3.375c0-.621-.504-1.125-1.125-1.125h-8.25c-.621 0-1.125.504-1.125 1.125v3.659M18 10.5h.008v.008H18V10.5zm-3 0h.008v.008H15V10.5z" />
          </svg>
          Xuất hóa đơn PDF
        </button>
        <button @click="$emit('close')" class="btn-secondary">
          Đóng
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { XMarkIcon } from '@heroicons/vue/24/outline'
import { printInvoice } from '@/utils/invoicePrinter'

const props = defineProps({
  order: {
    type: Object,
    required: true
  }
})

defineEmits(['close'])

function exportPDF() {
  printInvoice(props.order)
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
