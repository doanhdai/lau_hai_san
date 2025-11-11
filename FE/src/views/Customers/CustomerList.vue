<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-slate-900">Quản lý khách hàng</h1>
        <p class="text-slate-600 mt-1 text-sm">Danh sách và thông tin khách hàng</p>
      </div>
      <button @click="showCreateModal = true" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 transition-colors">
        <i class="fas fa-plus"></i>
        <span>Thêm khách hàng</span>
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white border border-gray-200 rounded-lg p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Tìm kiếm</label>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tên, SĐT, Email..."
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Loại khách hàng</label>
          <select v-model="filterType" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
            <option value="">Tất cả</option>
            <option value="vip">VIP</option>
            <option value="normal">Thường</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">Trạng thái</label>
          <select v-model="filterStatus" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition">
            <option value="">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="blocked">Bị chặn</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="loadCustomers" class="bg-gray-100 hover:bg-gray-200 text-slate-700 w-full px-4 py-2 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors">
            <i class="fas fa-search"></i>
            <span>Tìm kiếm</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center h-64">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Customers Table -->
    <div v-else class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-slate-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">ID</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Họ tên</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Số điện thoại</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Email</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Loại</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Trạng thái</th>
              <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Thao tác</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="customer in filteredCustomers" :key="customer.id" class="hover:bg-slate-50 transition">
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">{{ customer.id }}</td>
              <td class="px-4 py-3 whitespace-nowrap">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 bg-slate-900 rounded-full flex items-center justify-center text-white font-semibold text-sm">
                    {{ customer.fullName.charAt(0) }}
                  </div>
                  <div>
                    <p class="text-sm font-medium text-slate-900">{{ customer.fullName }}</p>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-900">{{ customer.phone }}</td>
              <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-600">{{ customer.email || '-' }}</td>
              <td class="px-4 py-3 whitespace-nowrap">
                <span v-if="customer.isVip" class="px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 flex items-center gap-1 w-fit">
                  <i class="fas fa-star text-xs"></i>
                  <span>VIP</span>
                </span>
                <span v-else class="px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800">Thường</span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap">
                <span v-if="customer.blocked" class="px-2.5 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800">Bị chặn</span>
                <span v-else-if="customer.active" class="px-2.5 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800">Hoạt động</span>
                <span v-else class="px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800">Ngưng hoạt động</span>
              </td>
              <td class="px-4 py-3 whitespace-nowrap text-sm">
                <div class="flex items-center gap-2">
                  <button @click="editCustomer(customer)" class="p-2 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors" title="Chỉnh sửa">
                    <i class="fas fa-edit text-sm"></i>
                  </button>
                  <button 
                    v-if="!customer.blocked" 
                    @click="blockCustomer(customer)" 
                    class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Chặn khách hàng"
                  >
                    <i class="fas fa-ban text-sm"></i>
                  </button>
                  <button 
                    v-else 
                    @click="unblockCustomer(customer)" 
                    class="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                    title="Bỏ chặn khách hàng"
                  >
                    <i class="fas fa-check-circle text-sm"></i>
                  </button>
                  <button @click="deleteCustomer(customer)" class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Xóa">
                    <i class="fas fa-trash text-sm"></i>
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
