<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Thêm món</h1>
        <p class="text-gray-600 mt-1">Thêm món vào đơn hàng</p>
      </div>
      <router-link to="/admin/reservations/checkin" class="btn-secondary flex items-center gap-2">
        <ArrowLeftIcon class="w-5 h-5" />
        Quay lại
      </router-link>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-stretch">
      <!-- Left Column: Order Form -->
      <div class="lg:col-span-2 flex flex-col">
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

        <!-- Tabs Navigation -->
        <div class="card p-0 overflow-hidden flex-1 flex flex-col">
          <div class="border-b border-gray-200">
            <nav class="flex -mb-px">
              <button
                @click="activeTab = 'dishes'"
                :class="[
                  'flex-1 px-4 py-3 text-sm font-medium text-center transition-colors border-b-2 rounded-tl-xl',
                  activeTab === 'dishes'
                    ? 'border-red-500 text-red-600 bg-red-50'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                ]"
              >
                <i class="fas fa-utensils mr-2"></i>
                Chọn món ăn
              </button>
              <button
                v-if="existingOrderDetails.length > 0"
                @click="activeTab = 'current-order'"
                :class="[
                  'flex-1 px-4 py-3 text-sm font-medium text-center transition-colors border-b-2 relative rounded-tr-xl',
                  activeTab === 'current-order'
                    ? 'border-blue-500 text-blue-600 bg-blue-50'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                ]"
              >
                <i class="fas fa-list-check mr-2"></i>
                Đơn hàng hiện tại
                <span class="ml-2 px-2 py-0.5 text-xs font-semibold rounded-full bg-blue-600 text-white">
                  {{ existingOrderDetails.length }}
                </span>
              </button>
            </nav>
          </div>

          <!-- Tab Content: Dish Selection -->
          <div v-if="activeTab === 'dishes'" class="p-6 flex-1 flex flex-col min-h-0 max-h-[100vh]">
            <!-- Search -->
            <div class="mb-4 flex-shrink-0">
              <input
                v-model="dishSearchQuery"
                type="text"
                placeholder="Tìm kiếm món ăn..."
                class="input-field"
              />
            </div>

            <!-- Dish Grid -->
            <div class="grid grid-cols-2 md:grid-cols-3 gap-3 overflow-y-auto h-full items-start" style="min-height: 450px; ">
              <div 
                v-for="dish in filteredDishes" 
                :key="dish.id"
                @click="addDishToOrder(dish)"
                class="border-2 border-gray-200 rounded-lg p-3 cursor-pointer hover:border-red-400 hover:bg-red-50 transition"
              >
                <div class="flex items-center gap-2">
                  <img :src="dish.imageUrl" :alt="dish.name" class="w-10 h-10 object-cover rounded-lg flex-shrink-0">
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-gray-900 truncate">{{ dish.name }}</p>
                    <p class="text-xs text-red-600 font-bold">{{ formatCurrency(dish.price) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Content: Existing Order Items -->
          <div v-if="activeTab === 'current-order' && existingOrderDetails.length > 0" class="p-6 bg-white flex-1 flex flex-col min-h-0">
            <div class="space-y-3 flex-1 min-h-0 overflow-y-auto">
              <div
                v-for="item in existingOrderDetails"
                :key="item.id"
                class="bg-gray-50 rounded-lg p-4 border border-gray-200 shadow-sm hover:shadow-md transition-shadow"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-bold text-gray-900 mb-2">{{ item.dishName || item.dish?.name || 'Món không xác định' }} ( x{{ item.quantity }} )</p>
                    <div class="space-y-1 text-xs text-gray-600">
                      <div>Giá: <span class="font-semibold text-red-600">{{ formatCurrency(item.price) }}</span></div>
                    </div>
                    <div v-if="getCleanedNotes(item.notes)" class="mt-3">
                      <div class="flex items-start gap-2">
                        <i class="fas fa-sticky-note text-yellow-500 text-xs mt-0.5"></i>
                        <p class="text-xs text-gray-600 italic bg-yellow-50 px-2 py-1 rounded border border-yellow-200 flex-1">
                          {{ getCleanedNotes(item.notes) }}
                        </p>
                      </div>
                    </div>
                  </div>
                  <div class="flex-shrink-0 text-right">
                    <p class="text-sm font-bold text-gray-900">{{ formatCurrency(item.subtotal || (item.price * item.quantity)) }}</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="existingOrderTotal > 0" class="mt-4 pt-4 border-t border-gray-300 flex-shrink-0">
              <div class="flex justify-between items-center">
                <span class="text-sm font-medium text-gray-700">Tổng đơn hàng hiện tại:</span>
                <span class="text-lg font-bold text-blue-600">{{ formatCurrency(existingOrderTotal) }}</span>
              </div>
            </div>
          </div>

          <!-- Empty state cho tab đơn hàng hiện tại -->
          <div v-if="activeTab === 'current-order' && existingOrderDetails.length === 0" class="p-6 text-center py-12">
            <i class="fas fa-shopping-cart text-gray-300 text-4xl mb-3"></i>
            <p class="text-gray-500">Chưa có món nào trong đơn hàng</p>
          </div>
        </div>
      </div>

      <!-- Right Column: Order Summary -->
      <div class="lg:col-span-1 flex flex-col">
        <div class="card w-full h-full flex flex-col">
          <h3 class="text-lg font-bold text-gray-900 mb-4">Tóm tắt</h3>
          
          <!-- Order Items -->
          <div class="space-y-3 mb-4 max-h-64 overflow-y-auto">
            <div v-if="orderForm.items.length === 0" class="text-center py-8 text-gray-500">
              Chưa có món nào
            </div>
            <div 
              v-else
              v-for="(item, index) in orderForm.items" 
              :key="index"
              class="p-3 bg-gray-50 rounded-lg space-y-2"
            >
              <div class="flex items-center gap-2">
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
              <div>
                <input
                  v-model="item.notes"
                  type="text"
                  placeholder="Ghi chú cho món này..."
                  class="w-full text-xs border border-gray-300 rounded px-2 py-1 focus:ring-2 focus:ring-red-500 focus:border-transparent"
                />
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
              :class="[
                'w-full font-medium py-2.5 px-4 rounded-lg transition-colors',
                canCreateOrder 
                  ? 'bg-red-600 hover:bg-red-700 text-white cursor-pointer' 
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              ]"
            >
              Thêm món
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
import { useRouter, useRoute } from 'vue-router'
import { orderService } from '@/services/orderService'
import { dishService } from '@/services/dishService'
import { reservationService } from '@/services/reservationService'
import { useNotificationStore } from '@/stores/notification'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'

const router = useRouter()
const route = useRoute()
const notification = useNotificationStore()

const dishes = ref([])
const confirmedReservations = ref([])
const orders = ref([])
const selectedReservationId = ref('')
const dishSearchQuery = ref('')
const existingOrderDetails = ref([])
const existingOrder = ref(null)
const activeTab = ref('dishes') // 'dishes' hoặc 'current-order'

const orderForm = ref({
  orderId: null, // ID của order (null nếu tạo mới, có giá trị nếu update)
  reservationId: null,
  items: [],
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
  return subtotal.value + tax.value
})

const canCreateOrder = computed(() => {
  return orderForm.value.reservationId && 
         orderForm.value.items.length > 0
})

const existingOrderTotal = computed(() => {
  if (!existingOrder.value) return 0
  return existingOrder.value.total || existingOrder.value.subtotal || 0
})

onMounted(() => {
  loadData()
  handleQueryParams()
})

async function handleQueryParams() {
  // Xử lý reservationId từ query params
  const reservationId = route.query.reservationId || route.query.reservation_id
  if (reservationId) {
    const reservationIdNum = parseInt(reservationId)
    orderForm.value.reservationId = reservationIdNum
    
    // Load orders để tìm order theo reservationId
    await loadData()
    
    // Tìm tất cả orders có cùng reservationId, loại bỏ CANCELLED, sắp xếp theo createdAt mới nhất
    const matchingOrders = orders.value
      .filter(o => o.reservationId === reservationIdNum && o.status !== 'CANCELLED')
      .sort((a, b) => {
        const dateA = new Date(a.createdAt || 0)
        const dateB = new Date(b.createdAt || 0)
        return dateB - dateA // Sắp xếp giảm dần (mới nhất trước)
      })
    
    const foundOrder = matchingOrders.length > 0 ? matchingOrders[0] : null
    
    if (foundOrder) {
      // Nếu có order, set orderId (để biết là update)
      orderForm.value.orderId = foundOrder.id
      await loadExistingOrderDetails(foundOrder.id)
    } else {
      // Nếu không có order, orderId = null (tạo mới)
      orderForm.value.orderId = null
      existingOrder.value = null
      existingOrderDetails.value = []
    }
  }

  // Xử lý orderId từ query params (nếu có)
  const orderId = route.query.orderId || route.query.order_id
  if (orderId) {
    const orderIdNum = parseInt(orderId)
    orderForm.value.orderId = orderIdNum
    // Load order để lấy reservationId và order details
    try {
      const orderRes = await orderService.getById(orderIdNum)
      if (orderRes.success && orderRes.data) {
        const order = orderRes.data
        // Set reservationId từ order
        if (order.reservationId) {
          orderForm.value.reservationId = order.reservationId
        }
        await loadExistingOrderDetails(orderIdNum)
      }
    } catch (error) {
      console.error('Error loading order:', error)
      notification.error('Không thể tải thông tin đơn hàng')
    }
  }
}

async function loadExistingOrderDetails(orderId) {
  try {
    const orderRes = await orderService.getById(orderId)
    if (orderRes.success && orderRes.data) {
      existingOrder.value = orderRes.data
      // Lấy order details từ order.items hoặc order.orderDetails
      existingOrderDetails.value = orderRes.data.items || orderRes.data.orderDetails || []
      // Không tự động chuyển tab - giữ nguyên tab hiện tại
    }
  } catch (error) {
    console.error('Error loading existing order details:', error)
    existingOrder.value = null
    existingOrderDetails.value = []
  }
}

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
    orderForm.value.reservationId = null
    orderForm.value.items = []
    orderForm.value.notes = ''
  }
}

