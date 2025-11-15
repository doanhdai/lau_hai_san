<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Tạo Đơn hàng mới</h1>
        <p class="text-gray-600 mt-1">Thêm đơn hàng cho khách hàng</p>
      </div>
      <router-link to="/orders" class="btn-secondary flex items-center gap-2">
        <ArrowLeftIcon class="w-5 h-5" />
        Quay lại
      </router-link>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Left Column: Order Form -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Reservation Selection -->
        <!-- <div class="card bg-gradient-to-r from-indigo-50 to-blue-50 border-2 border-indigo-200">
          <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
            <span class="text-2xl">📅</span>
            Chọn từ đặt bàn đã xác nhận
          </h3>
          <div>
            <select v-model="selectedReservationId" @change="onReservationSelected" class="input-field">
              <option value="">-- Chọn đặt bàn --</option>
              <option v-for="reservation in confirmedReservations" :key="reservation.id" :value="reservation.id">
                {{ reservation.customerName }} - {{ reservation.customerPhone }} - 
                {{ formatDateTime(reservation.reservationTime) }} - 
                {{ reservation.numberOfGuests }} người
                {{ reservation.tableNumber ? ` - Bàn ${reservation.tableNumber}` : '' }}
              </option>
            </select>
          </div>
        </div> -->

        <!-- Dish Selection -->
        <div class="card">
          <h3 class="text-lg font-bold text-gray-900 mb-4">Chọn món ăn</h3>
          
          <!-- Search -->
          <div class="mb-4">
            <input
              v-model="dishSearchQuery"
              type="text"
              placeholder="Tìm kiếm món ăn..."
              class="input-field"
            />
          </div>

          <!-- Dish Grid -->
          <div class="grid grid-cols-2 md:grid-cols-3 gap-3 max-h-96 overflow-y-auto">
            <div 
              v-for="dish in filteredDishes" 
              :key="dish.id"
              @click="addDishToOrder(dish)"
              class="border-2 border-gray-200 rounded-lg p-3 cursor-pointer hover:border-red-400 hover:bg-red-50 transition"
            >
              <div class="flex items-center gap-2">
                <img :src="dish.imageUrl" :alt="dish.name" class="w-10 h-10 object-cover rounded-lg">
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 truncate">{{ dish.name }}</p>
                  <p class="text-xs text-red-600 font-bold">{{ formatCurrency(dish.price) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Order Summary -->
      <div class="lg:col-span-1">
        <div class="card sticky top-6">
          <h3 class="text-lg font-bold text-gray-900 mb-4">Tóm tắt đơn hàng</h3>
          
          <!-- Order Items -->
          <div class="space-y-3 mb-4 max-h-64 overflow-y-auto">
            <div v-if="orderForm.items.length === 0" class="text-center py-8 text-gray-500">
              Chưa có món nào
            </div>
            <div 
              v-else
              v-for="(item, index) in orderForm.items" 
              :key="index"
              class="flex items-center gap-2 p-2 bg-gray-50 rounded-lg"
            >
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">{{ item.dishName }}</p>
                <p class="text-xs text-gray-600">{{ formatCurrency(item.price) }}</p>
              </div>
              <div class="flex items-center gap-2">
                <button @click="decreaseQuantity(index)" class="w-6 h-6 bg-gray-200 rounded hover:bg-gray-300">
                  -
                </button>
                <span class="text-sm font-bold w-8 text-center">{{ item.quantity }}</span>
                <button @click="increaseQuantity(index)" class="w-6 h-6 bg-red-500 text-white rounded hover:bg-red-600">
                  +
                </button>
                <button @click="removeItem(index)" class="w-6 h-6 bg-red-500 text-white rounded hover:bg-red-600">
                  ×
                </button>
              </div>
            </div>
          </div>

          <!-- Pricing -->
          <div class="space-y-2 pt-4 border-t">
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Tạm tính:</span>
              <span class="font-medium">{{ formatCurrency(subtotal) }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Thuế (10%):</span>
              <span class="font-medium">{{ formatCurrency(tax) }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Giảm giá:</span>
              <input 
                v-model.number="orderForm.discount" 
                type="number" 
                min="0"
                class="w-24 text-right border rounded px-2 py-1"
              />
            </div>
            <div class="flex justify-between text-lg font-bold pt-2 border-t">
              <span>Tổng cộng:</span>
              <span class="text-red-600">{{ formatCurrency(total) }}</span>
            </div>
          </div>

          <!-- Notes -->
          <div class="mt-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">Ghi chú</label>
            <textarea
              v-model="orderForm.notes"
              rows="2"
              class="input-field"
              placeholder="Ghi chú đơn hàng..."
            ></textarea>
          </div>

          <!-- Actions -->
          <div class="mt-4 space-y-2">
            <button 
              @click="createOrder" 
              :disabled="!canCreateOrder"
              class="btn-primary w-full"
            >
              Tạo đơn hàng
            </button>
            <button @click="clearOrder" class="btn-secondary w-full">
              Xóa tất cả
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderService } from '@/services/orderService'
import { dishService } from '@/services/dishService'
import { reservationService } from '@/services/reservationService'
import { useNotificationStore } from '@/stores/notification'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'

const router = useRouter()
const notification = useNotificationStore()

const dishes = ref([])
const confirmedReservations = ref([])
const orders = ref([])
const selectedReservationId = ref('')
const dishSearchQuery = ref('')

const orderForm = ref({
  customerId: '',
  reservationId: null,
  items: [],
  discount: 0,
  notes: ''
})

const filteredDishes = computed(() => {
  if (!dishSearchQuery.value) return dishes.value
  const query = dishSearchQuery.value.toLowerCase()
  return dishes.value.filter(d => 
    d.name.toLowerCase().includes(query) ||
    (d.description && d.description.toLowerCase().includes(query))
  )
})

const subtotal = computed(() => {
  return orderForm.value.items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
})

const tax = computed(() => subtotal.value * 0.1)

const total = computed(() => {
  return Math.max(0, subtotal.value + tax.value - (orderForm.value.discount || 0))
})

const canCreateOrder = computed(() => {
  return orderForm.value.reservationId && 
         orderForm.value.items.length > 0
})

onMounted(() => {
  loadData()
})

async function loadData() {
  try {
    const [dishesRes, reservationsRes, ordersRes] = await Promise.all([
      dishService.getAvailable(),
      reservationService.getAll(),
      orderService.getAll()
    ])
    
    if (dishesRes.success) dishes.value = dishesRes.data
    if (ordersRes.success) orders.value = ordersRes.data
    
    // Filter for confirmed reservations that don't have an order yet
    if (reservationsRes.success) {
      // Get all reservation IDs that already have orders
      const reservationIdsWithOrders = new Set(
        orders.value
          .filter(order => order.reservationId)
          .map(order => order.reservationId)
      )
      
      // Filter confirmed reservations that don't have orders
      confirmedReservations.value = reservationsRes.data.filter(
        r => r.status === 'CONFIRMED' && !reservationIdsWithOrders.has(r.id)
      ).sort((a, b) => new Date(a.reservationTime) - new Date(b.reservationTime))
    }
  } catch (error) {
    notification.error('Không thể tải dữ liệu')
  }
}

function onReservationSelected() {
  if (!selectedReservationId.value) return
  
  const reservation = confirmedReservations.value.find(
    r => r.id === parseInt(selectedReservationId.value)
  )
  
  if (reservation) {
    // Auto-fill customer and reservation
    orderForm.value.customerId = reservation.customerId || ''
    orderForm.value.reservationId = reservation.id
    
    notification.success(`Đã chọn đặt bàn cho ${reservation.customerName}`)
  }
}

function formatDateTime(datetime) {
  if (!datetime) return '-'
  const date = new Date(datetime)
  return date.toLocaleString('vi-VN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function addDishToOrder(dish) {
  const existingItem = orderForm.value.items.find(item => item.dishId === dish.id)
  
  if (existingItem) {
    existingItem.quantity++
  } else {
    orderForm.value.items.push({
      dishId: dish.id,
      dishName: dish.name,
      price: dish.price,
      quantity: 1,
      notes: ''
    })
  }
}

function increaseQuantity(index) {
  orderForm.value.items[index].quantity++
}

function decreaseQuantity(index) {
  if (orderForm.value.items[index].quantity > 1) {
    orderForm.value.items[index].quantity--
  }
}

function removeItem(index) {
  orderForm.value.items.splice(index, 1)
}

function clearOrder() {
  if (confirm('Bạn có chắc muốn xóa tất cả?')) {
    selectedReservationId.value = ''
    orderForm.value.customerId = ''
    orderForm.value.reservationId = null
    orderForm.value.items = []
    orderForm.value.discount = 0
    orderForm.value.notes = ''
  }
}

async function createOrder() {
  if (!canCreateOrder.value) return

  try {
    const orderData = {
      customerId: orderForm.value.customerId,
      reservationId: orderForm.value.reservationId,
      items: orderForm.value.items,
      discount: orderForm.value.discount || 0,
      notes: orderForm.value.notes
    }

    await orderService.create(orderData)
    notification.success('Tạo đơn hàng thành công')
    router.push('/admin/orders')
  } catch (error) {
    notification.error('Không thể tạo đơn hàng')
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}
</script>
