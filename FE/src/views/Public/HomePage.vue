<template>
  <div class="overflow-hidden">
    <!-- Hero Section -->
    <section class="relative h-screen flex items-center justify-center overflow-hidden">
      <!-- Background with parallax effect -->
      <div 
        class="absolute inset-0 bg-cover bg-center transform scale-110"
        :style="{ 
          backgroundImage: 'url(https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=1920)',
          transform: `scale(1.1) translateY(${scrollY * 0.5}px)`
        }"
      ></div>
      <div class="absolute inset-0 bg-black/60"></div>
      
      <!-- Hero Content -->
      <div class="relative z-10 text-center px-4 animate-fade-in-up">
        <h1 class="text-4xl md:text-6xl font-bold text-white mb-5 tracking-tight">
          Trải Nghiệm <span class="text-white/90">Lẩu</span> Đích Thực
        </h1>
        <p class="text-lg md:text-xl text-white/85 mb-8 max-w-2xl mx-auto leading-relaxed">
          Hương vị truyền thống kết hợp không gian hiện đại, mang đến bữa tiệc lẩu hoàn hảo cho gia đình và bạn bè
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center animate-slide-up animation-delay-400">
          <router-link 
            to="/reservation" 
            class="bg-slate-900 hover:bg-slate-800 text-white px-8 py-3.5 rounded-lg font-semibold text-base transition-colors shadow-lg"
          >
            Đặt Bàn Ngay
          </router-link>
          <router-link 
            to="/menu" 
            class="bg-white/95 hover:bg-white text-slate-900 px-8 py-3.5 rounded-lg font-semibold text-base border border-white/20 transition-colors shadow-lg"
          >
            Xem Thực Đơn
          </router-link>
        </div>
      </div>

      <!-- Scroll Indicator -->
      <div class="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce">
        <div class="w-6 h-10 border-2 border-white/50 rounded-full flex items-start justify-center p-2">
          <div class="w-1 h-3 bg-white rounded-full animate-scroll-down"></div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 mb-3">Tại Sao Chọn Chúng Tôi?</h2>
          <div class="w-16 h-0.5 bg-slate-900 mx-auto"></div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div 
            v-for="(feature, index) in features" 
            :key="index"
            class="group scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="bg-white border border-gray-200 p-8 rounded-lg hover:shadow-lg transition-all duration-200">
              <div class="w-14 h-14 bg-slate-900 rounded-lg flex items-center justify-center mb-4">
                <i :class="['fas', feature.icon]" class="text-white text-2xl"></i>
              </div>
              <h3 class="text-xl font-bold text-slate-900 mb-2">{{ feature.title }}</h3>
              <p class="text-slate-600 leading-relaxed text-sm">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Menu Preview Section -->
    <section class="py-20 bg-slate-900 text-white">

      <div class="container mx-auto px-4">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl md:text-4xl font-bold mb-3 text-white">Thực Đơn Đặc Biệt</h2>
          <p class="text-lg text-slate-300">Khám phá các món lẩu độc đáo và hấp dẫn</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div 
            v-for="(dish, index) in popularDishes" 
            :key="index"
            class="group scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden hover:border-slate-600 transition-all duration-200">
              <div class="aspect-square bg-slate-700/50 overflow-hidden">
                <div class="w-full h-full flex items-center justify-center opacity-60">
                  <i :class="['fas', dish.icon]" class="text-5xl text-white"></i>
                </div>
              </div>
              <div class="p-5">
                <h3 class="text-lg font-semibold mb-1.5 text-white">{{ dish.name }}</h3>
                <p class="text-slate-400 text-sm mb-3 line-clamp-2">{{ dish.description }}</p>
                <div class="flex items-center justify-between pt-3 border-t border-slate-700">
                  <span class="text-xl font-bold text-white">{{ formatPrice(dish.price) }}</span>
                  <button class="bg-white hover:bg-slate-100 text-slate-900 px-4 py-2 rounded-md text-sm font-medium transition-colors">
                    Đặt món
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="text-center mt-10 scroll-animate">
          <router-link 
            to="/menu" 
            class="inline-block bg-white hover:bg-slate-100 text-slate-900 px-8 py-3 rounded-lg font-semibold text-base transition-colors shadow-sm"
          >
            Xem Tất Cả Thực Đơn
          </router-link>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="py-16 bg-slate-50 border-y border-gray-200">
      <div class="container mx-auto px-4">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div 
            v-for="(stat, index) in stats" 
            :key="index"
            class="text-center scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="text-4xl md:text-5xl font-bold mb-2 text-slate-900 counter" :data-target="stat.value">0</div>
            <div class="text-sm md:text-base text-slate-600 font-medium">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Testimonials Section -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 mb-3">Khách Hàng Nói Gì?</h2>
          <div class="w-16 h-0.5 bg-slate-900 mx-auto"></div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div 
            v-for="(testimonial, index) in testimonials" 
            :key="index"
            class="scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="bg-white border border-gray-200 p-6 rounded-lg hover:shadow-md transition-all">
              <div class="flex items-center gap-1 text-amber-500 mb-3">
                <i v-for="i in 5" :key="i" class="fas fa-star text-sm"></i>
              </div>
              <p class="text-slate-700 text-sm mb-5 italic leading-relaxed">"{{ testimonial.comment }}"</p>
              <div class="flex items-center gap-3 pt-4 border-t border-gray-100">
                <div class="w-10 h-10 bg-slate-900 rounded-full flex items-center justify-center text-white font-semibold text-sm">
                  {{ testimonial.name.charAt(0) }}
                </div>
                <div>
                  <div class="font-semibold text-slate-900 text-sm">{{ testimonial.name }}</div>
                  <div class="text-xs text-slate-500">{{ testimonial.role }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-16 bg-slate-900 text-white">
      <div class="container mx-auto px-4">
        <div class="max-w-2xl mx-auto text-center scroll-animate">
          <h2 class="text-3xl md:text-4xl font-bold mb-4">Sẵn Sàng Trải Nghiệm?</h2>
          <p class="text-lg text-slate-300 mb-6">
            Đặt bàn ngay hôm nay và nhận ưu đãi đặc biệt cho khách hàng mới
          </p>
          <router-link 
            to="/reservation" 
            class="inline-block bg-white hover:bg-slate-100 text-slate-900 px-8 py-3.5 rounded-lg font-semibold text-base transition-colors shadow-sm"
          >
            Đặt Bàn Ngay - Nhận Ưu Đãi 20%
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const scrollY = ref(0)

const features = [
  {
    icon: 'fa-bowl-food',
    title: 'Nguyên Liệu Tươi Ngon',
    description: 'Chúng tôi chọn lọc kỹ càng từng nguyên liệu từ các nguồn uy tín, đảm bảo chất lượng và độ tươi ngon tốt nhất.'
  },
  {
    icon: 'fa-utensils',
    title: 'Đầu Bếp Chuyên Nghiệp',
    description: 'Đội ngũ đầu bếp giàu kinh nghiệm, tâm huyết với nghề, mang đến những món ăn đậm đà hương vị truyền thống.'
  },
  {
    icon: 'fa-sparkles',
    title: 'Không Gian Sang Trọng',
    description: 'Thiết kế hiện đại, thoáng mát với âm nhạc nhẹ nhàng, tạo không gian lý tưởng cho mọi bữa tiệc.'
  }
]

const popularDishes = [
  { icon: 'fa-bowl-food', name: 'Lẩu Thái Chua Cay', description: 'Hương vị đặc trưng Thái Lan', price: 299000 },
  { icon: 'fa-drumstick-bite', name: 'Lẩu Bò Mỹ', description: 'Bò nhập khẩu cao cấp', price: 399000 },
  { icon: 'fa-fish', name: 'Lẩu Hải Sản', description: 'Hải sản tươi sống', price: 449000 },
  { icon: 'fa-seedling', name: 'Lẩu Nấm Chay', description: 'Dinh dưỡng cho người ăn chay', price: 249000 }
]

const stats = [
  { value: 10000, label: 'Khách Hàng' },
  { value: 50, label: 'Món Ăn' },
  { value: 15, label: 'Chi Nhánh' },
  { value: 100, label: 'Nhân Viên' }
]

const testimonials = [
  {
    name: 'Nguyễn Văn A',
    role: 'Khách Hàng Thân Thiết',
    comment: 'Nhà hàng có không gian đẹp, món ăn ngon và phục vụ chu đáo. Gia đình tôi rất hài lòng và sẽ quay lại nhiều lần nữa!'
  },
  {
    name: 'Trần Thị B',
    role: 'Food Blogger',
    comment: 'Lẩu ở đây thật sự là tuyệt vời! Nước lẩu đậm đà, nguyên liệu tươi ngon. Tôi đã giới thiệu cho rất nhiều bạn bè.'
  },
  {
    name: 'Lê Văn C',
    role: 'Doanh Nhân',
    comment: 'Địa điểm lý tưởng cho các buổi gặp mặt đối tác. Không gian sang trọng, riêng tư và dịch vụ chuyên nghiệp.'
  }
]

function handleScroll() {
  scrollY.value = window.scrollY
  
  // Animate elements on scroll
  const elements = document.querySelectorAll('.scroll-animate')
  elements.forEach(el => {
    const rect = el.getBoundingClientRect()
    if (rect.top < window.innerHeight * 0.8) {
      el.classList.add('animate-fade-in-up')
    }
  })

  // Animate counters
  const counters = document.querySelectorAll('.counter')
  counters.forEach(counter => {
    const rect = counter.getBoundingClientRect()
    if (rect.top < window.innerHeight * 0.8 && !counter.classList.contains('counted')) {
      counter.classList.add('counted')
      animateCounter(counter)
    }
  })
}

function animateCounter(element) {
  const target = parseInt(element.dataset.target)
  const duration = 2000
  const step = target / (duration / 16)
  let current = 0

  const timer = setInterval(() => {
    current += step
    if (current >= target) {
      element.textContent = target.toLocaleString()
      clearInterval(timer)
    } else {
      element.textContent = Math.floor(current).toLocaleString()
    }
  }, 16)
}

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll() // Initial check
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
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

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 0.8s ease-out forwards;
}

.animate-slide-up {
  animation: slide-up 1s ease-out forwards;
}

.animation-delay-200 {
  animation-delay: 0.2s;
  opacity: 0;
}

.animation-delay-400 {
  animation-delay: 0.4s;
  opacity: 0;
}

.scroll-animate {
  opacity: 0;
}
</style>
