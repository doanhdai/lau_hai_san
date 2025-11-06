<template>
  <div>
    <!-- Hero -->
    <section class="relative h-96 flex items-center justify-center bg-gradient-to-br from-red-600 to-red-700">
      <div class="relative z-10 text-center text-white px-4">
        <h1 class="text-5xl md:text-6xl font-bold mb-4">Sơ Đồ Website</h1>
        <p class="text-xl">Tất cả trang và dịch vụ trong một nơi</p>
      </div>
    </section>

    <!-- Site Map -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <!-- Main Pages -->
          <div>
            <h3 class="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
              <span class="text-red-600">📄</span> Trang Chính
            </h3>
            <ul class="space-y-3">
              <li v-for="page in mainPages" :key="page.path">
                <router-link 
                  :to="page.path"
                  class="flex items-center gap-2 text-gray-700 hover:text-red-600 transition group"
                >
                  <span class="transform group-hover:translate-x-2 transition">→</span>
                  <span class="font-medium">{{ page.name }}</span>
                </router-link>
              </li>
            </ul>
          </div>

          <!-- Services -->
          <div>
            <h3 class="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
              <span class="text-red-600">⚙️</span> Dịch Vụ
            </h3>
            <ul class="space-y-3">
              <li v-for="service in services" :key="service.id">
                <a 
                  href="#"
                  class="flex items-center gap-2 text-gray-700 hover:text-red-600 transition group"
                >
                  <span class="transform group-hover:translate-x-2 transition">→</span>
                  <span class="font-medium">{{ service.name }}</span>
                </a>
              </li>
            </ul>
          </div>

          <!-- Information -->
          <div>
            <h3 class="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
              <span class="text-red-600">ℹ️</span> Thông Tin
            </h3>
            <ul class="space-y-3">
              <li v-for="info in information" :key="info.id">
                <a 
                  href="#"
                  class="flex items-center gap-2 text-gray-700 hover:text-red-600 transition group"
                >
                  <span class="transform group-hover:translate-x-2 transition">→</span>
                  <span class="font-medium">{{ info.name }}</span>
                </a>
              </li>
            </ul>
          </div>
        </div>

        <!-- Contact Section -->
        <div class="mt-16 bg-gray-50 rounded-2xl p-8">
          <div class="text-center mb-8">
            <h3 class="text-3xl font-bold text-gray-900 mb-2">Liên Hệ Với Chúng Tôi</h3>
            <p class="text-gray-600">Chúng tôi luôn sẵn sàng hỗ trợ bạn</p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="text-center">
              <div class="w-16 h-16 bg-red-600 rounded-full flex items-center justify-center text-2xl mx-auto mb-3">
                📞
              </div>
              <h4 class="font-bold text-gray-900 mb-1">Hotline</h4>
              <p class="text-gray-600">0123 456 789</p>
            </div>
            <div class="text-center">
              <div class="w-16 h-16 bg-red-600 rounded-full flex items-center justify-center text-2xl mx-auto mb-3">
                ✉️
              </div>
              <h4 class="font-bold text-gray-900 mb-1">Email</h4>
              <p class="text-gray-600">contact@nhahang.com</p>
            </div>
            <div class="text-center">
              <div class="w-16 h-16 bg-red-600 rounded-full flex items-center justify-center text-2xl mx-auto mb-3">
                📍
              </div>
              <h4 class="font-bold text-gray-900 mb-1">Địa chỉ</h4>
              <p class="text-gray-600">123 Đường ABC, Q.1, TP.HCM</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
const mainPages = [
  { name: 'Trang Chủ', path: '/home' },
  { name: 'Dịch Vụ', path: '/services' },
  { name: 'Thực Đơn', path: '/menu' },
  { name: 'Giới Thiệu', path: '/about' },
  { name: 'Đặt Bàn', path: '/reservation' },
  { name: 'Sơ Đồ Website', path: '/sitemap' }
]

const services = [
  { id: 1, name: 'Tiệc Gia Đình & Bạn Bè' },
  { id: 2, name: 'Tiệc Công Ty & Sự Kiện' },
  { id: 3, name: 'Tiệc Sinh Nhật & Kỷ Niệm' },
  { id: 4, name: 'Giao Hàng Tận Nơi' },
  { id: 5, name: 'Phòng VIP' },
  { id: 6, name: 'Đặt Bàn Online' }
]

const information = [
  { id: 1, name: 'Về Chúng Tôi' },
  { id: 2, name: 'Điều Khoản Sử Dụng' },
  { id: 3, name: 'Chính Sách Bảo Mật' },
  { id: 4, name: 'Câu Hỏi Thường Gặp' },
  { id: 5, name: 'Tuyển Dụng' },
  { id: 6, name: 'Tin Tức & Sự Kiện' }
]
</script>
