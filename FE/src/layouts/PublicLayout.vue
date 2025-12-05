<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <!-- Navbar -->
    <nav v-if="!isCustomerOrderPage" class="fixed top-0 left-0 right-0 z-50 bg-white shadow-lg transition-all duration-300">
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between h-20">
          <!-- Logo -->
          <router-link to="/home" class="flex items-center gap-3 group">
            <div class="w-10 h-10 bg-slate-900 rounded-lg flex items-center justify-center">
              <i class="fas fa-bowl-food text-white text-xl"></i>
            </div>
            <div>
              <h1 class="text-lg font-bold text-slate-900">
                Nhà Hàng Lẩu
              </h1>
              <p class="text-xs text-slate-600">
                Hương vị đậm đà
              </p>
            </div>
          </router-link>

          <!-- Desktop Menu -->
          <div class="hidden md:flex items-center gap-8">
            <router-link 
              v-for="item in menuItems" 
              :key="item.path"
              :to="item.path"
              class="nav-link font-medium text-sm text-slate-900 hover:text-slate-700 transition-all duration-200"
            >
              {{ item.label }}
            </router-link>
            <router-link 
              v-if="isAuthenticated"
              to="/my-reservations" 
              class="font-medium text-sm text-slate-900 hover:text-slate-700 transition-all duration-200"
            >
              Lịch sử
            </router-link>
            <router-link 
              to="/reservation" 
              class="font-medium text-sm text-slate-900 hover:text-slate-700 transition-all duration-200"
            >
              Đặt Bàn Ngay
            </router-link>
            
            <!-- Login/Logout buttons -->
            <router-link 
              v-if="!isAuthenticated"
              to="/login" 
              class="font-medium text-sm transition-all duration-200 bg-slate-900 hover:bg-slate-800 text-white px-5 py-2 rounded-lg shadow-sm"
            >
              Đăng nhập
            </router-link>
            
            <button 
              v-if="isAuthenticated"
              @click="handleLogout"
              class="font-medium text-sm text-slate-900 hover:text-slate-700 transition-all duration-200"
            >
              Đăng xuất
            </button>
            

          </div>

          <!-- Mobile Menu Button -->
          <button 
            @click="mobileMenuOpen = !mobileMenuOpen"
            class="md:hidden p-2 rounded-lg text-gray-700"
          >
            <span v-if="!mobileMenuOpen" class="text-2xl">☰</span>
            <span v-else class="text-2xl">✕</span>
          </button>
        </div>
      </div>

      <!-- Mobile Menu -->
      <div 
        v-show="mobileMenuOpen"
        class="md:hidden bg-white border-t animate-slide-down"
      >
        <div class="container mx-auto px-4 py-4 space-y-2">
          <router-link 
            v-for="item in menuItems" 
            :key="item.path"
            :to="item.path"
            @click="mobileMenuOpen = false"
            class="block px-4 py-3 rounded-lg text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition"
          >
            {{ item.label }}
          </router-link>
          <router-link 
            v-if="isAuthenticated"
            to="/my-reservations"
            @click="mobileMenuOpen = false"
            class="block px-4 py-3 rounded-lg text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition"
          >
            Lịch sử đặt bàn
          </router-link>
          
          <!-- Mobile Login/Logout -->
          <router-link 
            v-if="!isAuthenticated"
            to="/login"
            @click="mobileMenuOpen = false"
            class="block px-4 py-3 rounded-lg text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition"
          >
            Đăng nhập
          </router-link>
          
          <button 
            v-if="isAuthenticated"
            @click="handleLogout"
            class="block w-full text-left px-4 py-3 rounded-lg text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition"
          >
            Đăng xuất
          </button>
          
          <router-link 
            to="/reservation"
            @click="mobileMenuOpen = false"
            class="block bg-slate-900 hover:bg-slate-800 text-white text-center px-4 py-3 rounded-lg font-medium mt-4 transition-colors"
          >
            Đặt Bàn Ngay
          </router-link>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main :class="['flex-1', isCustomerOrderPage ? '' : 'pt-20']">
      <router-view />
    </main>

    <!-- Footer -->
    <footer v-if="!isCustomerOrderPage" class="bg-gray-900 text-white">
      <div class="container mx-auto px-4 py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <!-- About -->
          <div>
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 bg-white rounded-lg flex items-center justify-center">
                <i class="fas fa-bowl-food text-slate-900 text-xl"></i>
              </div>
              <h3 class="text-lg font-bold text-white">Nhà Hàng Lẩu</h3>
            </div>
            <p class="text-slate-400 text-sm mb-4 leading-relaxed">
              Mang đến trải nghiệm ẩm thực lẩu đặc sắc với hương vị truyền thống và không gian sang trọng.
            </p>
            <div class="flex gap-2">
              <a href="#" class="w-9 h-9 bg-slate-800 rounded-lg flex items-center justify-center hover:bg-slate-700 transition-colors">
                <i class="fab fa-facebook-f text-white text-sm"></i>
              </a>
              <a href="#" class="w-9 h-9 bg-slate-800 rounded-lg flex items-center justify-center hover:bg-slate-700 transition-colors">
                <i class="fab fa-instagram text-white text-sm"></i>
              </a>
              <a href="#" class="w-9 h-9 bg-slate-800 rounded-lg flex items-center justify-center hover:bg-slate-700 transition-colors">
                <i class="fab fa-youtube text-white text-sm"></i>
              </a>
            </div>
          </div>

          <!-- Quick Links -->
          <div>
            <h4 class="text-base font-semibold mb-3 text-white">Liên Kết</h4>
            <ul class="space-y-2">
              <li v-for="item in menuItems" :key="item.path">
                <router-link :to="item.path" class="text-sm text-slate-400 hover:text-white transition">
                  {{ item.label }}
                </router-link>
              </li>
            </ul>
          </div>

          <!-- Contact -->
          <div>
            <h4 class="text-base font-semibold mb-3 text-white">Liên Hệ</h4>
            <ul class="space-y-2.5 text-slate-400 text-sm">
              <li class="flex items-start gap-2">
                <i class="fas fa-map-marker-alt mt-0.5 text-slate-500"></i>
                <span>123 Đường ABC, Quận 1, TP.HCM</span>
              </li>
              <li class="flex items-center gap-2">
                <i class="fas fa-phone text-slate-500"></i>
                <span>0123 456 789</span>
              </li>
              <li class="flex items-center gap-2">
                <i class="fas fa-envelope text-slate-500"></i>
                <span>contact@nhahang.com</span>
              </li>
            </ul>
          </div>

          <!-- Hours -->
          <div>
            <h4 class="text-base font-semibold mb-3 text-white">Giờ Mở Cửa</h4>
            <ul class="space-y-2 text-slate-400 text-sm">
              <li class="flex justify-between">
                <span>Thứ 2 - Thứ 6:</span>
                <span class="text-white font-medium">10:00 - 22:00</span>
              </li>
              <li class="flex justify-between">
                <span>Thứ 7 - CN:</span>
                <span class="text-white font-medium">09:00 - 23:00</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="border-t border-slate-800 mt-8 pt-6 text-center text-slate-500 text-sm">
          <p>&copy; 2024 Nhà Hàng Lẩu. All rights reserved. | <router-link to="/sitemap" class="hover:text-white transition">Site Map</router-link></p>
        </div>
      </div>
    </footer>

    <!-- Scroll to Top Button -->
    <button
      v-if="!isCustomerOrderPage"
      v-show="showScrollTop"
      @click="scrollToTop"
      class="fixed bottom-8 right-8 w-11 h-11 bg-slate-900 text-white rounded-lg shadow-lg hover:bg-slate-800 transition-colors z-40 flex items-center justify-center"
    >
      ↑
    </button>
  </div>
  
  <!-- Chat Widget for Customer -->
  <CustomerChatWidget v-if="!isCustomerOrderPage" />
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import CustomerChatWidget from '@/components/CustomerChatWidget.vue'

