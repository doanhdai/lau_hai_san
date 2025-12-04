<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Main Content -->
    <div class="container mx-auto px-4 py-6">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-stretch">
        <!-- Left Column: Order Form -->
        <div class="lg:col-span-2 flex flex-col">
          <!-- Tabs Navigation -->
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-0 overflow-hidden flex-1 flex flex-col">
            <!-- Dish Selection -->
            <div class="p-6 flex-1 flex flex-col min-h-0">
              <!-- Search -->
              <div class="mb-4 flex-shrink-0">
                <input
                  v-model="dishSearchQuery"
                  type="text"
                  placeholder="Tìm kiếm món ăn..."
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                />
              </div>

              <!-- Dish Grid -->
              <div class="grid grid-cols-2 md:grid-cols-3 gap-3 overflow-y-auto h-full items-start" style="min-height: 450px;">
                <div 
                  v-for="dish in filteredDishes" 
                  :key="dish.id"
                  @click="addDishToOrder(dish)"
                  class="border-2 border-gray-200 rounded-lg p-3 cursor-pointer hover:border-red-400 hover:bg-red-50 transition"
                >
                  <div class="flex items-center gap-2">
                    <div class="flex-shrink-0 w-14 h-14 rounded-lg overflow-hidden bg-gray-200 border border-gray-300">
                      <img
                        v-if="dish.imageUrl && dish.imageUrl.trim() !== ''"
                        :src="dish.imageUrl"
                        :alt="dish.name"
                        class="w-full h-full object-cover"
                        @error="handleImageError"
                      />
                      <div v-else class="w-full h-full flex items-center justify-center bg-gray-100">
                        <i :class="['fas', getDishIcon(dish.name)]" class="text-xl text-gray-400"></i>
                      </div>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-900 truncate">{{ dish.name }}</p>
                      <p class="text-xs text-red-600 font-bold">{{ formatCurrency(dish.price) }}</p>
                      <p v-if="dish.estimatedPreparationTime" class="text-xs text-gray-500 mt-0.5">
                        Dự kiến {{ dish.estimatedPreparationTime }}p
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- Right Column: Order Summary -->
        <div class="lg:col-span-1 flex flex-col">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 w-full h-full flex flex-col">
            <h3 class="text-lg font-bold text-gray-900 mb-4">Tóm tắt</h3>
            
            <!-- Order Items -->
            <div class="space-y-3 mb-4 max-h-64 overflow-y-auto">
              <!-- Empty state -->
              <div v-if="orderForm.items.length === 0 && existingOrderDetails.length === 0" class="text-center py-8 text-gray-500">
                Chưa có món nào
              </div>
              
              <!-- Existing order items (from current order) -->
              <template v-if="existingOrderDetails.length > 0">
                <div 
                  v-for="(item, index) in existingOrderDetails" 
                  :key="'existing-' + (item.id || index)"
                  class="p-3 bg-green-50 border border-green-200 rounded-lg space-y-2 relative"
                >
                  <!-- Nút xóa món (chỉ hiển thị nếu món chưa được xác nhận) -->
                  <button
                    v-if="!isItemServed(item)"
                    @click="removeExistingItem(item.id)"
                    class="absolute top-2 right-2 w-6 h-6 flex items-center justify-center text-red-600 hover:text-red-800 hover:bg-red-50 rounded-full transition-colors"
                    title="Hủy món"
                  >
                    <i class="fas fa-times text-xs"></i>
                  </button>
                  
                  <div class="flex items-center gap-2 pr-8">
                    <div class="flex-1">
                      <div class="flex items-center gap-2 flex-wrap">
                        <p class="text-sm font-medium text-gray-900">{{ item.dishName || item.dish?.name || 'Món không xác định' }}</p>
                        <span v-if="isItemServed(item)" class="px-2 py-0.5 text-xs font-semibold rounded-full bg-green-600 text-white">
                          Đã lên
                        </span>
                      </div>
                      <p class="text-xs text-gray-600">{{ formatCurrency(item.price || item.unitPrice || 0) }} × {{ item.quantity }}</p>
                    </div>
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-bold text-green-700">{{ item.quantity }}</span>
                    </div>
                  </div>
                  <div v-if="getCleanedNotes(item.notes)" class="text-xs text-gray-600 italic">
                    Ghi chú: {{ getCleanedNotes(item.notes) }}
                  </div>
                </div>
              </template>
              
              <!-- New items (being added) -->
              <template v-if="orderForm.items.length > 0">
                <div 
                  v-for="(item, index) in orderForm.items" 
                  :key="'new-' + (item.dishId || index)"
                  class="p-3 bg-gray-50 rounded-lg space-y-2"
                >
                  <div class="flex items-center gap-2">
                    <div class="flex-1">
                      <p class="text-sm font-medium text-gray-900">{{ item.dishName }}</p>
                      <p class="text-xs text-gray-600">{{ formatCurrency(item.price) }}</p>
                    </div>
                    <div class="flex items-center gap-2">
                      <button @click="decreaseQuantity(index)" class="w-6 h-6 bg-gray-200 rounded hover:bg-gray-300 flex items-center justify-center">
                        -
                      </button>
                      <span class="text-sm font-bold w-8 text-center">{{ item.quantity }}</span>
                      <button @click="increaseQuantity(index)" class="w-6 h-6 bg-red-500 text-white rounded hover:bg-red-600 flex items-center justify-center">
                        +
                      </button>
                      <button @click="removeItem(index)" class="w-6 h-6 bg-red-500 text-white rounded hover:bg-red-600 flex items-center justify-center">
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
              </template>
            </div>

            <!-- Pricing -->
            <div class="space-y-2 pt-4 border-t flex-shrink-0">
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
            <div class="mt-4 flex-shrink-0">
              <label class="block text-sm font-medium text-gray-700 mb-2">Ghi chú</label>
              <textarea
                v-model="orderForm.notes"
                rows="2"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                placeholder="Ghi chú đơn hàng..."
              ></textarea>
            </div>

            <!-- Actions -->
            <div class="mt-4 space-y-2 flex-shrink-0">
              <button 
                @click="addItemsToOrder" 
                :disabled="!canAddItems"
                :class="[
                  'w-full font-medium py-2.5 px-4 rounded-lg transition-colors',
                  canAddItems 
                    ? 'bg-red-600 hover:bg-red-700 text-white cursor-pointer' 
                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                ]"
              >
                Thêm món
              </button>
              <button
                @click="clearOrder"
                class="w-full bg-gray-200 hover:bg-gray-300 text-gray-700 font-medium py-2.5 px-4 rounded-lg transition-colors"
              >
                Xóa tất cả
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { dishService } from '@/services/dishService'
import { orderService } from '@/services/orderService'
import { useNotificationStore } from '@/stores/notification'
import { publicApiClient } from '@/services/api'

