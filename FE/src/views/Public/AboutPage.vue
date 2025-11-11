<template>
  <div>
    <!-- Hero -->
    <section class="relative h-64 flex items-center justify-center bg-slate-900">
      <div class="absolute inset-0 bg-black/40"></div>
      <div class="relative z-10 text-center text-white px-4 max-w-3xl mx-auto">
        <h1 class="text-3xl md:text-4xl font-bold mb-3 tracking-tight">Về Chúng Tôi</h1>
        <p class="text-base md:text-lg text-slate-200">Câu chuyện về hành trình ẩm thực</p>
      </div>
    </section>

    <!-- Story -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-10 scroll-animate">
            <h2 class="text-3xl md:text-4xl font-bold text-slate-900 mb-4">Câu Chuyện Của Chúng Tôi</h2>
            <div class="w-16 h-0.5 bg-slate-900 mx-auto mb-6"></div>
            <div class="space-y-4 text-base text-slate-700 leading-relaxed text-left">
              <p>Được thành lập từ năm 2010, <strong>Nhà Hàng Lẩu</strong> bắt đầu từ một quán nhỏ với niềm đam mê mang đến hương vị lẩu truyền thống Việt Nam kết hợp với các món lẩu quốc tế.</p>
              <p>Trải qua hơn 14 năm phát triển, chúng tôi tự hào là một trong những chuỗi nhà hàng lẩu uy tín hàng đầu tại Việt Nam với <strong>15 chi nhánh</strong> trải dài khắp cả nước.</p>
              <p>Sứ mệnh của chúng tôi là mang đến những bữa tiệc lẩu hoàn hảo, nơi mọi người có thể sum họp, chia sẻ và tạo nên những kỷ niệm đáng nhớ cùng gia đình và bạn bè.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Values -->
    <section class="py-16 bg-slate-50 border-y border-gray-200">
      <div class="container mx-auto px-4">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 mb-3">Giá Trị Cốt Lõi</h2>
          <div class="w-16 h-0.5 bg-slate-900 mx-auto"></div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="text-center scroll-animate">
            <div class="w-16 h-16 bg-slate-900 rounded-lg flex items-center justify-center mx-auto mb-4">
              <i class="fas fa-heart text-white text-2xl"></i>
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-2">Tận Tâm</h3>
            <p class="text-slate-600 text-sm">Phục vụ khách hàng bằng cả trái tim, luôn lắng nghe và đáp ứng mọi nhu cầu</p>
          </div>
          <div class="text-center scroll-animate animation-delay-200">
            <div class="w-16 h-16 bg-slate-900 rounded-lg flex items-center justify-center mx-auto mb-4">
              <i class="fas fa-award text-white text-2xl"></i>
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-2">Chất Lượng</h3>
            <p class="text-slate-600 text-sm">Cam kết sử dụng nguyên liệu tươi ngon, chất lượng cao nhất</p>
          </div>
          <div class="text-center scroll-animate animation-delay-400">
            <div class="w-16 h-16 bg-slate-900 rounded-lg flex items-center justify-center mx-auto mb-4">
              <i class="fas fa-lightbulb text-white text-2xl"></i>
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-2">Đổi Mới</h3>
            <p class="text-slate-600 text-sm">Không ngừng sáng tạo, cải tiến thực đơn và dịch vụ</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Team -->
    <section class="py-16 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 mb-3">Đội Ngũ Của Chúng Tôi</h2>
          <div class="w-16 h-0.5 bg-slate-900 mx-auto"></div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div v-for="(member, index) in team" :key="index" class="text-center scroll-animate" :style="{ animationDelay: `${index * 100}ms` }">
            <div class="w-20 h-20 bg-slate-900 rounded-lg flex items-center justify-center mx-auto mb-3">
              <i :class="['fas', member.avatar]" class="text-white text-2xl"></i>
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-1">{{ member.name }}</h3>
            <p class="text-slate-700 font-medium mb-2 text-sm">{{ member.role }}</p>
            <p class="text-slate-600 text-xs">{{ member.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Location -->
    <section class="py-16 bg-slate-50 border-y border-gray-200">
      <div class="container mx-auto px-4">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 mb-3">Vị Trí Nhà Hàng</h2>
          <div class="w-16 h-0.5 bg-slate-900 mx-auto"></div>
        </div>

        <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden max-w-4xl mx-auto">
          <div class="aspect-video bg-gray-100 flex items-center justify-center">
            <i class="fas fa-map-marked-alt text-4xl text-slate-400"></i>
          </div>
          <div class="p-6 md:p-8">
            <h3 class="text-xl font-bold text-slate-900 mb-4">Địa Chỉ Chi Nhánh Chính</h3>
            <div class="space-y-2.5 text-sm md:text-base text-slate-700">
              <p class="flex items-start gap-2">
                <i class="fas fa-map-marker-alt mt-1 text-slate-500"></i>
                <span>123 Đường ABC, Phường XYZ, Quận 1, TP.Hồ Chí Minh</span>
              </p>
              <p class="flex items-center gap-2">
                <i class="fas fa-phone text-slate-500"></i>
                <span>Hotline: 0123 456 789</span>
              </p>
              <p class="flex items-center gap-2">
                <i class="fas fa-envelope text-slate-500"></i>
                <span>Email: contact@nhahang.com</span>
              </p>
              <p class="flex items-center gap-2">
                <i class="fas fa-clock text-slate-500"></i>
                <span>Giờ mở cửa: 10:00 - 22:00 (Thứ 2 - Chủ Nhật)</span>
              </p>
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
  { name: 'Nguyễn Văn A', role: 'CEO & Founder', avatar: 'fa-user-tie', description: '15 năm kinh nghiệm trong ngành F&B' },
  { name: 'Trần Thị B', role: 'Head Chef', avatar: 'fa-utensils', description: 'Đầu bếp 5 sao với nhiều giải thưởng' },
  { name: 'Lê Văn C', role: 'Manager', avatar: 'fa-user-gear', description: 'Chuyên gia quản lý nhà hàng' },
  { name: 'Phạm Thị D', role: 'Marketing Director', avatar: 'fa-user-tie', description: 'Sáng tạo các chiến dịch thương hiệu' }
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
