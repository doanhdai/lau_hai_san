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
          <button class="relative p-2 rounded-lg hover:bg-gray-100 transition-colors">
            <i class="fas fa-bell text-slate-600"></i>
            <span class="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full"></span>
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
  
  // Manager menu
  if (userRoles.includes('ROLE_MANAGER')) {
    return [
      { path: '/manager/dashboard', label: 'Tổng quan', icon: 'fa-home' },
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
  
  // Staff menu
  if (userRoles.includes('ROLE_STAFF')) {
    return [
      { path: '/staff/dashboard', label: 'Tổng quan', icon: 'fa-home' },
      { path: '/admin/customers', label: 'Khách hàng', icon: 'fa-user' },
      { path: '/admin/tables', label: 'Quản lý bàn', icon: 'fa-chair' },
      { path: '/admin/reservations', label: 'Đặt bàn', icon: 'fa-calendar' },
      { path: '/admin/reservations/checkin', label: 'Check-in bàn', icon: 'fa-check-circle' },
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
