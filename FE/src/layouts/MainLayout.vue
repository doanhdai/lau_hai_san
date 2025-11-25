<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside 
      class="fixed inset-y-0 left-0 z-50 w-64 bg-slate-900 text-white transform transition-transform duration-300 ease-in-out lg:translate-x-0 flex flex-col border-r border-slate-800"
      :class="{ '-translate-x-full': !sidebarOpen }"
    >
      <!-- Logo -->
      <div class="flex items-center justify-center h-16 border-b border-slate-800 flex-shrink-0">
        <h1 class="text-xl font-bold flex items-center gap-2">
          <i class="fas fa-bowl-food text-2xl"></i>
          <span>MNDHD</span>
        </h1>
      </div>

      <!-- Navigation - Scrollable -->
      <nav class="flex-1 overflow-y-auto px-3 py-4 space-y-1">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200 text-sm"
          :class="isActive(item.path) 
            ? 'bg-white text-slate-900 font-semibold' 
            : 'text-slate-300 hover:bg-slate-800 hover:text-white'"
        >
          <i :class="['fas', item.icon, 'text-base']"></i>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- User Info -->
      <div class="flex-shrink-0 p-4 border-t border-slate-800">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center border border-slate-700">
            <span class="text-sm font-semibold">{{ userInitial }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-medium text-sm truncate">{{ authStore.user?.fullName }}</p>
            <p class="text-xs text-slate-400 truncate">{{ userRole }}</p>
          </div>
          <button @click="handleLogout" class="p-2 hover:bg-slate-800 rounded-lg transition-colors">
            <i class="fas fa-sign-out-alt text-base"></i>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col lg:ml-64">
      <!-- Header -->
      <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6">
        <button
          @click="toggleSidebar"
          class="lg:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <i class="fas fa-bars text-slate-700"></i>
        </button>

        <div class="flex-1"></div>

        <div class="flex items-center gap-4">
          <!-- Notifications -->
          <div class="relative">
            <button
              @click="toggleNotificationDropdown"
              class="relative p-2 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <i class="fas fa-bell text-slate-600"></i>
              <span
                v-if="reservationNotificationStore.unreadCount > 0"
                class="absolute top-1 right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center font-semibold"
              >
                {{ reservationNotificationStore.unreadCount > 9 ? '9+' : reservationNotificationStore.unreadCount }}
              </span>
            </button>

            <!-- Notification Dropdown -->
            <div
              v-if="showNotificationDropdown"
              ref="notificationDropdown"
              class="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-xl border border-gray-200 z-50 max-h-96 overflow-hidden flex flex-col"
            >
              <div class="p-4 border-b border-gray-200 flex items-center justify-between">
                <h3 class="font-semibold text-slate-900">Thông báo đặt bàn</h3>
                <div class="flex items-center gap-2">
                  <button
                    v-if="reservationNotificationStore.unreadCount > 0"
                    @click="markAllAsRead"
                    class="text-xs text-blue-600 hover:text-blue-800"
                  >
                    Đánh dấu đã đọc
                  </button>
                  <button @click="closeNotificationDropdown" class="text-gray-400 hover:text-gray-600">
                    <i class="fas fa-times text-sm"></i>
                  </button>
                </div>
              </div>

              <div class="overflow-y-auto flex-1">
                <div v-if="reservationNotificationStore.notifications.length === 0" class="p-8 text-center text-gray-500">
                  <i class="fas fa-bell-slash text-3xl mb-2 opacity-50"></i>
                  <p class="text-sm">Không có thông báo</p>
                </div>

                <div v-else class="divide-y divide-gray-100">
                  <button
                    v-for="notification in reservationNotificationStore.notifications"
                    :key="notification.id"
                    @click="handleNotificationClick(notification)"
                    class="w-full p-4 text-left hover:bg-gray-50 transition-colors"
                    :class="{ 'bg-blue-50': !notification.read }"
                  >
                    <div class="flex items-start gap-3">
                      <div
                        class="w-2 h-2 rounded-full mt-2 flex-shrink-0"
                        :class="{
                          'bg-blue-500': notification.type === 'info',
                          'bg-yellow-500': notification.type === 'warning',
                          'bg-green-500': notification.type === 'success',
                          'bg-red-500': notification.type === 'error'
                        }"
                      ></div>
                      <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-slate-900" :class="{ 'font-semibold': !notification.read }">
                          {{ notification.message }}
                        </p>
                        <p class="text-xs text-gray-500 mt-1">
                          {{ formatNotificationTime(notification.createdAt) }}
                        </p>
                      </div>
                      <button
                        @click.stop="removeNotification(notification.id)"
                        class="text-gray-400 hover:text-red-500 transition-colors flex-shrink-0"
                      >
                        <i class="fas fa-times text-xs"></i>
                      </button>
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <main 
        class="flex-1"
        :class="route.path === '/admin/chat' ? 'overflow-hidden p-0' : 'overflow-y-auto p-6'"
      >
        <transition
          name="fade"
          mode="out-in"
        >
          <router-view />
        </transition>
      </main>
    </div>

    <!-- Overlay for mobile -->
    <div
      v-if="sidebarOpen"
      @click="closeSidebar"
      class="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
    ></div>

    <!-- Notifications -->
    <NotificationList />
    
    <!-- Chat Widget - Removed because chat is available in sidebar -->
    <!-- <ChatWidget /> -->
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useReservationNotificationStore } from '@/stores/reservationNotification'