async function createOrder() {
  if (!canCreateOrder.value) {
    notification.error('Vui lòng chọn đặt bàn và thêm ít nhất một món')
    return
  }

  try {
    // Chuẩn bị items data cho order_details (chỉ gửi dishId, quantity, notes)
    const itemsData = orderForm.value.items.map(item => ({
      dishId: item.dishId,
      quantity: item.quantity,
      notes: item.notes || ''
    }))

    // Nếu có orderId, update order và order_details
    if (orderForm.value.orderId) {
      // Update order với notes, subtotal, tax, total, reservation_id
      const updateData = {
        notes: orderForm.value.notes || '',
        subtotal: subtotal.value,
        tax: tax.value,
        total: total.value,
        reservationId: orderForm.value.reservationId,
        items: itemsData,
        status: 'CONFIRMED'
      }
      await orderService.update(orderForm.value.orderId, updateData)
      // Đảm bảo status được set là CONFIRMED
      await orderService.updateStatus(orderForm.value.orderId, 'CONFIRMED')
      notification.success('Đã cập nhật đơn hàng')
    } else {
      // Tạo order mới với notes, subtotal, tax, total, reservation_id
      const orderData = {
        notes: orderForm.value.notes || '',
        subtotal: subtotal.value,
        tax: tax.value,
        total: total.value,
        reservationId: orderForm.value.reservationId,
        items: itemsData,
        status: 'CONFIRMED'
      }

      const result = await orderService.create(orderData)
      // Đảm bảo status được set là CONFIRMED sau khi tạo
      if (result.success && result.data?.id) {
        await orderService.updateStatus(result.data.id, 'CONFIRMED')
      }
      notification.success('Tạo đơn hàng thành công')
    }
    
    router.push('/admin/orders')
  } catch (error) {
    notification.error('Không thể tạo/cập nhật đơn hàng: ' + (error.response?.data?.message || error.message))
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}

// Làm sạch notes - loại bỏ "served" và "Chưa lên"
function getCleanedNotes(notes) {
  if (!notes) return null
  
  let cleaned = notes
  // Loại bỏ "served" (không phân biệt hoa thường)
  const servedRegex = /\bserved\b/gi
  cleaned = cleaned.replace(servedRegex, '').trim()
  
  return cleaned || null
}
</script>
