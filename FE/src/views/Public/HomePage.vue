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
      <div class="absolute inset-0 bg-gradient-to-b from-black/70 via-black/50 to-black/70"></div>
      
      <!-- Hero Content -->
      <div class="relative z-10 text-center px-4 animate-fade-in-up">
        <h1 class="text-5xl md:text-7xl font-bold text-white mb-6 animate-slide-up">
          Trải Nghiệm <span class="text-sky-400">Lẩu</span> Đích Thực
        </h1>
        <p class="text-xl md:text-2xl text-white/90 mb-8 max-w-3xl mx-auto animate-slide-up animation-delay-200">
          Hương vị truyền thống kết hợp không gian hiện đại, mang đến bữa tiệc lẩu hoàn hảo cho gia đình và bạn bè
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center animate-slide-up animation-delay-400">
          <router-link 
            to="/reservation" 
            class="btn-primary px-8 py-4 text-lg shadow-2xl hover:shadow-sky-500/50 transform hover:scale-105 transition-all"
          >
            Đặt Bàn Ngay
          </router-link>
          <router-link 
            to="/menu" 
            class="bg-white text-gray-900 px-8 py-4 rounded-lg font-semibold text-lg hover:bg-gray-100 shadow-xl transform hover:scale-105 transition-all"
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
        <div class="text-center mb-16 scroll-animate">
          <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">Tại Sao Chọn Chúng Tôi?</h2>
          <div class="w-20 h-1 bg-sky-600 mx-auto"></div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div 
            v-for="(feature, index) in features" 
            :key="index"
            class="group scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="bg-gradient-to-br from-gray-50 to-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2">
              <div class="w-16 h-16 bg-gradient-to-br from-sky-500 to-blue-600 rounded-2xl flex items-center justify-center text-3xl mb-4 group-hover:scale-110 transition-transform">
                {{ feature.icon }}
              </div>
              <h3 class="text-2xl font-bold text-gray-900 mb-3">{{ feature.title }}</h3>
              <p class="text-gray-600 leading-relaxed">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Menu Preview Section -->
    <section class="py-20 bg-gray-900 text-white relative overflow-hidden">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-0 left-0 w-96 h-96 bg-sky-500 rounded-full filter blur-3xl"></div>
        <div class="absolute bottom-0 right-0 w-96 h-96 bg-blue-600 rounded-full filter blur-3xl"></div>
      </div>

      <div class="container mx-auto px-4 relative z-10">
        <div class="text-center mb-16 scroll-animate">
          <h2 class="text-4xl md:text-5xl font-bold mb-4">Thực Đơn Đặc Biệt</h2>
          <p class="text-xl text-gray-300">Khám phá các món lẩu độc đáo và hấp dẫn</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div 
            v-for="(dish, index) in popularDishes" 
            :key="index"
            class="group scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="bg-gray-800 rounded-2xl overflow-hidden hover:shadow-2xl hover:shadow-sky-500/20 transition-all duration-300 transform hover:-translate-y-2">
              <div class="aspect-square bg-gradient-to-br from-sky-900/20 to-gray-800 overflow-hidden">
                <div class="w-full h-full flex items-center justify-center text-6xl group-hover:scale-110 transition-transform duration-300">
                  {{ dish.icon }}
                </div>
              </div>
              <div class="p-5">
                <h3 class="text-xl font-bold mb-2">{{ dish.name }}</h3>
                <p class="text-gray-400 text-sm mb-3">{{ dish.description }}</p>
                <div class="flex items-center justify-between">
                  <span class="text-2xl font-bold text-sky-500">{{ formatPrice(dish.price) }}</span>
                  <button class="bg-sky-600 hover:bg-sky-700 px-4 py-2 rounded-lg font-semibold transition">
                    Đặt món
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="text-center mt-12 scroll-animate">
          <router-link 
            to="/menu" 
            class="inline-block bg-white text-gray-900 px-8 py-4 rounded-lg font-bold text-lg hover:bg-gray-100 shadow-xl transform hover:scale-105 transition-all"
          >
            Xem Tất Cả Thực Đơn
          </router-link>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="py-20 bg-gradient-to-br from-sky-600 to-blue-700 text-white">
      <div class="container mx-auto px-4">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div 
            v-for="(stat, index) in stats" 
            :key="index"
            class="text-center scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="text-5xl md:text-6xl font-bold mb-2 counter" :data-target="stat.value">0</div>
            <div class="text-xl text-sky-100">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Testimonials Section -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16 scroll-animate">
          <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">Khách Hàng Nói Gì?</h2>
          <div class="w-20 h-1 bg-sky-600 mx-auto"></div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div 
            v-for="(testimonial, index) in testimonials" 
            :key="index"
            class="scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="bg-gradient-to-br from-gray-50 to-white p-8 rounded-2xl shadow-lg hover:shadow-xl transition-all">
              <div class="flex items-center gap-1 text-yellow-500 mb-4">
                <span v-for="i in 5" :key="i" class="text-2xl">★</span>
              </div>
              <p class="text-gray-700 text-lg mb-6 italic leading-relaxed">"{{ testimonial.comment }}"</p>
              <div class="flex items-center gap-4">
                <div class="w-14 h-14 bg-gradient-to-br from-sky-500 to-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl">
                  {{ testimonial.name.charAt(0) }}
                </div>
                <div>
                  <div class="font-bold text-gray-900">{{ testimonial.name }}</div>
                  <div class="text-sm text-gray-500">{{ testimonial.role }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-20 bg-gray-900 text-white relative overflow-hidden">
      <div 
        class="absolute inset-0 bg-cover bg-center opacity-20"
        style="background-image: url('https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1920')"
      ></div>
      
      <div class="container mx-auto px-4 relative z-10">
        <div class="max-w-3xl mx-auto text-center scroll-animate">
          <h2 class="text-4xl md:text-5xl font-bold mb-6">Sẵn Sàng Trải Nghiệm?</h2>
          <p class="text-xl text-gray-300 mb-8">
            Đặt bàn ngay hôm nay và nhận ưu đãi đặc biệt cho khách hàng mới!
          </p>
          <router-link 
            to="/reservation" 
            class="inline-block bg-sky-600 hover:bg-sky-700 px-10 py-5 rounded-lg font-bold text-xl shadow-2xl transform hover:scale-105 transition-all"
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
    icon: '🍲',
    title: 'Nguyên Liệu Tươi Ngon',
    description: 'Chúng tôi chọn lọc kỹ càng từng nguyên liệu từ các nguồn uy tín, đảm bảo chất lượng và độ tươi ngon tốt nhất.'
  },
  {
    icon: '👨‍🍳',
    title: 'Đầu Bếp Chuyên Nghiệp',
    description: 'Đội ngũ đầu bếp giàu kinh nghiệm, tâm huyết với nghề, mang đến những món ăn đậm đà hương vị truyền thống.'
  },
  {
    icon: '✨',
    title: 'Không Gian Sang Trọng',
    description: 'Thiết kế hiện đại, thoáng mát với âm nhạc nhẹ nhàng, tạo không gian lý tưởng cho mọi bữa tiệc.'
  }
]

const popularDishes = [
  { icon: '🥘', name: 'Lẩu Thái Chua Cay', description: 'Hương vị đặc trưng Thái Lan', price: 299000 },
  { icon: '🍖', name: 'Lẩu Bò Mỹ', description: 'Bò nhập khẩu cao cấp', price: 399000 },
  { icon: '🦐', name: 'Lẩu Hải Sản', description: 'Hải sản tươi sống', price: 449000 },
  { icon: '🍄', name: 'Lẩu Nấm Chay', description: 'Dinh dưỡng cho người ăn chay', price: 249000 }
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

@keyframes scroll-down {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(8px);
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

.animate-scroll-down {
  animation: scroll-down 2s ease-in-out infinite;
}

.scroll-animate {
  opacity: 0;
}
</style>
