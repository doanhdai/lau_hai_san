<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999] p-4"
      @click.self="handleClose"
    >
      <div 
        class="bg-white rounded-lg shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col"
      >
        <!-- Modal Header -->
        <div class="bg-slate-900 text-white px-6 py-4 flex items-center justify-between">
          <div>
            <h3 class="text-xl font-bold">Chi tiết đơn hàng</h3>
            <p v-if="selectedOrder" class="text-sm text-slate-300 mt-1">
              {{ selectedOrder.orderNumber }}
            </p>
          </div>
          <button @click="handleClose" class="text-white/80 hover:text-white transition-colors">
            <i class="fas fa-times text-xl"></i>
          </button>
        </div>

        <!-- Modal Content -->
        <div class="flex-1 overflow-y-auto p-6">
          <!-- Loading State -->
          <div v-if="loadingOrder" class="flex flex-col items-center justify-center py-12">
            <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin mb-4"></div>
            <p class="text-sm text-slate-600 font-medium">Đang tải thông tin đơn hàng...</p>
          </div>

          <!-- No Order State -->
          <div v-else-if="!selectedOrder" class="text-center py-12">
            <i class="fas fa-receipt text-6xl text-gray-300 mb-4"></i>
            <p class="text-lg font-medium text-gray-600">Chưa có đơn hàng</p>
            <p class="text-sm text-gray-500 mt-2">Bàn này chưa có đơn hàng nào</p>
          </div>

          <!-- Order Details -->
          <div v-else class="space-y-6">
            <!-- Order Info -->
            <div class="bg-slate-50 rounded-lg p-4 space-y-3">
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <p class="text-xs text-slate-500 mb-1">Khách hàng</p>
                  <p class="text-sm font-semibold text-slate-900">{{ selectedOrder.customerName || 'Khách vãng lai' }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Bàn</p>
                  <p class="text-sm font-semibold text-slate-900">{{ selectedOrder.tableNumber || 'Chưa có' }}</p>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Trạng thái</p>
                  <span :class="['px-3 py-1 rounded-full text-xs font-semibold', getOrderStatusClass(selectedOrder.status)]">
                    {{ getOrderStatusLabel(selectedOrder.status) }}
                  </span>
                </div>
                <div>
                  <p class="text-xs text-slate-500 mb-1">Ngày tạo</p>
                  <p class="text-sm text-slate-700">{{ formatDateTime(selectedOrder.createdAt) }}</p>
                </div>
              </div>
              <div v-if="selectedOrder.notes">
                <p class="text-xs text-slate-500 mb-1">Ghi chú đơn hàng</p>
                <p class="text-sm text-slate-700 bg-white p-2 rounded border border-slate-200">{{ selectedOrder.notes }}</p>
              </div>
            </div>

            <!-- Order Items -->
            <div>
              <div class="flex items-center justify-between mb-3">
                <h4 class="font-semibold text-slate-900 flex items-center gap-2">
                  <i class="fas fa-utensils"></i>
                  <span>Danh sách món</span>
                </h4>
                <!-- Nút Đã lên tất cả -->
                <button
                  v-if="selectedOrder && selectedOrder.status !== 'COMPLETED' && selectedOrder.status !== 'CANCELLED' && !allItemsServed"
                  @click="markAllAsServed"
                  class="px-3 py-1.5 text-xs font-medium text-white bg-green-600 rounded hover:bg-green-700 transition-colors flex items-center gap-1.5"
                >
                  <i class="fas fa-check-double text-xs"></i>
                  <span>Đã lên tất cả</span>
                </button>
              </div>
              <div class="space-y-2">
                <div
                  v-for="(item, index) in selectedOrder.items"
                  :key="item.id || index"
                  class="bg-white border rounded-lg p-4 hover:shadow-md transition-shadow relative"
                  :class="isItemServed(item) ? 'border-green-300 bg-green-50' : 'border-slate-200'"
                >
                  <button
                    v-if="selectedOrder.status !== 'COMPLETED' && selectedOrder.status !== 'CANCELLED' && !isItemServed(item)"
                    @click="removeOrderItem(item.id)"
                    class="absolute top-2 right-2 w-6 h-6 flex items-center justify-center text-red-600 hover:text-red-800 hover:bg-red-50 rounded-full transition-colors"
                    title="Hủy món"
                  >
                    <i class="fas fa-times text-sm"></i>
                  </button>
                  <div class="flex items-start gap-3">
                    <div class="flex items-center gap-2 pt-1">
                      <input
                        type="checkbox"
                        :checked="isItemServed(item)"
                        @click="handleItemServedClick(item.id, $event)"
                        class="w-4 h-4 text-green-600 border-gray-300 rounded focus:ring-green-500"
                        :disabled="isItemServed(item) || selectedOrder.status === 'COMPLETED' || selectedOrder.status === 'CANCELLED'"
                      />
                    </div>
                    <!-- Dish Image -->
                    <div class="flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden bg-gray-200 border border-gray-300">
                      <img
                        v-if="getDishImage(item)"
                        :src="getDishImage(item)"
                        :alt="item.dishName"
                        class="w-full h-full object-cover"
                        @error="handleImageError"
                      />
                      <div v-else class="w-full h-full flex items-center justify-center bg-gray-100">
                        <i :class="['fas', getDishIcon(item.dishName)]" class="text-2xl text-gray-400"></i>
                      </div>
                    </div>
                    <div class="flex-1">
                      <div class="flex items-start justify-between gap-3">
                        <div class="flex-1">
                          <div class="flex items-center gap-2 flex-wrap">
                            <p class="text-sm font-semibold text-slate-900">{{ item.dishName }}</p>
                            <span v-if="isItemServed(item)" class="text-xs bg-green-500 text-white px-2 py-0.5 rounded-full font-medium">
                              <i class="fas fa-check mr-1"></i>Đã lên đủ
                            </span>
                            <span v-else class="text-xs bg-gray-200 text-gray-700 px-2 py-0.5 rounded-full font-medium">
                              Chưa lên
                            </span>
                            <!-- Cảnh báo quá 30 phút -->
                            <span 
                              v-if="!isItemServed(item) && isItemOver30Minutes(item)" 
                              class="text-xs bg-red-500 text-white px-2 py-0.5 rounded-full font-medium animate-pulse"
                            >
                              <i class="fas fa-exclamation-triangle mr-1"></i>Bạn chưa lên món này! Đã quá {{ WARNING_TIME_MINUTES }}p
                            </span>
                          </div>
                          <div class="flex items-center justify-between gap-4 mt-1">
                            <div class="flex items-center gap-4">
                              <span class="text-xs text-slate-900 font-bold">SL: {{ item.quantity }}</span>
                              <span class="text-xs text-slate-500 font-bold">Giá: {{ formatCurrency(item.price) }}</span>
                            </div>
                            <span class="text-sm font-bold text-slate-900">Tổng: {{ formatCurrency(item.subtotal) }}</span>
                          </div>
                          <p v-if="getDisplayNotes(item)" class="text-xs text-slate-600 mt-2 italic bg-slate-50 p-2 rounded">
                            <i class="fas fa-sticky-note mr-1"></i>{{ getDisplayNotes(item) }}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Order Summary -->
            <div class="bg-gradient-to-br from-slate-50 to-slate-100 rounded-lg p-4 border border-slate-200">
              <h4 class="text-sm font-semibold text-slate-900 mb-3">Tổng kết</h4>
              <div class="space-y-2">
                <div class="flex justify-between text-sm">
                  <span class="text-slate-600">Tạm tính:</span>
                  <span class="font-medium text-slate-900">{{ formatCurrency(selectedOrder.subtotal) }}</span>
                </div>
                <div v-if="selectedOrder.discount && selectedOrder.discount > 0" class="flex justify-between text-sm">
                  <span class="text-slate-600">Giảm giá:</span>
                  <span class="font-medium text-red-600">-{{ formatCurrency(selectedOrder.discount) }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-slate-600">Thuế (10%):</span>
                  <span class="font-medium text-slate-900">{{ formatCurrency(selectedOrder.tax) }}</span>
                </div>
                <div class="border-t border-slate-300 pt-2 mt-2">
                  <div class="flex justify-between">
                    <span class="text-base font-bold text-slate-900">Tổng cộng:</span>
                    <span class="text-lg font-bold text-red-600">{{ formatCurrency(selectedOrder.total) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div v-if="selectedOrder && !loadingOrder" class="border-t border-slate-200 bg-slate-50 px-6 py-4 flex items-center justify-between gap-3">
          <div class="flex items-center gap-2">
            <!-- <button
              @click="handleClose"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
            >
              Đóng
            </button> -->
            <button
              v-if="selectedOrder.status !== 'COMPLETED' && selectedOrder.status !== 'CANCELLED'"
              @click="cancelOrder"
              class="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors flex items-center gap-2"
            >
              <i class="fas fa-times-circle"></i>
              <span>Hủy đơn</span>
            </button>
          </div>
          <button
            v-if="selectedOrder.status !== 'COMPLETED' && selectedOrder.status !== 'CANCELLED'"
            @click="proceedToPayment"
            :disabled="!allItemsServed"
            class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-blue-600"
          >
            <i class="fas fa-credit-card"></i>
            <span>Tiến hành thanh toán</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Promotion Selection Dialog -->
    <PromotionSelectionDialog
      :show="showPromotionDialog"
      v-model="selectedPromotion"
      :promotions="promotions"
      :order-subtotal="selectedOrder?.subtotal || 0"
      @close="showPromotionDialog = false"
    />
  </Teleport>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { orderService } from '@/services/orderService'
import { promotionService } from '@/services/promotionService'
import PromotionSelectionDialog from './PromotionSelectionDialog.vue'
import { useNotificationStore } from '@/stores/notification'

const notification = useNotificationStore()
const router = useRouter()

// Từ khóa đánh dấu món đã lên (có thể thay đổi dễ dàng)
const SERVED_KEYWORD = 'served'

// Thời gian cảnh báo (phút) - món quá thời gian này chưa được đánh dấu đã lên sẽ hiển thị cảnh báo
const WARNING_TIME_MINUTES = 30

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  reservationId: {
    type: [Number, String],
    default: null
  },
  tableId: {
    type: [Number, String],
    default: null
  },
  reservations: {
    type: Array,
    default: () => []
  },
  tablesWithReservations: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'order-updated'])

const selectedOrder = ref(null)
const loadingOrder = ref(false)
const showPromotionDialog = ref(false)
const selectedPromotion = ref(null)
const promotions = ref([])

// Watch for show prop changes to load order
watch(() => props.show, (newVal) => {
  if (newVal) {
    loadOrder()
  } else {
      // Reset when closing
      selectedOrder.value = null
      showPromotionDialog.value = false
      selectedPromotion.value = null
  }
})

async function loadOrder() {
  if (!props.reservationId && !props.tableId) {
    selectedOrder.value = null
    return
  }

  loadingOrder.value = true
  try {
    let reservationId = props.reservationId

    // If only tableId is provided, find reservation from tablesWithReservations
    if (!reservationId && props.tableId) {
      const tableWithReservation = props.tablesWithReservations.find(t => 
        t && t.id === props.tableId && t.reservation
      )
      if (tableWithReservation && tableWithReservation.reservation) {
        reservationId = tableWithReservation.reservation.id
      } else if (Array.isArray(props.reservations)) {
        // Try to find from reservations list
        const reservation = props.reservations.find(r => 
          r && r.tableId === props.tableId && 
          (r.status === 'CONFIRMED' || r.status === 'CHECKED_IN')
        )
        if (reservation) {
          reservationId = reservation.id
        }
      }
    }

    // Load all orders to find order by reservationId or tableId
    const ordersRes = await orderService.getAll()
    const allOrders = ordersRes.success ? ordersRes.data : ordersRes.data || []

    let matchingOrders = []

    // Nếu có reservationId, tìm order theo reservationId
    if (reservationId) {
      matchingOrders = allOrders.filter(order => {
        const orderReservationId = order.reservationId
        return orderReservationId === reservationId || 
               orderReservationId === Number(reservationId) || 
               Number(orderReservationId) === reservationId ||
               String(orderReservationId) === String(reservationId)
      })
    } 
    // Nếu không có reservationId nhưng có tableId, tìm order theo tableId (cho walk-in customers)
    else if (props.tableId) {
      matchingOrders = allOrders.filter(order => {
        const orderTableId = order.tableId
        return orderTableId === props.tableId || 
               orderTableId === Number(props.tableId) || 
               Number(orderTableId) === props.tableId ||
               String(orderTableId) === String(props.tableId)
      })
    }

    // Loại bỏ orders có status CANCELLED và sắp xếp theo createdAt mới nhất
    const validOrders = matchingOrders
      .filter(order => order.status !== 'CANCELLED')
      .sort((a, b) => {
        const dateA = new Date(a.createdAt || 0)
        const dateB = new Date(b.createdAt || 0)
        return dateB - dateA // Sắp xếp giảm dần (mới nhất trước)
      })

    // Chọn order mới nhất
    const foundOrder = validOrders.length > 0 ? validOrders[0] : null

    if (!foundOrder || !foundOrder.id) {
      selectedOrder.value = null
      // Không hiển thị notification nếu không tìm thấy order (có thể là bàn mới chưa có order)
      return
    }

    // Get order by order_id
    const orderRes = await orderService.getById(foundOrder.id)
    if (orderRes.success && orderRes.data) {
      selectedOrder.value = orderRes.data
    } else {
      selectedOrder.value = null
      notification.error('Không thể tải thông tin đơn hàng')
    }
  } catch (error) {
    console.error('Error loading order:', error)
    notification.error('Không thể tải thông tin đơn hàng')
    selectedOrder.value = null
  } finally {
    loadingOrder.value = false
  }
}

function handleClose() {
  emit('close')
}

// Kiểm tra món đã được served chưa (dựa vào notes có chứa SERVED_KEYWORD)
function isItemServed(item) {
  if (!item || !item.notes) return false
  return item.notes.includes(SERVED_KEYWORD)
}

// Kiểm tra món đã quá thời gian cảnh báo chưa được đánh dấu đã lên
function isItemOver30Minutes(item) {
  if (!item || !selectedOrder.value) return false
  
  // Logic tính thời gian:
  // 1. Nếu order đã checkin (có confirmedAt):
  //    - Nếu món được thêm TRƯỚC khi checkin: dùng confirmedAt (thời gian checkin)
  //    - Nếu món được thêm SAU khi checkin: dùng order_detail.createdAt (thời gian món được thêm)
  // 2. Nếu order chưa checkin (không có confirmedAt): dùng order_detail.createdAt
  
  let startTimeValue = null
  const orderConfirmedAt = selectedOrder.value.confirmedAt || selectedOrder.value.confirmed_at
  const itemCreatedAt = item.createdAt || item.created_at
  
  if (orderConfirmedAt && itemCreatedAt) {
    // Order đã checkin và món có createdAt
    // So sánh thời gian: nếu món được thêm trước checkin → dùng confirmedAt, ngược lại → dùng item.createdAt
    try {
      const confirmedTime = new Date(orderConfirmedAt)
      const itemCreatedTime = new Date(itemCreatedAt)
      
      if (isNaN(confirmedTime.getTime()) || isNaN(itemCreatedTime.getTime())) {
        // Invalid date, fallback về item.createdAt
        startTimeValue = itemCreatedAt
      } else {
        // Nếu món được thêm trước hoặc cùng lúc với checkin → dùng confirmedAt
        // Nếu món được thêm sau checkin → dùng item.createdAt
        if (itemCreatedTime <= confirmedTime) {
          startTimeValue = orderConfirmedAt // Món có từ trước khi checkin
        } else {
          startTimeValue = itemCreatedAt // Món được thêm sau khi checkin
        }
      }
    } catch (error) {
      console.error('Error comparing dates:', error)
      startTimeValue = itemCreatedAt // Fallback
    }
  } else if (orderConfirmedAt) {
    // Order đã checkin nhưng món không có createdAt → dùng confirmedAt
    startTimeValue = orderConfirmedAt
  } else if (itemCreatedAt) {
    // Order chưa checkin → dùng thời gian món được thêm vào
    startTimeValue = itemCreatedAt
  } else {
    // Fallback: dùng order.createdAt nếu không có gì
    startTimeValue = selectedOrder.value.createdAt || selectedOrder.value.created_at
  }
  
  if (!startTimeValue) return false
  
  try {
    const startTime = new Date(startTimeValue)
    if (isNaN(startTime.getTime())) return false // Invalid date
    
    const now = new Date()
    const diffInMinutes = (now - startTime) / (1000 * 60) // Chuyển đổi từ milliseconds sang minutes
    
    return diffInMinutes > WARNING_TIME_MINUTES
  } catch (error) {
    console.error('Error calculating time difference:', error)
    return false
  }
}

// Lấy notes hiển thị (loại bỏ SERVED_KEYWORD)
function getDisplayNotes(item) {
  if (!item || !item.notes) return null
  const regex = new RegExp(SERVED_KEYWORD, 'gi')
  return item.notes.replace(regex, '').trim() || null
}

// Lấy hình ảnh món ăn
function getDishImage(item) {
  if (item && item.dishImageUrl && item.dishImageUrl.trim() !== '') {
    return item.dishImageUrl
  }
  return null
}

// Lấy icon mặc định cho món ăn
function getDishIcon(dishName) {
  if (!dishName) return 'fa-utensils'
  const name = dishName.toLowerCase()
  if (name.includes('lẩu thai') || name.includes('thái')) return 'fa-bowl-food'
  if (name.includes('tôm')) return 'fa-shrimp'
  if (name.includes('bò')) return 'fa-drumstick-bite'
  if (name.includes('hải sản')) return 'fa-fish'
  if (name.includes('nấm')) return 'fa-seedling'
  if (name.includes('rau')) return 'fa-leaf'
  if (name.includes('mì') || name.includes('bún')) return 'fa-bowl-rice'
  if (name.includes('nước') || name.includes('trà') || name.includes('bia')) return 'fa-glass-water'
  if (name.includes('cá')) return 'fa-fish'
  if (name.includes('cua')) return 'fa-crab'
  if (name.includes('gà')) return 'fa-drumstick-bite'
  if (name.includes('heo') || name.includes('sườn')) return 'fa-drumstick-bite'
  return 'fa-utensils'
}

// Xử lý lỗi khi load ảnh
function handleImageError(event) {
  const img = event.target
  const parent = img.parentElement
  if (parent) {
    img.style.display = 'none'
    const iconPlaceholder = document.createElement('div')
    iconPlaceholder.className = 'w-full h-full flex items-center justify-center bg-gray-100'
    const dishName = img.alt || 'Dish'
    const iconClass = getDishIcon(dishName)
    iconPlaceholder.innerHTML = `<i class="fas ${iconClass} text-2xl text-gray-400"></i>`
    parent.appendChild(iconPlaceholder)
  }
}

// Đếm số món đã served
const servedItemsCount = computed(() => {
  if (!selectedOrder.value || !selectedOrder.value.items) return 0
  return selectedOrder.value.items.filter(item => isItemServed(item)).length
})

// Kiểm tra tất cả món đã served chưa
const allItemsServed = computed(() => {
  if (!selectedOrder.value || !selectedOrder.value.items || selectedOrder.value.items.length === 0) return false
  return selectedOrder.value.items.every(item => isItemServed(item))
})

async function handleItemServedClick(itemId, event) {
  if (!selectedOrder.value || !selectedOrder.value.items) {
    event.preventDefault()
    return
  }
  
  const item = selectedOrder.value.items.find(i => i.id === itemId)
  if (!item) {
    event.preventDefault()
    return
  }
  
  // Nếu đã served thì không cho phép bỏ
  if (isItemServed(item)) {
    event.preventDefault()
    notification.error('Món đã lên không thể hủy')
    return
  }
  
  // Nếu chưa served, preventDefault để kiểm soát việc tick
  event.preventDefault()
  
  // Confirm trước khi đánh dấu món đã lên
  if (!confirm(`Xác nhận đã lên đủ số lượng món "${item.dishName}"?`)) {
    return
  }
  
  // Nếu confirm, update và checkbox sẽ tự động tick sau khi data được reload
  await updateItemServedStatus(itemId, true)
}

// Cập nhật notes của order_detail để đánh dấu served
// Nếu có order_detail khác cùng dishId và notes đã served, tăng quantity và xóa order_detail hiện tại
async function updateItemServedStatus(itemId, served) {
  if (!selectedOrder.value || !selectedOrder.value.items) return
  
  try {
    const item = selectedOrder.value.items.find(i => i.id === itemId)
    if (!item) return
    
    const dishId = item.dishId || item.dish?.id
    if (!dishId) return
    
    // Lấy notes để so sánh (loại bỏ SERVED_KEYWORD)
    const itemNotes = item.notes || ''
    const regex = new RegExp(SERVED_KEYWORD, 'gi')
    const cleanNotes = itemNotes.replace(regex, '').trim() || null
    
    // Tìm order_detail khác cùng dishId và notes đã được served
    const existingServedItem = selectedOrder.value.items.find(otherItem => {
      if (otherItem.id === itemId) return false // Bỏ qua chính nó
      
      const otherDishId = otherItem.dishId || otherItem.dish?.id
      if (otherDishId !== dishId) return false
      
      // Kiểm tra đã served chưa
      if (!isItemServed(otherItem)) return false
      
      // So sánh notes (loại bỏ SERVED_KEYWORD)
      const otherNotes = otherItem.notes || ''
      const cleanOtherNotes = otherNotes.replace(regex, '').trim() || null
      
      return cleanOtherNotes === cleanNotes
    })
    
    if (existingServedItem) {
      // Nếu có order_detail khác cùng dishId và notes đã served:
      // 1. Tăng quantity của order_detail đó
      // 2. Xóa order_detail hiện tại
      const newQuantity = (existingServedItem.quantity || 1) + (item.quantity || 1)
      const newSubtotal = existingServedItem.price * newQuantity
      
      // Update quantity của order_detail đã có
      const updateItemData = {
        dishId: existingServedItem.dishId || existingServedItem.dish?.id,
        quantity: newQuantity,
        notes: existingServedItem.notes // Giữ nguyên notes (đã có SERVED_KEYWORD)
      }
      
      await orderService.updateOrderDetail(selectedOrder.value.id, existingServedItem.id, updateItemData)
      
      // Xóa order_detail hiện tại
      await orderService.deleteOrderDetail(selectedOrder.value.id, itemId)
      
      notification.success('Đã cập nhật số lượng món đã lên')
    } else {
      // Nếu không có, chỉ đánh dấu served như bình thường
      let currentNotes = item.notes || ''
      currentNotes = currentNotes.replace(regex, '').trim()
      
      const newNotes = currentNotes ? `${currentNotes} ${SERVED_KEYWORD}` : SERVED_KEYWORD
      
      const updateItemData = {
        dishId: item.dishId || item.dish?.id,
        quantity: item.quantity,
        notes: newNotes
      }
      
      await orderService.updateOrderDetail(selectedOrder.value.id, itemId, updateItemData)
    }
    
    // Reload order để lấy dữ liệu mới nhất
    await loadOrder()
    
    emit('order-updated')
  } catch (error) {
    console.error('Error updating item served status:', error)
    notification.error('Không thể cập nhật trạng thái món: ' + (error.response?.data?.message || error.message))
  }
}

// Hàm checkAndUpdateOrderStatus đã được loại bỏ - không cần cập nhật status order nữa
// Chỉ cần update từ khóa SERVED_KEYWORD trong notes của order_detail

async function markAllAsServed() {
  if (!selectedOrder.value || !selectedOrder.value.items) return

  if (!confirm('Xác nhận đã lên đủ số lượng món cho đơn hàng này?')) return

  try {
    // Đánh dấu tất cả items chưa served là served
    const unservedItems = selectedOrder.value.items.filter(item => !isItemServed(item))
    
    for (const item of unservedItems) {
      await updateItemServedStatus(item.id, true)
    }
    
    notification.success('Đã cập nhật: Đã lên món tất cả')
    await loadOrder()
    emit('order-updated')
  } catch (error) {
    console.error('Error updating items:', error)
    notification.error('Không thể cập nhật: ' + (error.response?.data?.message || error.message))
  }
}

async function markSelectedAsServed() {
  if (!selectedOrder.value) return

  showServedDropdown.value = false
  
  // Tìm các items chưa served
  const unservedItems = selectedOrder.value.items.filter(item => !isItemServed(item))
  
  if (unservedItems.length === 0) {
    notification.info('Tất cả món đã được đánh dấu là đã lên')
    return
  }

  if (!confirm(`Xác nhận đã lên món cho ${unservedItems.length} món chưa lên?`)) return

  try {
    // Đánh dấu tất cả items chưa served là served - cập nhật từng item theo id
    const updatePromises = unservedItems.map(async (item) => {
      let currentNotes = item.notes || ''
      const regex = new RegExp(SERVED_KEYWORD, 'gi')
      currentNotes = currentNotes.replace(regex, '').trim()
      const newNotes = currentNotes ? `${currentNotes} ${SERVED_KEYWORD}` : SERVED_KEYWORD
      
      const updateItemData = {
        dishId: item.dishId || item.dish?.id,
        quantity: item.quantity,
        notes: newNotes
      }
      
      return orderService.updateOrderDetail(selectedOrder.value.id, item.id, updateItemData)
    })
    
    await Promise.all(updatePromises)
    notification.success(`Đã đánh dấu ${unservedItems.length} món đã lên món`)
    await loadOrder()
    emit('order-updated')
  } catch (error) {
    console.error('Error marking items as served:', error)
    notification.error('Không thể đánh dấu món: ' + (error.response?.data?.message || error.message))
  }
}

async function proceedToPayment() {
  if (!selectedOrder.value) return
  
  // Redirect sang trang thanh toán
  router.push(`/admin/payment/${selectedOrder.value.id}`)
}

async function loadPromotions() {
  try {
    const response = await promotionService.getActive()
    if (response.success) {
      promotions.value = response.data || []
    }
  } catch (error) {
    console.error('Error loading promotions:', error)
  }
}

// Kiểm tra promotion có đủ điều kiện không
function isPromotionEligible(promotion) {
  if (!selectedOrder.value || !promotion) return false
  
  const subtotal = selectedOrder.value.subtotal || 0
  
  // Kiểm tra minOrderValue
  if (promotion.minOrderValue && subtotal < promotion.minOrderValue) {
    return false
  }
  
  return true
}

// Watch để reset promotion nếu không còn đủ điều kiện
watch([() => selectedOrder.value?.subtotal, selectedPromotion], ([subtotal, promotion]) => {
  if (promotion && !isPromotionEligible(promotion)) {
    selectedPromotion.value = null
  }
})

// Tính discount từ promotion
function getPromotionDiscount() {
  if (!selectedOrder.value || !selectedPromotion.value) return 0
  
  const promotion = selectedPromotion.value
  const subtotal = selectedOrder.value.subtotal || 0
  
  // Kiểm tra minOrderValue
  if (promotion.minOrderValue && subtotal < promotion.minOrderValue) {
    return 0
  }
  
  if (promotion.discountPercent) {
    const discount = (subtotal * promotion.discountPercent) / 100
    const maxDiscount = promotion.maxDiscount || discount
    return Math.min(discount, maxDiscount)
  } else if (promotion.discountAmount) {
    return promotion.discountAmount
  }
  
  return 0
}

// Hủy đơn hàng
async function cancelOrder() {
  if (!selectedOrder.value) return
  
  if (!confirm(`Bạn có chắc muốn hủy đơn hàng ${selectedOrder.value.orderNumber || selectedOrder.value.id}?`)) {
    return
  }
  
  try {
    await orderService.updateStatus(selectedOrder.value.id, 'CANCELLED')
    notification.success('Đã hủy đơn hàng')
    await loadOrder()
    emit('order-updated')
  } catch (error) {
    console.error('Error cancelling order:', error)
    notification.error('Không thể hủy đơn hàng: ' + (error.response?.data?.message || error.message))
  }
}

// Hủy từng món (xóa order_detail)
async function removeOrderItem(itemId) {
  if (!selectedOrder.value || !itemId) return
  
  const item = selectedOrder.value.items.find(i => i.id === itemId)
  if (!item) return
  
  // Kiểm tra món đã lên chưa - không cho phép xóa món đã lên
  if (isItemServed(item)) {
    notification.error('Không thể hủy món đã được xác nhận đã lên')
    return
  }
  
  // Kiểm tra nếu đây là món duy nhất trong đơn hàng
  if (selectedOrder.value.items && selectedOrder.value.items.length === 1) {
    notification.error('Không thể hủy món duy nhất trong đơn hàng')
    return
  }
  
  if (!confirm(`Bạn có chắc muốn hủy món "${item.dishName}" khỏi đơn hàng?`)) {
    return
  }
  
  try {
    // Gọi API xóa order_detail
    await orderService.deleteOrderDetail(selectedOrder.value.id, itemId)
    notification.success('Đã hủy món')
    await loadOrder()
    emit('order-updated')
  } catch (error) {
    console.error('Error removing order item:', error)
    notification.error('Không thể hủy món: ' + (error.response?.data?.message || error.message))
  }
}

function formatCurrency(value) {
  if (!value) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}

function getOrderStatusLabel(status) {
  const labels = {
    'PENDING': 'Chờ xử lý',
    'CONFIRMED': 'Đã xác nhận',
    'PREPARING': 'Đang chuẩn bị',
    'READY': 'Sẵn sàng',
    'SERVED': 'Đã lên đủ',
    'COMPLETED': 'Hoàn thành',
    'CANCELLED': 'Đã hủy'
  }
  return labels[status] || status
}

function getOrderStatusClass(status) {
  const classes = {
    'PENDING': 'bg-yellow-100 text-yellow-800',
    'CONFIRMED': 'bg-blue-100 text-blue-800',
    'PREPARING': 'bg-orange-100 text-orange-800',
    'READY': 'bg-purple-100 text-purple-800',
    'SERVED': 'bg-green-100 text-green-800',
    'COMPLETED': 'bg-gray-100 text-gray-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

function formatDateTime(datetime) {
  if (!datetime) return '-'
  try {
    const date = new Date(datetime)
    if (isNaN(date.getTime())) return '-'
    return date.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('Error formatting datetime:', error)
    return '-'
  }
}
</script>