import NotificationList from '@/components/NotificationList.vue'
// ChatWidget removed - chat is available in sidebar navigation
// import ChatWidget from '@/components/ChatWidget.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const reservationNotificationStore = useReservationNotificationStore()
const sidebarOpen = ref(false)
const showNotificationDropdown = ref(false)
const notificationDropdown = ref(null)

// Helper function to get primary role (highest priority)
function getPrimaryRole(roles) {
  if (!roles || roles.length === 0) return null
  
  // Priority order: ADMIN > MANAGER > STAFF > CUSTOMER
  const rolePriority = {
    'ROLE_ADMIN': 1,
    'ROLE_MANAGER': 2,
    'ROLE_STAFF': 3,
    'ROLE_CUSTOMER': 4
  }
  
  // Filter out CUSTOMER role and find the highest priority role
  const staffRoles = roles.filter(role => role !== 'ROLE_CUSTOMER')
  if (staffRoles.length === 0) return roles[0] // Fallback to first role if only CUSTOMER
  
  // Sort by priority and return the highest (lowest number = highest priority)
  return staffRoles.sort((a, b) => {
    const priorityA = rolePriority[a] || 999
    const priorityB = rolePriority[b] || 999
    return priorityA - priorityB
  })[0]
}

const menuItems = computed(() => {
  const userRoles = authStore.user?.roles || []
  const primaryRole = getPrimaryRole(userRoles)
  
  // Admin menu - only if primary role is ADMIN
  if (primaryRole === 'ROLE_ADMIN') {
    return [
      { path: '/admin/dashboard', label: 'Tổng quan', icon: 'fa-home' },
      { path: '/admin/users', label: 'Người dùng', icon: 'fa-users' },
      { path: '/admin/customers', label: 'Khách hàng', icon: 'fa-user' },
      { path: '/admin/tables', label: 'Quản lý bàn', icon: 'fa-chair' },
      { path: '/admin/dishes', label: 'Thực đơn', icon: 'fa-utensils' },
      { path: '/admin/categories', label: 'Danh mục', icon: 'fa-tags' },
      { path: '/admin/orders', label: 'Đơn hàng', icon: 'fa-shopping-bag' },
      { path: '/admin/reservations', label: 'Đặt bàn', icon: 'fa-calendar' },
      { path: '/admin/reservations/checkin', label: 'Check-in bàn', icon: 'fa-check-circle' },
      { path: '/admin/promotions', label: 'Khuyến mãi', icon: 'fa-gift' },
      { path: '/admin/feedbacks', label: 'Phản hồi', icon: 'fa-comments' },
      { path: '/admin/reports', label: 'Báo cáo', icon: 'fa-chart-bar' },
    ]
  }
  
  // Manager menu - only if primary role is MANAGER (and not ADMIN)
  if (primaryRole === 'ROLE_MANAGER') {
    return [
      { path: '/manager/dashboard', label: 'Tổng quan', icon: 'fa-home' },
      { path: '/admin/customers', label: 'Khách hàng', icon: 'fa-user' },
      { path: '/admin/tables', label: 'Quản lý bàn', icon: 'fa-chair' },
      { path: '/admin/dishes', label: 'Thực đơn', icon: 'fa-utensils' },
      { path: '/admin/categories', label: 'Danh mục', icon: 'fa-tags' },
      { path: '/admin/orders', label: 'Đơn hàng', icon: 'fa-shopping-bag' },
      { path: '/admin/reservations', label: 'Đặt bàn', icon: 'fa-calendar' },
      { path: '/admin/reservations/checkin', label: 'Check-in bàn', icon: 'fa-check-circle' },
      { path: '/admin/chat', label: 'Chat', icon: 'fa-comments' },
      { path: '/admin/promotions', label: 'Khuyến mãi', icon: 'fa-gift' },
      { path: '/admin/feedbacks', label: 'Phản hồi', icon: 'fa-comments' },
      { path: '/admin/reports', label: 'Báo cáo', icon: 'fa-chart-bar' },
    ]
  }
  
  // Staff menu - only if primary role is STAFF (and not ADMIN or MANAGER)
  if (primaryRole === 'ROLE_STAFF') {
    return [
      { path: '/admin/dashboard', label: 'Tổng quan', icon: 'fa-home' },
      { path: '/admin/customers', label: 'Khách hàng', icon: 'fa-user' },
      { path: '/admin/tables', label: 'Quản lý bàn', icon: 'fa-chair' },
      { path: '/admin/reservations', label: 'Đặt bàn', icon: 'fa-calendar' },
      { path: '/admin/reservations/checkin', label: 'Check-in bàn', icon: 'fa-check-circle' },
      { path: '/admin/chat', label: 'Chat', icon: 'fa-comments' },
      { path: '/admin/orders', label: 'Đơn hàng', icon: 'fa-shopping-bag' },
    ]
  }
  
  return []
})