const router = useRouter()
const route = useRoute()

// Kiểm tra xem có phải route customer order không (ẩn header và footer)
const isCustomerOrderPage = computed(() => route.path === '/customer/order')

// Lazy load authStore to avoid circular dependency
const authStore = ref(null)
const isAuthenticated = computed(() => authStore.value?.isAuthenticated || false)

const scrolled = ref(false)
const mobileMenuOpen = ref(false)
const showScrollTop = ref(false)

const menuItems = [
  { path: '/home', label: 'Trang Chủ' },
  { path: '/services', label: 'Dịch Vụ' },
  { path: '/menu', label: 'Thực Đơn' },
  { path: '/about', label: 'Giới Thiệu' }
]

function handleScroll() {
  scrolled.value = window.scrollY > 50
  showScrollTop.value = window.scrollY > 300
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleLogout() {
  if (confirm('Bạn có chắc muốn đăng xuất?')) {
    if (authStore.value) {
      authStore.value.logout()
    }
    // Close mobile menu if open
    mobileMenuOpen.value = false
    // Redirect to home page
    router.push('/home')
  }
}

onMounted(async () => {
  // Load authStore
  try {
    const { useAuthStore } = await import('@/stores/auth')
    authStore.value = useAuthStore()
  } catch (e) {
    console.warn('AuthStore not available:', e)
  }
  
  // Check initial scroll position
  handleScroll()
  
  // Setup scroll listener
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.nav-link {
  position: relative;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: currentColor;
  transition: width 0.3s;
}

.nav-link:hover::after,
.nav-link.router-link-active::after {
  width: 100%;
}

@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slide-down {
  animation: slide-down 0.3s ease-out;
}
</style>