const route = useRoute()
const router = useRouter()
const notification = useNotificationStore()

// Removed activeTab - only showing dishes tab now
const dishSearchQuery = ref('')
const dishes = ref([])
const existingOrder = ref(null)
const existingOrderDetails = ref([])

const orderForm = ref({
  orderId: null,
  reservationId: null,
  tableId: null,
  items: [],
  notes: ''
})

const filteredDishes = computed(() => {
  if (!dishSearchQuery.value) return dishes.value
  const query = dishSearchQuery.value.toLowerCase()
  return dishes.value.filter(dish => 
    dish.name.toLowerCase().includes(query) &&
    dish.status !== 'DISCONTINUED'
  )
})

const existingOrderTotal = computed(() => {
  return existingOrderDetails.value.reduce((sum, item) => {
    return sum + (item.subtotal || (item.price * item.quantity))
  }, 0)
})

const subtotal = computed(() => {
  const newItemsTotal = orderForm.value.items.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)
  return existingOrderTotal.value + newItemsTotal
})

const tax = computed(() => {
  return subtotal.value * 0.1
})

const total = computed(() => {
  return subtotal.value + tax.value
})

const canAddItems = computed(() => {
  return orderForm.value.items.length > 0
})

onMounted(async () => {
  await loadDishes()
  await handleQueryParams()
})

