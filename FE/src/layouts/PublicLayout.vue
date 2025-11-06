<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <!-- Navbar -->
    <nav class="fixed top-0 left-0 right-0 z-50 transition-all duration-300" :class="scrolled ? 'bg-white shadow-lg' : 'bg-transparent'">
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between h-20">
          <!-- Logo -->
          <router-link to="/home" class="flex items-center gap-3 group">
            <div class="w-12 h-12 bg-gradient-to-br from-sky-500 to-blue-600 rounded-full flex items-center justify-center transform group-hover:scale-110 transition-transform">
              <span class="text-white text-2xl font-bold">🍲</span>
            </div>
            <div>
              <h1 class="text-xl font-bold" :class="scrolled ? 'text-gray-900' : 'text-white'">
                Nhà Hàng Lẩu
              </h1>
              <p class="text-xs" :class="scrolled ? 'text-gray-600' : 'text-white/80'">
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
              class="nav-link font-medium transition-all duration-300"
              :class="scrolled ? 'text-gray-700 hover:text-sky-600' : 'text-white hover:text-sky-200'"
            >
              {{ item.label }}
            </router-link>
            <router-link 
              v-if="isAuthenticated"
              to="/my-reservations" 
              class="font-medium transition-all duration-300"
              :class="scrolled ? 'text-gray-700 hover:text-sky-600' : 'text-white hover:text-sky-200'"
            >
              Lịch sử
            </router-link>
            
            <!-- Login/Logout buttons -->
            <router-link 
              v-if="!isAuthenticated"
              to="/login" 
              class="font-medium transition-all duration-300"
              :class="scrolled ? 'text-gray-700 hover:text-sky-600' : 'text-white hover:text-sky-200'"
            >
              Đăng nhập
            </router-link>
            
            <button 
              v-if="isAuthenticated"
              @click="handleLogout"
              class="font-medium transition-all duration-300"
              :class="scrolled ? 'text-gray-700 hover:text-sky-600' : 'text-white hover:text-sky-200'"
            >
              Đăng xuất
            </button>
            
            <router-link 
              to="/reservation" 
              class="btn-primary px-6 py-2 shadow-lg hover:shadow-xl transform hover:scale-105 transition-all"
            >
              Đặt Bàn Ngay
            </router-link>
          </div>

          <!-- Mobile Menu Button -->
          <button 
            @click="mobileMenuOpen = !mobileMenuOpen"
            class="md:hidden p-2 rounded-lg"
            :class="scrolled ? 'text-gray-700' : 'text-white'"
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
            class="block px-4 py-3 rounded-lg text-gray-700 hover:bg-sky-50 hover:text-sky-600 transition"
          >
            {{ item.label }}
          </router-link>
          <router-link 
            v-if="isAuthenticated"
            to="/my-reservations"
            @click="mobileMenuOpen = false"
            class="block px-4 py-3 rounded-lg text-gray-700 hover:bg-sky-50 hover:text-sky-600 transition"
          >
            Lịch sử đặt bàn
          </router-link>
          
          <!-- Mobile Login/Logout -->
          <router-link 
            v-if="!isAuthenticated"
            to="/login"
            @click="mobileMenuOpen = false"
            class="block px-4 py-3 rounded-lg text-gray-700 hover:bg-sky-50 hover:text-sky-600 transition"
          >
            Đăng nhập
          </router-link>
          
          <button 
            v-if="isAuthenticated"
            @click="handleLogout"
            class="block w-full text-left px-4 py-3 rounded-lg text-gray-700 hover:bg-sky-50 hover:text-sky-600 transition"
          >
            Đăng xuất
          </button>
          
          <router-link 
            to="/reservation"
            @click="mobileMenuOpen = false"
            class="block btn-primary text-center mt-4"
          >
            Đặt Bàn Ngay
          </router-link>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="flex-1">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="bg-gray-900 text-white">
      <div class="container mx-auto px-4 py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <!-- About -->
          <div>
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 bg-gradient-to-br from-sky-500 to-blue-600 rounded-full flex items-center justify-center">
                <span class="text-white text-xl">🍲</span>
              </div>
              <h3 class="text-xl font-bold">Nhà Hàng Lẩu</h3>
            </div>
            <p class="text-gray-400 mb-4">
              Mang đến trải nghiệm ẩm thực lẩu đặc sắc với hương vị truyền thống và không gian sang trọng.
            </p>
            <div class="flex gap-3">
              <a href="#" class="w-10 h-10 bg-gray-800 rounded-full flex items-center justify-center hover:bg-sky-600 transition">
                <span>📘</span>
              </a>
              <a href="#" class="w-10 h-10 bg-gray-800 rounded-full flex items-center justify-center hover:bg-sky-600 transition">
                <span>📷</span>
              </a>
              <a href="#" class="w-10 h-10 bg-gray-800 rounded-full flex items-center justify-center hover:bg-sky-600 transition">
                <span>🎬</span>
              </a>
            </div>
          </div>

          <!-- Quick Links -->
          <div>
            <h4 class="text-lg font-bold mb-4">Liên Kết</h4>
            <ul class="space-y-2">
              <li v-for="item in menuItems" :key="item.path">
                <router-link :to="item.path" class="text-gray-400 hover:text-sky-400 transition">
                  {{ item.label }}
                </router-link>
              </li>
            </ul>
          </div>

          <!-- Contact -->
          <div>
            <h4 class="text-lg font-bold mb-4">Liên Hệ</h4>
            <ul class="space-y-3 text-gray-400">
              <li class="flex items-start gap-2">
                <span>📍</span>
                <span>123 Đường ABC, Quận 1, TP.HCM</span>
              </li>
              <li class="flex items-center gap-2">
                <span>📞</span>
                <span>0123 456 789</span>
              </li>
              <li class="flex items-center gap-2">
                <span>✉️</span>
                <span>contact@nhahang.com</span>
              </li>
            </ul>
          </div>

          <!-- Hours -->
          <div>
            <h4 class="text-lg font-bold mb-4">Giờ Mở Cửa</h4>
            <ul class="space-y-2 text-gray-400">
              <li class="flex justify-between">
                <span>Thứ 2 - Thứ 6:</span>
                <span class="text-white">10:00 - 22:00</span>
              </li>
              <li class="flex justify-between">
                <span>Thứ 7 - CN:</span>
                <span class="text-white">09:00 - 23:00</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
          <p>&copy; 2024 Nhà Hàng Lẩu. All rights reserved. | <router-link to="/sitemap" class="hover:text-sky-400">Site Map</router-link></p>
        </div>
      </div>
    </footer>

    <!-- Scroll to Top Button -->
    <button
      v-show="showScrollTop"
      @click="scrollToTop"
      class="fixed bottom-8 right-8 w-12 h-12 bg-sky-600 text-white rounded-full shadow-lg hover:bg-sky-700 hover:shadow-xl transform hover:scale-110 transition-all z-40 flex items-center justify-center"
    >
      ↑
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

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
