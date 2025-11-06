<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Quản lý người dùng</h1>
        <p class="text-gray-600 mt-1">Quản lý tài khoản người dùng và phân quyền</p>
      </div>
      <button @click="openCreateModal" class="btn-primary flex items-center gap-2">
        <span class="text-lg">+</span>
        Thêm người dùng
      </button>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Tìm kiếm</label>
          <input
            v-model="filters.search"
            type="text"
            placeholder="Tên, email, số điện thoại..."
            class="input-field"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Vai trò</label>
          <select v-model="filters.role" class="input-field">
            <option value="">Tất cả vai trò</option>
            <option value="ROLE_ADMIN">Quản trị viên</option>
            <option value="ROLE_MANAGER">Quản lý</option>
            <option value="ROLE_STAFF">Nhân viên</option>
            <option value="ROLE_CUSTOMER">Khách hàng</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
          <select v-model="filters.status" class="input-field">
            <option value="">Tất cả trạng thái</option>
            <option value="true">Hoạt động</option>
            <option value="false">Tạm khóa</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="loadUsers" class="btn-secondary w-full">
            <span class="mr-2">🔍</span>
            Tìm kiếm
          </button>
        </div>
      </div>
    </div>

    <!-- Users Table -->
    <div class="card">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Người dùng
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Liên hệ
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Vai trò
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Trạng thái
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ngày tạo
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-if="loading" v-for="n in 5" :key="n" class="animate-pulse">
              <td class="px-6 py-4">
                <div class="flex items-center">
                  <div class="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div class="ml-4">
                    <div class="h-4 bg-gray-200 rounded w-24 mb-2"></div>
                    <div class="h-3 bg-gray-200 rounded w-32"></div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="h-4 bg-gray-200 rounded w-32 mb-2"></div>
                <div class="h-3 bg-gray-200 rounded w-28"></div>
              </td>
              <td class="px-6 py-4">
                <div class="h-6 bg-gray-200 rounded w-20"></div>
              </td>
              <td class="px-6 py-4">
                <div class="h-6 bg-gray-200 rounded w-16"></div>
              </td>
              <td class="px-6 py-4">
                <div class="h-4 bg-gray-200 rounded w-20"></div>
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex justify-end gap-2">
                  <div class="h-8 w-8 bg-gray-200 rounded"></div>
                  <div class="h-8 w-8 bg-gray-200 rounded"></div>
                </div>
              </td>
            </tr>
            
            <tr v-else-if="users.length === 0">
              <td colspan="6" class="px-6 py-12 text-center text-gray-500">
                <div class="text-6xl mb-4">👤</div>
                <p class="text-lg font-medium">Không tìm thấy người dùng</p>
                <p class="text-sm">Thử thay đổi bộ lọc hoặc thêm người dùng mới</p>
              </td>
            </tr>

            <tr v-else v-for="user in users" :key="user.id" class="hover:bg-gray-50">
              <td class="px-6 py-4">
                <div class="flex items-center">
                  <div class="w-10 h-10 rounded-full bg-primary-500 flex items-center justify-center text-white font-bold">
                    {{ getUserInitial(user.fullName) }}
                  </div>
                  <div class="ml-4">
                    <div class="text-sm font-medium text-gray-900">{{ user.fullName }}</div>
                    <div class="text-sm text-gray-500">{{ user.username }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="text-sm text-gray-900">{{ user.email }}</div>
                <div class="text-sm text-gray-500">{{ user.phoneNumber || 'Chưa có' }}</div>
              </td>
              <td class="px-6 py-4">
                <span class="badge" :class="getRoleBadgeClass(user.roles)">
                  {{ getRoleDisplay(user.roles) }}
                </span>
              </td>
              <td class="px-6 py-4">
                <span class="badge" :class="user.active ? 'badge-success' : 'badge-danger'">
                  {{ user.active ? 'Hoạt động' : 'Tạm khóa' }}
                </span>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">
                {{ formatDate(user.createdAt) }}
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex justify-end gap-2">
                  <button
                    @click="openEditModal(user)"
                    class="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                    title="Chỉnh sửa"
                  >
                    <span class="text-lg">✏️</span>
                  </button>
                  <button
                    @click="toggleUserStatus(user)"
                    :class="user.active ? 'text-red-600 hover:bg-red-50' : 'text-green-600 hover:bg-green-50'"
                    class="p-2 rounded-lg transition"
                    :title="user.active ? 'Khóa tài khoản' : 'Kích hoạt tài khoản'"
                  >
                    <span v-if="user.active" class="text-lg">🔒</span>
                    <span v-else class="text-lg">🔓</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="pagination.totalPages > 1" class="px-6 py-4 border-t border-gray-200">
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-700">
            Hiển thị {{ (pagination.currentPage - 1) * pagination.pageSize + 1 }} - 
            {{ Math.min(pagination.currentPage * pagination.pageSize, pagination.totalElements) }} 
            trong {{ pagination.totalElements }} kết quả
          </div>
          <div class="flex gap-2">
            <button
              @click="changePage(pagination.currentPage - 1)"
              :disabled="pagination.currentPage === 1"
              class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Trước
            </button>
            <button
              v-for="page in getVisiblePages()"
              :key="page"
              @click="changePage(page)"
              :class="page === pagination.currentPage ? 'bg-primary-600 text-white' : 'bg-white text-gray-500 hover:bg-gray-50'"
              class="px-3 py-2 text-sm font-medium border border-gray-300 rounded-md"
            >
              {{ page }}
            </button>
            <button
              @click="changePage(pagination.currentPage + 1)"
              :disabled="pagination.currentPage === pagination.totalPages"
              class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Sau
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- User Modal -->
    <UserModal
      v-if="showModal"
      :user="selectedUser"
      @close="closeModal"
      @saved="handleUserSaved"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { userService } from '@/services/userService'
import { useNotificationStore } from '@/stores/notification'
import UserModal from '@/components/modals/UserModal.vue'

const notification = useNotificationStore()

const loading = ref(false)
const users = ref([])
const showModal = ref(false)
const selectedUser = ref(null)

const filters = reactive({
  search: '',
  role: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  totalElements: 0,
  totalPages: 0
})

// Watch filters for auto-search
watch(filters, () => {
  pagination.currentPage = 1
  loadUsers()
}, { deep: true })

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      search: filters.search || undefined,
      role: filters.role || undefined,
      active: filters.status !== '' ? filters.status === 'true' : undefined
    }

    const response = await userService.getAll(params)
    
    if (response.success) {
      users.value = response.data.content || response.data
      
      // Update pagination if response has pagination info
      if (response.data.totalElements !== undefined) {
        pagination.totalElements = response.data.totalElements
        pagination.totalPages = response.data.totalPages
        pagination.currentPage = response.data.number + 1
      }
    }
  } catch (error) {
    console.error('Error loading users:', error)
    notification.error('Không thể tải danh sách người dùng')
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  selectedUser.value = null
  showModal.value = true
}

