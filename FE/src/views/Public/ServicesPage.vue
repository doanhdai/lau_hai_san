<template>
  <div>
    <!-- Hero -->
    <section class="relative h-96 flex items-center justify-center bg-gradient-to-br from-sky-600 to-blue-700">
      <div class="absolute inset-0 bg-black/20"></div>
      <div class="relative z-10 text-center text-white px-4">
        <h1 class="text-5xl md:text-6xl font-bold mb-4 animate-slide-up">Dịch Vụ Của Chúng Tôi</h1>
        <p class="text-xl animate-slide-up animation-delay-200">Phục vụ mọi nhu cầu của quý khách</p>
      </div>
    </section>

    <!-- Services Grid -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-12">
          <div 
            v-for="(service, index) in services" 
            :key="index"
            class="scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="bg-gradient-to-br from-white to-gray-50 p-8 rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2">
              <div class="flex items-start gap-6">
                <div class="w-20 h-20 bg-gradient-to-br from-sky-500 to-sky-600 rounded-2xl flex items-center justify-center text-4xl flex-shrink-0 transform hover:rotate-12 transition-transform">
                  {{ service.icon }}
                </div>
                <div class="flex-1">
                  <h3 class="text-2xl font-bold text-gray-900 mb-3">{{ service.title }}</h3>
                  <p class="text-gray-600 leading-relaxed mb-4">{{ service.description }}</p>
                  <ul class="space-y-2">
                    <li v-for="(feature, idx) in service.features" :key="idx" class="flex items-center gap-2 text-gray-700">
                      <span class="text-sky-600">✓</span>
                      <span>{{ feature }}</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Special Packages -->
    <section class="py-20 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16 scroll-animate">
          <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">Gói Dịch Vụ Đặc Biệt</h2>
          <div class="w-20 h-1 bg-sky-600 mx-auto"></div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div 
            v-for="(pkg, index) in packages" 
            :key="index"
            class="scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:scale-105" :class="pkg.featured ? 'border-4 border-sky-600' : ''">
              <div v-if="pkg.featured" class="bg-sky-600 text-white text-sm font-bold px-4 py-1 rounded-full inline-block mb-4">
                PHỔ BIẾN NHẤT
              </div>
              <h3 class="text-3xl font-bold text-gray-900 mb-2">{{ pkg.name }}</h3>
              <div class="text-4xl font-bold text-sky-600 mb-6">
                {{ formatPrice(pkg.price) }}
                <span class="text-lg text-gray-500 font-normal">/người</span>
              </div>
              <ul class="space-y-3 mb-8">
                <li v-for="(item, idx) in pkg.includes" :key="idx" class="flex items-start gap-2">
                  <span class="text-sky-600 mt-1">✓</span>
                  <span class="text-gray-700">{{ item }}</span>
                </li>
              </ul>
              <button class="w-full bg-sky-600 hover:bg-sky-700 text-white font-bold py-3 rounded-lg transition">
                Chọn Gói Này
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Process -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16 scroll-animate">
          <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">Quy Trình Phục Vụ</h2>
          <p class="text-xl text-gray-600">4 bước đơn giản để có bữa tiệc hoàn hảo</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div 
            v-for="(step, index) in processSteps" 
            :key="index"
            class="text-center scroll-animate"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="relative mb-6">
              <div class="w-24 h-24 bg-gradient-to-br from-sky-500 to-sky-600 rounded-full flex items-center justify-center text-white text-3xl font-bold mx-auto transform hover:scale-110 transition-transform">
                {{ index + 1 }}
              </div>
              <div v-if="index < 3" class="hidden md:block absolute top-1/2 left-full w-full h-1 bg-gradient-to-r from-sky-600 to-sky-300 -translate-y-1/2"></div>
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-2">{{ step.title }}</h3>
            <p class="text-gray-600">{{ step.description }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

const services = [
  {
    icon: '🍽️',
    title: 'Tiệc Gia Đình & Bạn Bè',
    description: 'Không gian ấm cúng, riêng tư dành cho các buổi họp mặt gia đình, bạn bè. Phục vụ chu đáo, tận tình.',
    features: [
      'Phòng riêng VIP từ 10-20 người',
      'Thực đơn đa dạng, linh hoạt',
      'Trang trí theo yêu cầu',
      'Ưu đãi cho nhóm đông'
    ]
  },
  {
    icon: '💼',
    title: 'Tiệc Công Ty & Sự Kiện',
    description: 'Tổ chức các sự kiện công ty, hội nghị, team building với quy mô lớn, chuyên nghiệp.',
    features: [
      'Sức chứa đến 200 khách',
      'Hỗ trợ âm thanh, ánh sáng',
      'Menu buffet đa dạng',
      'Dịch vụ MC, trang trí'
    ]
  },
  {
    icon: '🎂',
    title: 'Tiệc Sinh Nhật & Kỷ Niệm',
    description: 'Tổ chức tiệc sinh nhật, kỷ niệm đáng nhớ với không gian lãng mạn, ấm áp.',
    features: [
      'Trang trí theo chủ đề',
      'Bánh kem sinh nhật miễn phí',
      'Chương trình giải trí',
      'Chụp ảnh miễn phí'
    ]
  },
  {
    icon: '🚗',
    title: 'Giao Hàng Tận Nơi',
    description: 'Phục vụ giao hàng nhanh chóng, đảm bảo chất lượng món ăn như tại nhà hàng.',
    features: [
      'Giao trong 30-45 phút',
      'Miễn phí ship nội thành',
      'Đóng gói chuyên nghiệp',
      'Thanh toán linh hoạt'
    ]
  }
]

const packages = [
  {
    name: 'Gói Cơ Bản',
    price: 199000,
    featured: false,
    includes: [
      'Lẩu 1 vị (chọn từ thực đơn)',
      '5 món thịt/hải sản cơ bản',
      'Rau củ & nấm tươi',
      'Nước uống không giới hạn',
      'Tráng miệng theo mùa'
    ]
  },
  {
    name: 'Gói Cao Cấp',
    price: 299000,
    featured: true,
    includes: [
      'Lẩu 2 vị tùy chọn',
      '8 món thịt/hải sản cao cấp',
      'Rau củ & nấm đặc biệt',
      'Nước uống & bia không giới hạn',
      'Tráng miệng đặc biệt',
      'Giảm 10% cho lần sau'
    ]
  },
  {
    name: 'Gói VIP',
    price: 499000,
    featured: false,
    includes: [
      'Lẩu 3 vị cao cấp',
      '12 món thượng hạng',
      'Hải sản tươi sống',
      'Rượu vang & bia cao cấp',
      'Tráng miệng VIP',
      'Phòng riêng sang trọng',
      'Giảm 20% cho lần sau'
    ]
  }
]

const processSteps = [
  {
    title: 'Đặt Bàn',
    description: 'Đặt bàn online hoặc gọi điện, chọn thời gian phù hợp'
  },
  {
    title: 'Xác Nhận',
    description: 'Nhận xác nhận qua SMS/Email trong 5 phút'
  },
  {
    title: 'Đến Nhà Hàng',
    description: 'Đến đúng giờ, được hướng dẫn vào bàn ngay'
  },
  {
    title: 'Thưởng Thức',
    description: 'Tận hưởng bữa ăn ngon với dịch vụ tuyệt vời'
  }
]

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

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

.scroll-animate {
  opacity: 0;
}
</style>
