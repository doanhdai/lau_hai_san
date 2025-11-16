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
                  class="bg-white border rounded-lg p-4 hover:shadow-md transition-shadow"
                  :class="isItemServed(item) ? 'border-green-300 bg-green-50' : 'border-slate-200'"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="flex items-start gap-3 flex-1">
                      <div class="flex items-center gap-2 pt-1">
                        <input
                          type="checkbox"
                          :checked="isItemServed(item)"
                          @click="handleItemServedClick(item.id, $event)"
                          class="w-4 h-4 text-green-600 border-gray-300 rounded focus:ring-green-500"
                          :disabled="isItemServed(item) || selectedOrder.status === 'COMPLETED' || selectedOrder.status === 'CANCELLED'"
                        />
                      </div>
                      <div class="flex-1">
                        <div class="flex items-center gap-3">
                          <div class="flex-1">
                            <div class="flex items-center gap-2">
                              <p class="text-sm font-semibold text-slate-900">{{ item.dishName }}</p>
                              <span v-if="isItemServed(item)" class="text-xs bg-green-500 text-white px-2 py-0.5 rounded-full font-medium">
                                <i class="fas fa-check mr-1"></i>Đã lên đủ
                              </span>
                              <span v-else class="text-xs bg-gray-200 text-gray-700 px-2 py-0.5 rounded-full font-medium">
                                Chưa lên
                              </span>
                            </div>
                            <div class="flex items-center gap-4 mt-1">
                              <span class="text-xs text-slate-500">SL: {{ item.quantity }}</span>
                              <span class="text-xs text-slate-500">Giá: {{ formatCurrency(item.price) }}</span>
                            </div>
                            <p v-if="getDisplayNotes(item)" class="text-xs text-slate-600 mt-2 italic bg-slate-50 p-2 rounded">
                              <i class="fas fa-sticky-note mr-1"></i>{{ getDisplayNotes(item) }}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="text-right flex items-center gap-3">
                      <p class="text-sm font-bold text-slate-900">{{ formatCurrency(item.subtotal) }}</p>
                      <button
                        v-if="selectedOrder.status !== 'COMPLETED' && selectedOrder.status !== 'CANCELLED'"
                        @click="removeOrderItem(item.id)"
                        class="text-red-600 hover:text-red-800 transition-colors p-1"
                        title="Hủy món"
                      >
                        <i class="fas fa-times text-sm"></i>
                      </button>
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

    <!-- Payment Dialog Step 1 -->
    <Teleport to="body">
      <div
        v-if="showPaymentDialog && !showPaymentStep2"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[10000] p-4"
        @click.self="closePaymentDialog"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
          <div class="bg-slate-900 text-white px-6 py-4 flex items-center justify-between">
            <h3 class="text-xl font-bold">Thanh toán đơn hàng</h3>
            <button @click="closePaymentDialog" class="text-white/80 hover:text-white transition-colors">
              <i class="fas fa-times text-xl"></i>
            </button>
          </div>

          <div class="flex-1 overflow-y-auto p-6">
            <div v-if="selectedOrder" class="space-y-6">
              <!-- Customer Info -->
              <div class="bg-slate-50 rounded-lg p-4">
                <h4 class="text-sm font-semibold text-slate-900 mb-2">Thông tin khách hàng</h4>
                <p class="text-sm text-slate-700">{{ selectedOrder.customerName || 'Khách vãng lai' }}</p>
                <p v-if="selectedOrder.tableNumber" class="text-sm text-slate-700">Bàn: {{ selectedOrder.tableNumber }}</p>
              </div>

              <!-- Order Items -->
              <div>
                <h4 class="text-sm font-semibold text-slate-900 mb-3">Danh sách món</h4>
                <div class="space-y-2">
                  <div
                    v-for="item in selectedOrder.items"
                    :key="item.id"
                    class="flex items-center justify-between p-3 bg-slate-50 rounded-lg"
                  >
                    <div class="flex-1">
                      <p class="text-sm font-medium text-slate-900">{{ item.dishName }}</p>
                      <p class="text-xs text-slate-500">SL: {{ item.quantity }} x {{ formatCurrency(item.price) }}</p>
                    </div>
                    <p class="text-sm font-bold text-slate-900">{{ formatCurrency(item.subtotal) }}</p>
                  </div>
                </div>
              </div>

              <!-- Promotion Selection -->
              <div>
                <h4 class="text-sm font-semibold text-slate-900 mb-3">Khuyến mãi</h4>
                <div class="flex items-center gap-3">
                  <div v-if="selectedPromotion" class="flex-1 p-2 bg-green-50 border border-green-200 rounded-lg">
                    <div class="flex items-center justify-between">
                      <div class="flex-1">
                        <p class="text-sm font-medium text-green-900">
                          {{ selectedPromotion.name || selectedPromotion.promotionName }}
                        </p>
                        <!-- <p v-if="selectedPromotion.description" class="text-xs text-green-700 mt-1">
                          {{ selectedPromotion.description }}
                        </p> -->
                      </div>
                      <span class="text-sm font-medium text-green-600 ml-3">
                        {{ selectedPromotion.discountPercent 
                          ? `${selectedPromotion.discountPercent}%` 
                          : formatCurrency(selectedPromotion.discountAmount) }}
                      </span>
                    </div>
                  </div>
                  <div v-else class="flex-1 p-2 bg-slate-50 border border-slate-200 rounded-lg">
                    <p class="text-sm text-slate-600">Chưa chọn khuyến mãi</p>
                  </div>
                  <button
                    @click="showPromotionDialog = true"
                    class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 whitespace-nowrap"
                  >
                    <i class="fas fa-tag"></i>
                    <span>{{ selectedPromotion ? 'Đổi khuyến mãi' : 'Chọn khuyến mãi' }}</span>
                  </button>
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
                    <span class="text-slate-600">Giảm giá hiện tại:</span>
                    <span class="font-medium text-red-600">-{{ formatCurrency(selectedOrder.discount) }}</span>
                  </div>
                  <div v-if="selectedPromotion" class="flex justify-between text-sm">
                    <span class="text-slate-600">Giảm giá khuyến mãi:</span>
                    <span class="font-medium text-red-600">-{{ formatCurrency(getPromotionDiscount()) }}</span>
                  </div>
                  <div class="flex justify-between text-sm">
                    <span class="text-slate-600">Thuế (10%):</span>
                    <span class="font-medium text-slate-900">{{ formatCurrency(selectedOrder.tax) }}</span>
                  </div>
                  <div class="border-t border-slate-300 pt-2 mt-2">
                    <div class="flex justify-between">
                      <span class="text-base font-bold text-slate-900">Tổng cộng:</span>
                      <span class="text-lg font-bold text-red-600">{{ formatCurrency(finalTotal) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="border-t border-slate-200 bg-slate-50 px-6 py-4 flex items-center justify-between gap-3">
            <button
              @click="closePaymentDialog"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
            >
              Quay lại
            </button>
            <button
              @click="goToPaymentStep2"
              class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Tiếp tục
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Payment Dialog Step 2 -->
    <Teleport to="body">
      <div
        v-if="showPaymentDialog && showPaymentStep2"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[10000] p-4"
        @click.self="closePaymentDialog"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-md max-h-[90vh] overflow-hidden flex flex-col">
          <div class="bg-slate-900 text-white px-6 py-4 flex items-center justify-between">
            <h3 class="text-xl font-bold">Chọn phương thức thanh toán</h3>
            <button @click="closePaymentDialog" class="text-white/80 hover:text-white transition-colors">
              <i class="fas fa-times text-xl"></i>
            </button>
          </div>

          <div class="flex-1 overflow-y-auto p-6">
            <div class="space-y-4">
              <!-- Payment Method Selection -->
              <div>
                <h4 class="text-sm font-semibold text-slate-900 mb-3">Phương thức thanh toán</h4>
                <div class="space-y-2">
                  <label class="flex items-center gap-3 p-4 border-2 rounded-lg cursor-pointer transition-all"
                         :class="paymentMethod === 'CASH' ? 'border-blue-500 bg-blue-50' : 'border-slate-200 hover:border-slate-300'">
                    <input
                      type="radio"
                      v-model="paymentMethod"
                      value="CASH"
                      class="w-4 h-4 text-blue-600"
                      @change="qrCodeUrl = ''"
                    />
                    <div class="flex-1">
                      <p class="font-medium text-slate-900">Tiền mặt</p>
                      <p class="text-xs text-slate-500">Thanh toán bằng tiền mặt</p>
                    </div>
                  </label>
                  <label class="flex items-center gap-3 p-4 border-2 rounded-lg cursor-pointer transition-all"
                         :class="paymentMethod === 'BANK_TRANSFER' ? 'border-blue-500 bg-blue-50' : 'border-slate-200 hover:border-slate-300'">
                    <input
                      type="radio"
                      v-model="paymentMethod"
                      value="BANK_TRANSFER"
                      class="w-4 h-4 text-blue-600"
                    />
                    <div class="flex-1">
                      <p class="font-medium text-slate-900">Chuyển khoản</p>
                      <p class="text-xs text-slate-500">Thanh toán qua chuyển khoản ngân hàng</p>
                    </div>
                  </label>
                </div>
              </div>

              <!-- QR Code for Bank Transfer -->
              <div v-if="paymentMethod === 'BANK_TRANSFER' && qrCodeUrl" class="bg-slate-50 rounded-lg p-4">
                <h4 class="text-sm font-semibold text-slate-900 mb-3">Quét mã QR để thanh toán</h4>
                <div class="flex flex-col items-center">
                  <img :src="qrCodeUrl" alt="QR Code" class="w-64 h-auto border-2 border-slate-200 rounded-lg mb-3" />
                  <div class="text-center text-xs text-slate-600 space-y-1">
                    <p>Số tài khoản: <span class="font-semibold">1234567890</span></p>
                    <p>Chủ tài khoản: <span class="font-semibold">NHÀ HÀNG LẨU HẢI SẢN</span></p>
                    <p>Ngân hàng: <span class="font-semibold">Vietcombank</span></p>
                    <p>Số tiền: <span class="font-semibold text-red-600">{{ formatCurrency(finalTotal) }}</span></p>
                    <p class="text-slate-500 italic">Nội dung: Thanh toan don hang {{ selectedOrder?.orderNumber }}</p>
                  </div>
                </div>
              </div>

              <!-- Total Amount -->
              <div class="bg-gradient-to-br from-slate-50 to-slate-100 rounded-lg p-4 border border-slate-200">
                <div class="flex justify-between items-center">
                  <span class="text-base font-bold text-slate-900">Tổng thanh toán:</span>
                  <span class="text-xl font-bold text-red-600">{{ formatCurrency(finalTotal) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="border-t border-slate-200 bg-slate-50 px-6 py-4 flex items-center justify-between gap-3">
            <button
              @click="backToPaymentStep1"
              class="px-4 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors"
            >
              Quay lại
            </button>
            <button
              @click="handlePayment"
              :disabled="loadingPayment || (paymentMethod === 'BANK_TRANSFER' && !qrCodeUrl)"
              class="px-4 py-2 text-sm font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              <span v-if="loadingPayment" class="inline-block animate-spin">
                <i class="fas fa-spinner"></i>
              </span>
              <span>{{ loadingPayment ? 'Đang xử lý...' : 'Đã nhận' }}</span>
            </button>
          </div>
        </div>
      </div>
    </Teleport>

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
import { orderService } from '@/services/orderService'
import { promotionService } from '@/services/promotionService'
import PromotionSelectionDialog from './PromotionSelectionDialog.vue'
import { paymentService } from '@/services/paymentService'
import { useNotificationStore } from '@/stores/notification'

const notification = useNotificationStore()

// Từ khóa đánh dấu món đã lên (có thể thay đổi dễ dàng)
const SERVED_KEYWORD = 'served'

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
const loadingPayment = ref(false)
const showPaymentDialog = ref(false)
const showPaymentStep2 = ref(false)
const showPromotionDialog = ref(false)
const selectedPromotion = ref(null)
const paymentMethod = ref('CASH')
const promotions = ref([])
const qrCodeUrl = ref('')

// Watch for show prop changes to load order
watch(() => props.show, (newVal) => {
  if (newVal) {
    loadOrder()
  } else {
      // Reset when closing
      selectedOrder.value = null
      showPaymentDialog.value = false
      showPaymentStep2.value = false
      showPromotionDialog.value = false
      selectedPromotion.value = null
      paymentMethod.value = 'CASH'
      qrCodeUrl.value = ''
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

    if (!reservationId) {
      selectedOrder.value = null
      notification.info('Bàn này chưa có đặt bàn')
      return
    }

    // Load all orders to find order by reservationId
    const ordersRes = await orderService.getAll()
    const allOrders = ordersRes.success ? ordersRes.data : ordersRes.data || []

    // Tìm tất cả orders có cùng reservationId
    const matchingOrders = allOrders.filter(order => {
      const orderReservationId = order.reservationId
      return orderReservationId === reservationId || 
             orderReservationId === Number(reservationId) || 
             Number(orderReservationId) === reservationId ||
             String(orderReservationId) === String(reservationId)
    })

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
      notification.info('Chưa có đơn hàng cho đặt bàn này')
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

// Lấy notes hiển thị (loại bỏ SERVED_KEYWORD)
function getDisplayNotes(item) {
  if (!item || !item.notes) return null
  const regex = new RegExp(SERVED_KEYWORD, 'gi')
  return item.notes.replace(regex, '').trim() || null
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
async function updateItemServedStatus(itemId, served) {
  if (!selectedOrder.value || !selectedOrder.value.items) return
  
  try {
    const item = selectedOrder.value.items.find(i => i.id === itemId)
    if (!item) return
    
    // Lấy notes hiện tại (loại bỏ SERVED_KEYWORD nếu có)
    let currentNotes = item.notes || ''
    const regex = new RegExp(SERVED_KEYWORD, 'gi')
    currentNotes = currentNotes.replace(regex, '').trim()
    
    // Thêm SERVED_KEYWORD vào notes
    let newNotes = currentNotes
    if (served) {
      newNotes = currentNotes ? `${currentNotes} ${SERVED_KEYWORD}` : SERVED_KEYWORD
    }
    
    // Cập nhật order_detail trực tiếp theo id
    const updateItemData = {
      dishId: item.dishId || item.dish?.id,
      quantity: item.quantity,
      notes: newNotes
    }
    
    await orderService.updateOrderDetail(selectedOrder.value.id, itemId, updateItemData)
    
    // Reload order để lấy dữ liệu mới nhất
    await loadOrder()
    
    // Tự động kiểm tra và cập nhật status order
    await checkAndUpdateOrderStatus()
    
    emit('order-updated')
  } catch (error) {
    console.error('Error updating item served status:', error)
    notification.error('Không thể cập nhật trạng thái món: ' + (error.response?.data?.message || error.message))
  }
}

// Kiểm tra và cập nhật status order
async function checkAndUpdateOrderStatus() {
  if (!selectedOrder.value || !selectedOrder.value.items || selectedOrder.value.items.length === 0) return
  
  // Kiểm tra tất cả món đã served chưa
  const allServed = selectedOrder.value.items.every(item => isItemServed(item))
  
  // Nếu tất cả đã served và status chưa phải SERVED, cập nhật
  if (allServed && selectedOrder.value.status !== 'SERVED') {
    try {
      await orderService.updateStatus(selectedOrder.value.id, 'SERVED')
      await loadOrder()
      emit('order-updated')
    } catch (error) {
      console.error('Error updating order status:', error)
    }
  }
  // Nếu còn món chưa served và status là SERVED, chuyển về PENDING
  else if (!allServed && selectedOrder.value.status === 'SERVED') {
    try {
      await orderService.updateStatus(selectedOrder.value.id, 'PENDING')
      await loadOrder()
      emit('order-updated')
    } catch (error) {
      console.error('Error updating order status:', error)
    }
  }
}

async function markAllAsServed() {
  if (!selectedOrder.value) return

  if (!confirm('Xác nhận đã lên đủ số lượng món cho đơn hàng này?')) return

  try {
    // Đánh dấu tất cả items là served - cập nhật từng item theo id
    const updatePromises = selectedOrder.value.items.map(async (item) => {
      if (isItemServed(item)) return // Đã served rồi thì bỏ qua
      
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
    notification.success('Đã cập nhật: Đã lên món tất cả')
    await loadOrder()
    await checkAndUpdateOrderStatus()
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
    await checkAndUpdateOrderStatus()
    emit('order-updated')
  } catch (error) {
    console.error('Error marking items as served:', error)
    notification.error('Không thể đánh dấu món: ' + (error.response?.data?.message || error.message))
  }
}

async function proceedToPayment() {
  if (!selectedOrder.value) return
  
  // Load promotions
  await loadPromotions()
  showPaymentDialog.value = true
  showPaymentStep2.value = false
  selectedPromotion.value = null
  paymentMethod.value = 'CASH'
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

function closePaymentDialog() {
  showPaymentDialog.value = false
  showPaymentStep2.value = false
  selectedPromotion.value = null
  paymentMethod.value = 'CASH'
  qrCodeUrl.value = ''
  loadingPayment.value = false
}

function goToPaymentStep2() {
  if (!selectedOrder.value) return
  showPaymentStep2.value = true
}

function backToPaymentStep1() {
  showPaymentStep2.value = false
}

// Tính toán tổng tiền sau khi áp dụng khuyến mãi
const finalTotal = computed(() => {
  if (!selectedOrder.value) return 0
  
  let subtotal = selectedOrder.value.subtotal || 0
  let tax = selectedOrder.value.tax || 0
  let discount = selectedOrder.value.discount || 0
  
  // Tính discount từ promotion
  if (selectedPromotion.value) {
    const promotion = selectedPromotion.value
    // Kiểm tra minOrderValue
    if (!promotion.minOrderValue || subtotal >= promotion.minOrderValue) {
      let promotionDiscount = 0
      
      if (promotion.discountPercent) {
        promotionDiscount = (subtotal * promotion.discountPercent) / 100
        const maxDiscount = promotion.maxDiscount || promotionDiscount
        promotionDiscount = Math.min(promotionDiscount, maxDiscount)
      } else if (promotion.discountAmount) {
        promotionDiscount = promotion.discountAmount
      }
      
      discount = discount + promotionDiscount
    }
  }
  
  // Tính tổng: subtotal - discount + tax
  const total = subtotal - discount + tax
  return Math.max(0, total)
})

// Watch paymentMethod để tự động generate QR code
watch(() => paymentMethod.value, (newVal) => {
  if (newVal === 'BANK_TRANSFER') {
    generateQRCode()
  } else {
    qrCodeUrl.value = ''
  }
})

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

// Tạo QR code cho chuyển khoản
async function generateQRCode() {
  if (!selectedOrder.value || paymentMethod.value !== 'BANK_TRANSFER') return
  
  try {
    // Tạo QR code với thông tin: số tài khoản, số tiền, ghi chú
    const accountNumber = '1234567890' // Số tài khoản ngân hàng - cần lấy từ config
    const accountName = 'NHÀ HÀNG LẨU HẢI SẢN'
    const bankCode = 'VCB' // Vietcombank
    const amount = finalTotal.value
    const note = `Thanh toan don hang ${selectedOrder.value.orderNumber}`
    
    // Tạo QR code URL sử dụng VietQR API
    // Format: https://img.vietqr.io/image/{bankCode}-{accountNumber}-compact2.png?amount={amount}&addInfo={note}
    qrCodeUrl.value = `https://img.vietqr.io/image/${bankCode}-${accountNumber}-compact2.png?amount=${amount}&addInfo=${encodeURIComponent(note)}`
  } catch (error) {
    console.error('Error generating QR code:', error)
    notification.error('Không thể tạo QR code')
  }
}

// Xử lý thanh toán
async function handlePayment() {
  if (!selectedOrder.value || loadingPayment.value) return
  
  loadingPayment.value = true
  
  try {
    // Xóa SERVED_KEYWORD khỏi notes của tất cả order_details
    if (selectedOrder.value.items && selectedOrder.value.items.length > 0) {
      for (const item of selectedOrder.value.items) {
        if (item.notes && item.notes.includes(SERVED_KEYWORD)) {
          // Xóa SERVED_KEYWORD khỏi notes
          const regex = new RegExp(SERVED_KEYWORD, 'gi')
          const cleanedNotes = item.notes.replace(regex, '').trim()
          // Update order_detail để xóa "served" - gửi đầy đủ thông tin
          const updateItemData = {
            dishId: item.dishId || item.dish?.id,
            quantity: item.quantity,
            notes: cleanedNotes || null
          }
          await orderService.updateOrderDetail(selectedOrder.value.id, item.id, updateItemData)
        }
      }
    }
    
    // Tạo payment
    const paymentData = {
      orderId: selectedOrder.value.id,
      amount: finalTotal.value,
      paymentMethod: paymentMethod.value,
      paymentStatus: 'COMPLETED',
      notes: selectedPromotion.value ? `Áp dụng khuyến mãi: ${selectedPromotion.value.name || selectedPromotion.value.promotionName}` : null
    }
    
    // Gọi API tạo payment
    try {
      await paymentService.create(paymentData)
    } catch (error) {
      console.warn('Payment API not available, skipping payment creation:', error)
      // Nếu API chưa có, chỉ cập nhật order status
    }
    
    // Cập nhật discount và total nếu có promotion
    if (selectedPromotion.value) {
      const promotionDiscount = getPromotionDiscount()
      const newDiscount = (selectedOrder.value.discount || 0) + promotionDiscount
      const newTotal = finalTotal.value
      
      // Cập nhật discount và total mà không cần gửi items
      const updateData = {
        notes: selectedOrder.value.notes || '',
        discount: newDiscount,
        total: newTotal
      }
      await orderService.update(selectedOrder.value.id, updateData)
    }
    
    // Cập nhật order status thành COMPLETED
    await orderService.updateStatus(selectedOrder.value.id, 'COMPLETED')
    
    notification.success('Thanh toán thành công')
    await loadOrder()
    emit('order-updated')
    
    // Đóng dialog
    setTimeout(() => {
      closePaymentDialog()
      handleClose()
    }, 1500)
  } catch (error) {
    console.error('Error processing payment:', error)
    notification.error('Không thể xử lý thanh toán: ' + (error.response?.data?.message || error.message))
  } finally {
    loadingPayment.value = false
  }
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
  
  // Kiểm tra nếu đây là món duy nhất trong đơn hàng
  if (selectedOrder.value.items && selectedOrder.value.items.length === 1) {
    notification.error('Không thể hủy món duy nhất trong đơn hàng')
    return
  }
  
  if (!confirm(`Bạn có chắc muốn hủy món "${item.dishName} khỏi đơn hàng"?`)) {
    return
  }
  
  try {
    // Gọi API xóa order_detail
    await orderService.deleteOrderDetail(selectedOrder.value.id, itemId)
    notification.success('Đã hủy món')
    await loadOrder()
    await checkAndUpdateOrderStatus()
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