async function loadDishes() {
  try {
    // Sử dụng public API để không cần auth
    const response = await publicApiClient.get('/dishes/available')
    if (response.data && response.data.success) {
      dishes.value = response.data.data.filter(d => d.status !== 'DISCONTINUED')
    } else if (Array.isArray(response.data)) {
      dishes.value = response.data.filter(d => d.status !== 'DISCONTINUED')
    }
  } catch (error) {
    console.error('Error loading dishes:', error)
    notification.error('Không thể tải danh sách món ăn: ' + (error.response?.data?.message || error.message))
  }
}

async function handleQueryParams() {
  const reservationId = route.query.reservationId || route.query.reservation_id
  const tableId = route.query.tableId || route.query.table_id
  
  // Ưu tiên tableId nếu có (vì có thể URL có cả reservationId và tableId)
  // Nếu có tableId, tìm order theo tableId trước
  if (tableId && tableId !== 'null' && tableId !== 'undefined') {
    const tableIdNum = parseInt(tableId)
    if (!isNaN(tableIdNum)) {
      orderForm.value.tableId = tableIdNum
      
      // Tìm order theo tableId (sử dụng public endpoint - không cần auth)
      try {
        const response = await orderService.getOrderByTableIdPublic(tableIdNum)
        if (response.success && response.data) {
          const foundOrder = response.data
          orderForm.value.orderId = foundOrder.id
          // Set reservationId từ order (nếu có)
          orderForm.value.reservationId = foundOrder.reservationId || null
          await loadExistingOrderDetails(foundOrder.id)
          return // Đã tìm thấy order, không cần xử lý reservationId nữa
        }
      } catch (error) {
        console.error('Error loading order:', error)
        // Nếu không tìm thấy order, có thể chưa có order cho bàn này
        // User sẽ cần liên hệ nhân viên để tạo order
      }
    }
  }
  
  // Nếu không tìm thấy order theo tableId, thử tìm theo reservationId
  if (reservationId && reservationId !== 'null' && reservationId !== 'undefined') {
    const reservationIdNum = parseInt(reservationId)
    if (!isNaN(reservationIdNum)) {
      orderForm.value.reservationId = reservationIdNum
      orderForm.value.tableId = null
      
      // Tìm order theo reservationId
      try {
        const response = await orderService.getByReservationId(reservationIdNum)
        let ordersData = []
        if (response.success && response.data) {
          if (Array.isArray(response.data)) {
            ordersData = response.data
          } else if (response.data.data && Array.isArray(response.data.data)) {
            ordersData = response.data.data
          }
        } else if (Array.isArray(response)) {
          ordersData = response
        }
        
        // Lấy order chưa thanh toán mới nhất
        const matchingOrders = ordersData
          .filter(o => o.status !== 'CANCELLED' && o.status !== 'COMPLETED')
          .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
        
        const foundOrder = matchingOrders.length > 0 ? matchingOrders[0] : null
        
        if (foundOrder) {
          orderForm.value.orderId = foundOrder.id
          await loadExistingOrderDetails(foundOrder.id)
        }
      } catch (error) {
        console.error('Error loading order:', error)
      }
    }
  }
}

async function loadExistingOrderDetails(orderId) {
  try {
    // Sử dụng public endpoint nếu có, nếu không thì dùng endpoint thường (có thể fail nếu không auth)
    let orderRes
    try {
      // Thử dùng endpoint public trước (nếu có)
      const tableId = orderForm.value.tableId
      if (tableId) {
        orderRes = await orderService.getOrderByTableIdPublic(tableId)
      } else {
        orderRes = await orderService.getById(orderId)
      }
    } catch (error) {
      // Nếu public endpoint không có, thử endpoint thường
      orderRes = await orderService.getById(orderId)
    }
    
    if (orderRes.success && orderRes.data) {
      existingOrder.value = orderRes.data
      existingOrderDetails.value = orderRes.data.items || orderRes.data.orderDetails || []
    }
  } catch (error) {
    console.error('Error loading existing order details:', error)
    existingOrder.value = null
    existingOrderDetails.value = []
  }
}