const userInitial = computed(() => {
  return authStore.user?.fullName?.charAt(0).toUpperCase() || 'U'
})

const userRole = computed(() => {
  const roles = authStore.user?.roles || []
  if (roles.includes('ROLE_ADMIN')) return 'Quản trị viên'
  if (roles.includes('ROLE_MANAGER')) return 'Quản lý'
  if (roles.includes('ROLE_STAFF')) return 'Nhân viên'
  return 'Người dùng'
})

function isActive(path) {
  // Exact match for check-in path to avoid activating both reservations and check-in
  if (path === '/admin/reservations/checkin') {
    return route.path === path
  }
  // For other paths, use startsWith but ensure it's not a sub-path conflict
  if (path === '/admin/reservations') {
    return route.path.startsWith(path) && route.path !== '/admin/reservations/checkin'
  }
  return route.path.startsWith(path)
}

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function closeSidebar() {
  sidebarOpen.value = false
}

function handleLogout() {
  if (confirm('Bạn có chắc muốn đăng xuất?')) {
    reservationNotificationStore.stopPolling()
    reservationNotificationStore.clear()
    authStore.logout()
    router.push('/login')
  }
}

function toggleNotificationDropdown() {
  showNotificationDropdown.value = !showNotificationDropdown.value
}

function closeNotificationDropdown() {
  showNotificationDropdown.value = false
}

function handleNotificationClick(notification) {
  reservationNotificationStore.markAsRead(notification.id)
  closeNotificationDropdown()
  router.push('/admin/reservations')
}

function markAllAsRead() {
  reservationNotificationStore.markAllAsRead()
}

function removeNotification(notificationId) {
  reservationNotificationStore.remove(notificationId)
}

function formatNotificationTime(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  
  if (diffMins < 1) {
    return 'Vừa xong'
  } else if (diffMins < 60) {
    return `${diffMins} phút trước`
  } else if (diffMins < 1440) {
    const hours = Math.floor(diffMins / 60)
    return `${hours} giờ trước`
  } else {
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

onMounted(() => {
  // Only start notifications if user is staff/admin/manager
  const userRoles = authStore.user?.roles || []
  if (userRoles.some(role => ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'].includes(role))) {
    reservationNotificationStore.start()
  }

  // Handle click outside notification dropdown
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  reservationNotificationStore.stop()
  document.removeEventListener('click', handleClickOutside)
})

function handleClickOutside(event) {
  if (!showNotificationDropdown.value) return
  
  const target = event.target
  const dropdown = notificationDropdown.value
  
  // Check if click is outside dropdown and not on the bell button
  if (dropdown && !dropdown.contains(target)) {
    const bellButton = target.closest('button')
    if (!bellButton || !bellButton.querySelector('.fa-bell')) {
      closeNotificationDropdown()
    }
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
