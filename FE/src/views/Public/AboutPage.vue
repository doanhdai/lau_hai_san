<template>
  <div>
    <!-- Hero -->
    <section class="relative h-96 flex items-center justify-center bg-gradient-to-br from-sky-600 to-blue-700">
      <div class="relative z-10 text-center text-white px-4">
        <h1 class="text-5xl md:text-6xl font-bold mb-4">Về Chúng Tôi</h1>
        <p class="text-xl">Câu chuyện về hành trình ẩm thực</p>
      </div>
    </section>

    <!-- Story -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-12 scroll-animate">
            <h2 class="text-4xl font-bold text-gray-900 mb-6">Câu Chuyện Của Chúng Tôi</h2>
            <div class="w-20 h-1 bg-sky-600 mx-auto mb-8"></div>
            <div class="space-y-6 text-lg text-gray-700 leading-relaxed text-left">
              <p>Được thành lập từ năm 2010, <strong>Nhà Hàng Lẩu</strong> bắt đầu từ một quán nhỏ với niềm đam mê mang đến hương vị lẩu truyền thống Việt Nam kết hợp với các món lẩu quốc tế.</p>
              <p>Trải qua hơn 14 năm phát triển, chúng tôi tự hào là một trong những chuỗi nhà hàng lẩu uy tín hàng đầu tại Việt Nam với <strong>15 chi nhánh</strong> trải dài khắp cả nước.</p>
              <p>Sứ mệnh của chúng tôi là mang đến những bữa tiệc lẩu hoàn hảo, nơi mọi người có thể sum họp, chia sẻ và tạo nên những kỷ niệm đáng nhớ cùng gia đình và bạn bè.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Values -->
    <section class="py-20 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16 scroll-animate">
          <h2 class="text-4xl font-bold text-gray-900 mb-4">Giá Trị Cốt Lõi</h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="text-center scroll-animate">
            <div class="w-20 h-20 bg-gradient-to-br from-sky-500 to-sky-600 rounded-full flex items-center justify-center text-4xl mx-auto mb-4">
              ❤️
            </div>
            <h3 class="text-2xl font-bold text-gray-900 mb-3">Tận Tâm</h3>
            <p class="text-gray-600">Phục vụ khách hàng bằng cả trái tim, luôn lắng nghe và đáp ứng mọi nhu cầu</p>
          </div>
          <div class="text-center scroll-animate animation-delay-200">
            <div class="w-20 h-20 bg-gradient-to-br from-sky-500 to-sky-600 rounded-full flex items-center justify-center text-4xl mx-auto mb-4">
              ✨
            </div>
            <h3 class="text-2xl font-bold text-gray-900 mb-3">Chất Lượng</h3>
            <p class="text-gray-600">Cam kết sử dụng nguyên liệu tươi ngon, chất lượng cao nhất</p>
          </div>
          <div class="text-center scroll-animate animation-delay-400">
            <div class="w-20 h-20 bg-gradient-to-br from-sky-500 to-sky-600 rounded-full flex items-center justify-center text-4xl mx-auto mb-4">
              🌟
            </div>
            <h3 class="text-2xl font-bold text-gray-900 mb-3">Đổi Mới</h3>
            <p class="text-gray-600">Không ngừng sáng tạo, cải tiến thực đơn và dịch vụ</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Team -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16 scroll-animate">
          <h2 class="text-4xl font-bold text-gray-900 mb-4">Đội Ngũ Của Chúng Tôi</h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div v-for="(member, index) in team" :key="index" class="text-center scroll-animate" :style="{ animationDelay: `${index * 100}ms` }">
            <div class="w-32 h-32 bg-gradient-to-br from-red-500 to-red-600 rounded-full flex items-center justify-center text-white text-4xl mx-auto mb-4 transform hover:scale-110 transition">
              {{ member.avatar }}
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-1">{{ member.name }}</h3>
            <p class="text-red-600 font-semibold mb-2">{{ member.role }}</p>
            <p class="text-gray-600 text-sm">{{ member.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Location -->
    <section class="py-20 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16 scroll-animate">
          <h2 class="text-4xl font-bold text-gray-900 mb-4">Vị Trí Nhà Hàng</h2>
        </div>

        <div class="bg-white rounded-2xl shadow-xl overflow-hidden">
          <div class="aspect-video bg-gray-200 flex items-center justify-center">
            <p class="text-2xl text-gray-500">🗺️ Google Maps</p>
          </div>
          <div class="p-8">
            <h3 class="text-2xl font-bold text-gray-900 mb-4">Địa Chỉ Chi Nhánh Chính</h3>
            <div class="space-y-3 text-lg text-gray-700">
              <p>📍 123 Đường ABC, Phường XYZ, Quận 1, TP.Hồ Chí Minh</p>
              <p>📞 Hotline: 0123 456 789</p>
              <p>✉️ Email: contact@nhahang.com</p>
              <p>🕐 Giờ mở cửa: 10:00 - 22:00 (Thứ 2 - Chủ Nhật)</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

const team = [
  { name: 'Nguyễn Văn A', role: 'CEO & Founder', avatar: '👨‍💼', description: '15 năm kinh nghiệm trong ngành F&B' },
  { name: 'Trần Thị B', role: 'Head Chef', avatar: '👩‍🍳', description: 'Đầu bếp 5 sao với nhiều giải thưởng' },
  { name: 'Lê Văn C', role: 'Manager', avatar: '👨‍💻', description: 'Chuyên gia quản lý nhà hàng' },
  { name: 'Phạm Thị D', role: 'Marketing Director', avatar: '👩‍💼', description: 'Sáng tạo các chiến dịch thương hiệu' }
]

function handleScroll() {
  const elements = document.querySelectorAll('.scroll-animate')
  elements.forEach(el => {
    const rect = el.getBoundingClientRect()
    if (rect.top < window.innerHeight * 0.8) {
      el.classList.add('animate-fade-in-up')
    }
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll()
})
</script>

<style scoped>
@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 0.8s ease-out forwards;
}

.animation-delay-200 {
  animation-delay: 0.2s;
}

.animation-delay-400 {
  animation-delay: 0.4s;
}

.scroll-animate {
  opacity: 0;
}
</style>