function addDishToOrder(dish) {
  if (dish.status === 'DISCONTINUED') {
    notification.error('Món ăn "' + dish.name + '" đã dừng kinh doanh.')
    return
  }
  
  if (!Array.isArray(orderForm.value.items)) {
    orderForm.value.items = []
  }
  
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
    orderForm.value.items = []
    orderForm.value.notes = ''
  }
}

async function addItemsToOrder() {
  console.log('addItemsToOrder called', {
    canAddItems: canAddItems.value,
    itemsCount: orderForm.value.items.length,
    orderId: orderForm.value.orderId,
    tableId: orderForm.value.tableId,
    reservationId: orderForm.value.reservationId
  })

  if (!canAddItems.value) {
    notification.error('Vui lòng thêm ít nhất một món')
    return
  }

  // Nếu chưa có orderId, thử tìm lại order
  if (!orderForm.value.orderId) {
    const tableId = orderForm.value.tableId
    const reservationId = orderForm.value.reservationId
    
    console.log('No orderId found, trying to find order...', { tableId, reservationId })
    
    if (tableId) {
      try {
        const response = await orderService.getOrderByTableIdPublic(tableId)
        console.log('getOrderByTableIdPublic response:', response)
        if (response.success && response.data) {
          orderForm.value.orderId = response.data.id
          orderForm.value.reservationId = response.data.reservationId || null
          await loadExistingOrderDetails(response.data.id)
          console.log('Found order by tableId:', response.data.id)
        } else {
          // Nếu không có order, vẫn cho phép user thêm món (sẽ tạo order mới)
          console.log('No existing order found, will create new order when adding items')
          notification.warning('Chưa có đơn hàng cho bàn này. Món sẽ được thêm vào đơn hàng mới.')
          // Không return, cho phép tiếp tục để tạo order mới
        }
      } catch (error) {
        console.error('Error finding order by tableId:', error)
        // Nếu 404, có nghĩa là chưa có order - vẫn cho phép tiếp tục
        if (error.response?.status === 404) {
          console.log('No order found (404), will create new order when adding items')
          notification.warning('Chưa có đơn hàng cho bàn này. Món sẽ được thêm vào đơn hàng mới.')
          // Không return, cho phép tiếp tục
        } else {
          notification.error('Không thể tải thông tin đơn hàng: ' + (error.response?.data?.message || error.message))
          return
        }
      }
    } else if (reservationId) {
      try {
        const response = await orderService.getByReservationId(reservationId)
        let ordersData = []
        if (response.success && response.data) {
          if (Array.isArray(response.data)) {
            ordersData = response.data
          } else if (response.data.data && Array.isArray(response.data.data)) {
            ordersData = response.data.data
          }
        }
        
        const matchingOrders = ordersData
          .filter(o => o.status !== 'CANCELLED' && o.status !== 'COMPLETED')
          .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
        
        if (matchingOrders.length > 0) {
          orderForm.value.orderId = matchingOrders[0].id
          await loadExistingOrderDetails(matchingOrders[0].id)
          console.log('Found order by reservationId:', matchingOrders[0].id)
        } else {
          notification.error('Không tìm thấy đơn hàng cho đặt bàn này. Vui lòng liên hệ nhân viên.')
          return
        }
      } catch (error) {
        console.error('Error finding order by reservationId:', error)
        notification.error('Không tìm thấy đơn hàng. Vui lòng liên hệ nhân viên.')
        return
      }
    } else {
      notification.error('Không tìm thấy đơn hàng. Vui lòng liên hệ nhân viên.')
      return
    }
  }

  // Nếu không có orderId, vẫn cho phép thêm món (sẽ tạo order mới)
  // Backend sẽ tự động tạo order mới nếu orderId không tồn tại
  
  try {
    const itemsData = orderForm.value.items.map(item => ({
      dishId: item.dishId,
      quantity: item.quantity,
      notes: item.notes || ''
    }))

    const updateData = {
      notes: orderForm.value.notes || '',
      subtotal: subtotal.value,
      tax: tax.value,
      total: total.value,
      reservationId: orderForm.value.reservationId || null,
      tableId: orderForm.value.reservationId ? null : (orderForm.value.tableId || null),
      items: itemsData,
      status: 'CONFIRMED'
    }
    
    // Sử dụng orderId nếu có, nếu không thì truyền 0 (backend sẽ tạo mới)
    const orderIdToUse = orderForm.value.orderId || 0
    
    console.log('Calling addItemsToOrderPublic with:', {
      orderId: orderIdToUse,
      itemsCount: itemsData.length,
      updateData
    })
    
    // Sử dụng public endpoint để thêm món (không cần auth)
    // Nếu orderId = 0, backend sẽ tạo order mới
    const response = await orderService.addItemsToOrderPublic(orderIdToUse, updateData)
    
    console.log('addItemsToOrderPublic response:', response)
    
    if (response.success) {
      // Lưu orderId từ response (nếu tạo order mới)
      if (response.data && response.data.id) {
        orderForm.value.orderId = response.data.id
        console.log('Order ID saved:', response.data.id)
      }
      
      notification.success('Đã thêm món vào đơn hàng')
      
      // Reload order details
      if (orderForm.value.orderId) {
        await loadExistingOrderDetails(orderForm.value.orderId)
      }
      
      // Clear new items
      orderForm.value.items = []
      orderForm.value.notes = ''
      
      // Reload existing order details after adding items
    } else {
      notification.error(response.message || 'Không thể thêm món vào đơn hàng')
    }
  } catch (error) {
    console.error('Error in addItemsToOrder:', error)
    console.error('Error response:', error.response)
    const errorMessage = error.response?.data?.message || error.response?.data?.error || error.message || 'Không thể thêm món'
    notification.error('Không thể thêm món: ' + errorMessage)
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}

function getCleanedNotes(notes) {
  if (!notes) return null
  let cleaned = notes
  const servedRegex = /\bserved\b/gi
  cleaned = cleaned.replace(servedRegex, '').trim()
  return cleaned || null
}

// Kiểm tra món đã được xác nhận (đã lên) chưa
function isItemServed(item) {
  if (!item || !item.notes) return false
  const notes = item.notes.toLowerCase()
  return notes.includes('served')
}

// Xóa món từ đơn hàng hiện tại
async function removeExistingItem(itemId) {
  if (!itemId || !orderForm.value.orderId) {
    notification.error('Không thể xóa món. Vui lòng thử lại.')
    return
  }

  const item = existingOrderDetails.value.find(i => i.id === itemId)
  if (!item) {
    notification.error('Không tìm thấy món cần xóa.')
    return
  }

  // Kiểm tra món đã lên chưa
  if (isItemServed(item)) {
    notification.error('Không thể hủy món đã được xác nhận đã lên.')
    return
  }

  if (!confirm(`Bạn có chắc muốn hủy món "${item.dishName || item.dish?.name || 'Món này'}" khỏi đơn hàng?`)) {
    return
  }

  try {
    // Sử dụng public endpoint để xóa món (không cần auth)
    await orderService.deleteOrderDetailPublic(orderForm.value.orderId, itemId)
    
    notification.success('Đã hủy món khỏi đơn hàng')
    
    // Reload order details
    await loadExistingOrderDetails(orderForm.value.orderId)
  } catch (error) {
    console.error('Error removing item:', error)
    notification.error('Không thể hủy món: ' + (error.response?.data?.message || error.message))
  }
}

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

function handleImageError(event) {
  const img = event.target
  const parent = img.parentElement
  if (parent) {
    img.style.display = 'none'
    const iconPlaceholder = document.createElement('div')
    iconPlaceholder.className = 'w-full h-full flex items-center justify-center bg-gray-100'
    const dishName = img.alt || 'Dish'
    const iconClass = getDishIcon(dishName)
    iconPlaceholder.innerHTML = `<i class="fas ${iconClass} text-lg text-gray-400"></i>`
    parent.appendChild(iconPlaceholder)
  }
}

function goBack() {
  window.history.back()
}
</script>