function openEditModal(user) {
  selectedUser.value = { ...user }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  selectedUser.value = null
}

function handleUserSaved() {
  closeModal()
  loadUsers()
}

async function toggleUserStatus(user) {
  const action = user.active ? 'khóa' : 'kích hoạt'
  if (!confirm(`Bạn có chắc muốn ${action} tài khoản "${user.fullName}"?`)) {
    return
  }

  try {
    const response = await userService.toggleStatus(user.id)
    if (response.success) {
      notification.success(`${action === 'khóa' ? 'Khóa' : 'Kích hoạt'} tài khoản thành công`)
      loadUsers()
    }
  } catch (error) {
    console.error('Error toggling user status:', error)
    notification.error(`Không thể ${action} tài khoản`)
  }
}

function changePage(page) {
  if (page >= 1 && page <= pagination.totalPages) {
    pagination.currentPage = page
    loadUsers()
  }
}

function getVisiblePages() {
  const current = pagination.currentPage
  const total = pagination.totalPages
  const pages = []
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) pages.push(i)
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) pages.push(i)
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) pages.push(i)
      pages.push('...')
      pages.push(total)
    }
  }
  
  return pages.filter(p => p !== '...' || pages.indexOf(p) === pages.lastIndexOf(p))
}

function getUserInitial(fullName) {
  return fullName?.charAt(0).toUpperCase() || 'U'
}

function getRoleDisplay(roles) {
  if (!roles || roles.length === 0) return 'Không xác định'
  
  const roleMap = {
    'ROLE_ADMIN': 'Quản trị viên',
    'ROLE_MANAGER': 'Quản lý',
    'ROLE_STAFF': 'Nhân viên',
    'ROLE_CUSTOMER': 'Khách hàng'
  }
  
  const mainRole = roles.find(role => role !== 'ROLE_CUSTOMER') || roles[0]
  return roleMap[mainRole] || mainRole
}

function getRoleBadgeClass(roles) {
  if (!roles || roles.length === 0) return 'badge-info'
  
  const mainRole = roles.find(role => role !== 'ROLE_CUSTOMER') || roles[0]
  
  switch (mainRole) {
    case 'ROLE_ADMIN': return 'bg-red-100 text-red-800'
    case 'ROLE_MANAGER': return 'bg-purple-100 text-purple-800'
    case 'ROLE_STAFF': return 'bg-blue-100 text-blue-800'
    case 'ROLE_CUSTOMER': return 'bg-green-100 text-green-800'
    default: return 'badge-info'
  }
}

function formatDate(dateString) {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('vi-VN')
}
</script>
