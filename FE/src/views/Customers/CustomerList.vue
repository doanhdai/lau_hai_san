<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý khách hàng</h1>
        <p class="text-gray-600 mt-1">Danh sách và thông tin khách hàng</p>
      </div>
      <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
        <PlusIcon class="w-5 h-5" />
        Thêm khách hàng
      </button>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên, SĐT, Email..."
            class="input-field"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Loại khách hàng</label>
          <select v-model="filterType" class="input-field">
            <option value="">Tất cả</option>
            <option value="vip">VIP</option>
            <option value="normal">Thường</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="input-field">
            <option value="">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="blocked">Bị chặn</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="loadCustomers" class="btn-secondary w-full flex items-center justify-center gap-2">
            <MagnifyingGlassIcon class="w-5 h-5" />
            Tìm kiếm
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="loading-spinner"></div>
    </div>

    <!-- Customers Table -->
    <div v-else class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Họ tên</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Số điện thoại</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Loại</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="customer in filteredCustomers" :key="customer.id" class="hover:bg-gray-50 transition">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ customer.id }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 bg-gradient-to-br from-red-500 to-red-600 rounded-full flex items-center justify-center text-white font-bold">
                    {{ customer.fullName.charAt(0) }}
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-900">{{ customer.fullName }}</p>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ customer.phone }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ customer.email || '-' }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span v-if="customer.isVip" class="badge bg-yellow-100 text-yellow-800">
                  <StarIcon class="w-4 h-4 inline mr-1" />
                  VIP
                </span>
                <span v-else class="badge bg-gray-100 text-gray-800">Thường</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span v-if="customer.blocked" class="badge-danger">Bị chặn</span>
                <span v-else-if="customer.active" class="badge-success">Hoạt động</span>
                <span v-else class="badge bg-gray-100 text-gray-800">Ngưng hoạt động</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                  <button @click="editCustomer(customer)" class="text-blue-600 hover:text-blue-900 transition">
                    <PencilIcon class="w-5 h-5" />
                  </button>
                  <button 
                    v-if="!customer.blocked" 
                    @click="blockCustomer(customer)" 
                    class="text-red-600 hover:text-red-900 transition"
                    title="Chặn khách hàng"
                  >
                    <NoSymbolIcon class="w-5 h-5" />
                  </button>
                  <button 
                    v-else 
                    @click="unblockCustomer(customer)" 
                    class="text-green-600 hover:text-green-900 transition"
                    title="Bỏ chặn khách hàng"
                  >
                    <CheckCircleIcon class="w-5 h-5" />
                  </button>
                  <button @click="deleteCustomer(customer)" class="text-red-600 hover:text-red-900 transition">
                    <TrashIcon class="w-5 h-5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <CustomerModal
      v-if="showCreateModal || selectedCustomer"
      :customer="selectedCustomer"
      @close="closeModal"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { customerService } from '@/services/customerService'
import { useNotificationStore } from '@/stores/notification'
import {
  PlusIcon,
  MagnifyingGlassIcon,
  PencilIcon,
  TrashIcon,
  StarIcon,
  NoSymbolIcon,
  CheckCircleIcon
} from '@heroicons/vue/24/outline'
import CustomerModal from '@/components/modals/CustomerModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const customers = ref([])
const searchQuery = ref('')
const filterType = ref('')
const filterStatus = ref('')
const showCreateModal = ref(false)
const selectedCustomer = ref(null)

const filteredCustomers = computed(() => {
  let result = customers.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(c => 
      c.fullName.toLowerCase().includes(query) ||
      c.phone.includes(query) ||
      (c.email && c.email.toLowerCase().includes(query))
    )
  }

  if (filterType.value === 'vip') {
    result = result.filter(c => c.isVip)
  } else if (filterType.value === 'normal') {
    result = result.filter(c => !c.isVip)
  }

  if (filterStatus.value === 'active') {
    result = result.filter(c => c.active && !c.blocked)
  } else if (filterStatus.value === 'blocked') {
    result = result.filter(c => c.blocked)
  }

  return result
})

onMounted(() => {
  loadCustomers()
})

async function loadCustomers() {
  loading.value = true
  try {
    const response = await customerService.getAll()
    if (response.success) {
      customers.value = response.data
    }
  } catch (error) {
    notification.error('Không thể tải danh sách khách hàng')
  } finally {
    loading.value = false
  }
}

function editCustomer(customer) {
  selectedCustomer.value = { ...customer }
}

async function blockCustomer(customer) {
  if (!confirm(`Chặn khách hàng "${customer.fullName}"?`)) return

  try {
    await customerService.block(customer.id)
    notification.success('Đã chặn khách hàng')
    loadCustomers()
  } catch (error) {
    notification.error('Không thể chặn khách hàng')
  }
}

async function unblockCustomer(customer) {
  try {
    await customerService.unblock(customer.id)
    notification.success('Đã bỏ chặn khách hàng')
    loadCustomers()
  } catch (error) {
    notification.error('Không thể bỏ chặn khách hàng')
  }
}

async function deleteCustomer(customer) {
  if (!confirm(`Xóa khách hàng "${customer.fullName}"?`)) return

  try {
    await customerService.delete(customer.id)
    notification.success('Đã xóa khách hàng')
    loadCustomers()
  } catch (error) {
    notification.error('Không thể xóa khách hàng')
  }
}

function closeModal() {
  showCreateModal.value = false
  selectedCustomer.value = null
}

async function handleSave(customerData) {
  try {
    if (selectedCustomer.value) {
      await customerService.update(selectedCustomer.value.id, customerData)
      notification.success('Cập nhật khách hàng thành công')
    } else {
      await customerService.create(customerData)
      notification.success('Thêm khách hàng thành công')
    }
    closeModal()
    loadCustomers()
  } catch (error) {
    notification.error('Không thể lưu thông tin khách hàng')
  }
}
</script>
