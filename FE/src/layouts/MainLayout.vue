<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside 
      class="fixed inset-y-0 left-0 z-50 w-64 bg-gradient-to-b from-sky-600 to-sky-800 text-white transform transition-transform duration-300 ease-in-out lg:translate-x-0"
      :class="{ '-translate-x-full': !sidebarOpen }"
    >
      <!-- Logo -->
      <div class="flex items-center justify-center h-16 bg-sky-900 bg-opacity-50">
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <span class="text-3xl">🍲</span>
          Hotpot Manager
        </h1>
      </div>

      <!-- Navigation -->
      <nav class="mt-6 px-4 space-y-2">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-sky-700"
          :class="{ 'bg-sky-700 shadow-lg': isActive(item.path) }"
        >
          <span class="text-lg">{{ item.icon }}</span>
          <span class="font-medium">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- User Info -->
      <div class="absolute bottom-0 left-0 right-0 p-4 bg-sky-900 bg-opacity-50">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-sky-500 flex items-center justify-center">
            <span class="text-lg font-bold">{{ userInitial }}</span>
          </div>
          <div class="flex-1">
            <p class="font-medium text-sm">{{ authStore.user?.fullName }}</p>
            <p class="text-xs text-sky-200">{{ userRole }}</p>
          </div>
          <button @click="handleLogout" class="p-2 hover:bg-sky-700 rounded-lg transition">
            <span class="text-lg">🚪</span>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col lg:ml-64">
      <!-- Header -->
      <header class="h-16 bg-white shadow-sm flex items-center justify-between px-6">
        <button
          @click="toggleSidebar"
          class="lg:hidden p-2 rounded-lg hover:bg-gray-100"
        >
          <span class="text-xl">☰</span>
        </button>

        <div class="flex-1"></div>

        <div class="flex items-center gap-4">
          <!-- Notifications -->
          <button class="relative p-2 rounded-lg hover:bg-gray-100">
            <span class="text-xl text-gray-600">🔔</span>
            <span class="absolute top-1 right-1 w-2 h-2 bg-sky-500 rounded-full"></span>
          </button>
        </div>
      </header>

      <!-- Page Content -->
      <main class="flex-1 overflow-y-auto p-6">
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
// Temporarily comment out heroicons to fix loading issue
// import {
//   HomeIcon,
//   UsersIcon,
//   TableCellsIcon,
//   BuildingStorefrontIcon,
//   CakeIcon,
//   TagIcon,
//   ShoppingBagIcon,
//   CalendarDaysIcon,
//   GiftIcon,
//   ChatBubbleLeftRightIcon,
//   ChartBarIcon,
//   Bars3Icon,
//   BellIcon,
//   ArrowRightOnRectangleIcon
// } from '@heroicons/vue/24/outline'
import NotificationList from '@/components/NotificationList.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const sidebarOpen = ref(false)

const menuItems = computed(() => {
  const userRoles = authStore.user?.roles || []
  
  // Admin menu
  if (userRoles.includes('ROLE_ADMIN')) {
    return [
      { path: '/admin/dashboard', label: 'Tổng quan', icon: '🏠' },
      { path: '/admin/users', label: 'Người dùng', icon: '👥' },
      { path: '/admin/customers', label: 'Khách hàng', icon: '👤' },
      { path: '/admin/tables', label: 'Quản lý bàn', icon: '🪑' },
      { path: '/admin/rooms', label: 'Quản lý phòng', icon: '🏢' },
      { path: '/admin/dishes', label: 'Thực đơn', icon: '🍽️' },
      { path: '/admin/categories', label: 'Danh mục', icon: '🏷️' },
      { path: '/admin/orders', label: 'Đơn hàng', icon: '🛒' },
      { path: '/admin/reservations', label: 'Đặt bàn', icon: '📅' },
      { path: '/admin/promotions', label: 'Khuyến mãi', icon: '🎁' },
      { path: '/admin/feedbacks', label: 'Phản hồi', icon: '💬' },
      { path: '/admin/reports', label: 'Báo cáo', icon: '📊' },
    ]
  }
  
  // Manager menu
  if (userRoles.includes('ROLE_MANAGER')) {
    return [
      { path: '/manager/dashboard', label: 'Tổng quan', icon: '🏠' },
      { path: '/admin/customers', label: 'Khách hàng', icon: '👤' },
      { path: '/admin/tables', label: 'Quản lý bàn', icon: '🪑' },
      { path: '/admin/rooms', label: 'Quản lý phòng', icon: '🏢' },
      { path: '/admin/dishes', label: 'Thực đơn', icon: '🍽️' },
      { path: '/admin/categories', label: 'Danh mục', icon: '🏷️' },
      { path: '/admin/orders', label: 'Đơn hàng', icon: '🛒' },
      { path: '/admin/reservations', label: 'Đặt bàn', icon: '📅' },
      { path: '/admin/promotions', label: 'Khuyến mãi', icon: '🎁' },
      { path: '/admin/feedbacks', label: 'Phản hồi', icon: '💬' },
      { path: '/admin/reports', label: 'Báo cáo', icon: '📊' },
    ]
  }
  
  // Staff menu
  if (userRoles.includes('ROLE_STAFF')) {
    return [
      { path: '/staff/dashboard', label: 'Tổng quan', icon: '🏠' },
      { path: '/admin/customers', label: 'Khách hàng', icon: '👤' },
      { path: '/admin/tables', label: 'Quản lý bàn', icon: '🪑' },
      { path: '/admin/reservations', label: 'Đặt bàn', icon: '📅' },
      { path: '/admin/orders', label: 'Đơn hàng', icon: '🛒' },
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
    authStore.logout()
    router.push('/login')
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
