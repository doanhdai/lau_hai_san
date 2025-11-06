<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Đặt hàng tại quầy</h1>
        <p class="text-gray-600 mt-1">Tạo đơn hàng nhanh cho khách hàng tại quầy</p>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Menu Selection -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Search -->
        <div class="card">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm món ăn..."
            class="input-field"
          />
        </div>

        <!-- Dishes Grid -->
        <div v-if="loading" class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div v-for="n in 6" :key="n" class="card animate-pulse">
            <div class="aspect-video bg-gray-200 rounded-lg mb-3"></div>
            <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
            <div class="h-6 bg-gray-200 rounded w-1/2"></div>
          </div>
        </div>

        <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div
            v-for="dish in filteredDishes"
            :key="dish.id"
            @click="addToOrder(dish)"
            class="card cursor-pointer hover:shadow-lg transition-all hover:-translate-y-1"
          >
            <img
              v-if="dish.imageUrl"
              :src="dish.imageUrl"
              :alt="dish.name"
              class="w-full aspect-video object-cover rounded-lg mb-3"
            />
            <div v-else class="w-full aspect-video bg-gray-200 rounded-lg mb-3 flex items-center justify-center">
              <span class="text-4xl">🍜</span>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">{{ dish.name }}</h3>
            <p class="text-primary-600 font-bold">{{ formatCurrency(dish.price) }}</p>
          </div>
        </div>
      </div>

      <!-- Order Summary -->
      <div class="lg:col-span-1">
        <div class="card sticky top-4">
          <h2 class="text-xl font-bold text-gray-900 mb-4">Đơn hàng</h2>

          <!-- Customer Info -->
          <div class="space-y-3 mb-4">
            <input
              v-model="order.customerName"
              type="text"
              placeholder="Tên khách hàng (tùy chọn)"
              class="input-field"
            />
            <input
              v-model="order.customerPhone"
              type="text"
              placeholder="Số điện thoại (tùy chọn)"
              class="input-field"
            />
          </div>

          <!-- Order Items -->
          <div v-if="order.items.length === 0" class="text-center py-8 text-gray-500">
            <div class="text-5xl mb-2">🛒</div>
            <p>Chưa có món nào</p>
          </div>

          <div v-else class="space-y-3 mb-4 max-h-96 overflow-y-auto">
            <div
              v-for="(item, index) in order.items"
              :key="index"
              class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg"
            >
              <div class="flex-1">
                <p class="font-medium text-sm">{{ item.dishName }}</p>
                <p class="text-xs text-gray-500">{{ formatCurrency(item.price) }}</p>
              </div>
              <div class="flex items-center gap-2">
                <button
                  @click="decreaseQuantity(index)"
                  class="w-8 h-8 flex items-center justify-center bg-white border rounded hover:bg-gray-50"
                >
                  -
                </button>
                <span class="w-8 text-center font-medium">{{ item.quantity }}</span>
                <button
                  @click="increaseQuantity(index)"
                  class="w-8 h-8 flex items-center justify-center bg-white border rounded hover:bg-gray-50"
                >
                  +
                </button>
                <button
                  @click="removeItem(index)"
                  class="ml-2 text-red-600 hover:text-red-700"
                >
                  ✕
                </button>
              </div>
            </div>
          </div>

          <!-- Total -->
          <div v-if="order.items.length > 0" class="border-t pt-4 space-y-2">
            <div class="flex justify-between text-sm">
              <span>Tạm tính:</span>
              <span>{{ formatCurrency(subtotal) }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span>Thuế VAT (10%):</span>
              <span>{{ formatCurrency(tax) }}</span>
            </div>
            <div class="flex justify-between text-lg font-bold text-primary-600">
              <span>Tổng cộng:</span>
              <span>{{ formatCurrency(total) }}</span>
            </div>

            <!-- Notes -->
            <textarea
              v-model="order.notes"
              placeholder="Ghi chú đơn hàng..."
              class="input-field mt-3"
              rows="2"
            ></textarea>

            <!-- Action Buttons -->
            <div class="space-y-2 mt-4">
              <button
                @click="submitOrder"
                :disabled="submitting"
                class="btn-primary w-full"
              >
                {{ submitting ? 'Đang xử lý...' : 'Xác nhận đơn hàng' }}
              </button>
              <button
                @click="clearOrder"
                class="btn-secondary w-full"
              >
                Hủy đơn hàng
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
import { dishService } from '@/services/dishService'
import { orderService } from '@/services/orderService'
import { useNotificationStore } from '@/stores/notification'

const notification = useNotificationStore()

const loading = ref(false)
const submitting = ref(false)
const dishes = ref([])
const searchQuery = ref('')

const order = ref({
  customerName: '',
  customerPhone: '',
  items: [],
  notes: ''
})

const filteredDishes = computed(() => {
  if (!searchQuery.value) return dishes.value
  
  const query = searchQuery.value.toLowerCase()
  return dishes.value.filter(dish =>
    dish.name.toLowerCase().includes(query)
  )
})

const subtotal = computed(() => {
  return order.value.items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
})

const tax = computed(() => {
  return subtotal.value * 0.1
})

const total = computed(() => {
  return subtotal.value + tax.value
})

onMounted(() => {
  loadDishes()
})

async function loadDishes() {
  loading.value = true
  try {
    const response = await dishService.getAvailable()
    dishes.value = response.data
  } catch (error) {
    console.error('Error loading dishes:', error)
    notification.error('Không thể tải danh sách món ăn')
  } finally {
    loading.value = false
  }
}

function addToOrder(dish) {
  const existingItem = order.value.items.find(item => item.dishId === dish.id)
  
  if (existingItem) {
    existingItem.quantity++
  } else {
    order.value.items.push({
      dishId: dish.id,
      dishName: dish.name,
      price: dish.price,
      quantity: 1,
      notes: ''
    })
  }
}

function increaseQuantity(index) {
  order.value.items[index].quantity++
}

function decreaseQuantity(index) {
  if (order.value.items[index].quantity > 1) {
    order.value.items[index].quantity--
  }
}

function removeItem(index) {
  order.value.items.splice(index, 1)
}

function clearOrder() {
  if (confirm('Bạn có chắc muốn hủy đơn hàng này?')) {
    order.value = {
      customerName: '',
      customerPhone: '',
      items: [],
      notes: ''
    }
  }
}

async function submitOrder() {
  if (order.value.items.length === 0) {
    notification.error('Vui lòng chọn ít nhất một món')
    return
  }

  submitting.value = true
  try {
    const orderData = {
      customerName: order.value.customerName || null,
      customerPhone: order.value.customerPhone || null,
      items: order.value.items.map(item => ({
        dishId: item.dishId,
        quantity: item.quantity,
        notes: item.notes
      })),
      notes: order.value.notes
    }

    const response = await orderService.createCounterOrder(orderData)
    
    if (response.success) {
      notification.success('Tạo đơn hàng thành công')
      
      // Print/show invoice option
      if (confirm('Đơn hàng đã được tạo. Bạn có muốn xem hóa đơn?')) {
        // TODO: Open invoice view
      }
      
      clearOrder()
    }
  } catch (error) {
    console.error('Error creating order:', error)
    notification.error('Không thể tạo đơn hàng')
  } finally {
    submitting.value = false
  }
}

function formatCurrency(amount) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}
</script>
