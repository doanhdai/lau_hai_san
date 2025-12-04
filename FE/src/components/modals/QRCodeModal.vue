<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[10000] p-4"
      @click.self="$emit('close')"
    >
      <div class="bg-white rounded-lg shadow-2xl w-full max-w-md mx-auto p-6" @click.stop>
        <!-- Modal Header -->
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold text-slate-900">QR Code đặt món</h3>
          <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600">
            <i class="fas fa-times text-xl"></i>
          </button>
        </div>

        <!-- QR Code -->
        <div class="flex flex-col items-center space-y-4">
          <div class="bg-white p-4 rounded-lg border-2 border-gray-200">
            <qrcode-vue
              :value="qrValue"
              :size="250"
              level="H"
            />
          </div>
          
          <div class="text-center">
            <p class="text-sm text-gray-600 mb-2">Quét mã QR để đặt món cho bàn này</p>
            <p class="text-xs text-gray-500">Bàn: {{ tableNumber }}</p>
          </div>

          <!-- Copy Link Button -->
          <button
            @click="copyLink"
            class="w-full bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
          >
            <i class="fas fa-copy"></i>
            <span>Sao chép liên kết</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import QrcodeVue from 'qrcode.vue'
import { useNotificationStore } from '@/stores/notification'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  tableId: {
    type: Number,
    required: true
  },
  tableNumber: {
    type: String,
    required: true
  },
  reservationId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['close'])
const notification = useNotificationStore()

const qrValue = computed(() => {
  const baseUrl = window.location.origin
  const params = new URLSearchParams()
  params.append('tableId', props.tableId)
  // Chỉ thêm reservationId nếu thực sự có (không phải null, undefined, hoặc 0)
  if (props.reservationId && props.reservationId !== null && props.reservationId !== undefined && props.reservationId !== 0) {
    params.append('reservationId', props.reservationId)
  }
  return `${baseUrl}/customer/order?${params.toString()}`
})

async function copyLink() {
  try {
    await navigator.clipboard.writeText(qrValue.value)
    notification.success('Đã sao chép liên kết')
  } catch (error) {
    notification.error('Không thể sao chép liên kết')
  }
}
</script>

