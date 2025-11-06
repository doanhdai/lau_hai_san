<template>
  <div>
    <!-- Hero -->
    <section class="relative h-96 flex items-center justify-center bg-gradient-to-br from-sky-600 to-blue-700">
      <div class="relative z-10 text-center text-white px-4">
        <h1 class="text-5xl md:text-6xl font-bold mb-4 animate-slide-up">Thực Đơn Đa Dạng</h1>
        <p class="text-xl animate-slide-up animation-delay-200">Hơn 50 món lẩu và toppings cao cấp</p>
      </div>
    </section>

    <!-- Loading State -->
    <div v-if="loading" class="py-20 text-center">
      <div class="inline-block w-16 h-16 border-4 border-sky-600 border-t-transparent rounded-full animate-spin"></div>
      <p class="mt-4 text-gray-600">Đang tải thực đơn...</p>
    </div>

    <div v-else>
      <!-- Categories Filter -->
      <section class="py-8 bg-white sticky top-20 z-40 shadow-md">
        <div class="container mx-auto px-4">
          <div class="flex gap-4 overflow-x-auto pb-2">
            <button
              v-for="cat in categories"
              :key="cat.id"
              @click="selectedCategory = cat.id"
              class="px-6 py-3 rounded-full font-semibold whitespace-nowrap transition-all"
              :class="selectedCategory === cat.id ? 'bg-sky-600 text-white shadow-lg' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
            >
              {{ cat.icon }} {{ cat.name }}
            </button>
          </div>
        </div>
      </section>

      <!-- Menu Grid -->
      <section class="py-20 bg-gray-50">
        <div class="container mx-auto px-4">
          <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
            <div
              v-for="dish in filteredDishes"
              :key="dish.id"
              class="group scroll-animate"
            >
              <div class="bg-white rounded-2xl overflow-hidden shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2">
                <!-- Image -->
                <div class="aspect-square bg-gradient-to-br from-sky-100 to-gray-100 overflow-hidden relative">
                  <div class="w-full h-full flex items-center justify-center text-7xl group-hover:scale-110 transition-transform duration-300">
                    {{ dish.icon }}
                  </div>
                  <div v-if="dish.isNew" class="absolute top-3 right-3 bg-sky-600 text-white text-xs font-bold px-3 py-1 rounded-full">
                    MỚI
                  </div>
                  <div v-if="dish.isPopular" class="absolute top-3 left-3 bg-yellow-500 text-white text-xs font-bold px-3 py-1 rounded-full">
                    PHỔ BIẾN
                  </div>
                </div>
                
                <!-- Info -->
                <div class="p-5">
                  <h3 class="text-xl font-bold text-gray-900 mb-2 group-hover:text-sky-600 transition">
                    {{ dish.name }}
                  </h3>
                  <p class="text-gray-600 text-sm mb-3 line-clamp-2">{{ dish.description }}</p>
                  
                  <!-- Rating -->
                  <div class="flex items-center gap-1 mb-3">
                    <span v-for="i in 5" :key="i" class="text-yellow-500">
                      {{ i <= dish.rating ? '★' : '☆' }}
                    </span>
                    <span class="text-sm text-gray-500 ml-1">({{ dish.reviews }})</span>
                  </div>

                  <div class="flex items-center justify-between">
                    <span class="text-2xl font-bold text-sky-600">{{ formatPrice(dish.price) }}</span>
                    <button @click="handleOrder(dish)" class="bg-sky-600 hover:bg-sky-700 text-white px-4 py-2 rounded-lg font-semibold transition transform hover:scale-105">
                      Đặt món
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="filteredDishes.length === 0" class="text-center py-20">
            <div class="text-6xl mb-4">🍲</div>
            <p class="text-xl text-gray-500">Không tìm thấy món ăn</p>
          </div>
        </div>
      </section>

      <!-- Download Menu CTA -->
      <section class="py-20 bg-gradient-to-br from-sky-600 to-blue-700 text-white">
        <div class="container mx-auto px-4 text-center">
          <h2 class="text-4xl font-bold mb-6">Tải Menu Đầy Đủ</h2>
          <p class="text-xl mb-8">Xem thực đọn chi tiết với hình ảnh và giá cả</p>
          <button class="bg-white text-sky-600 px-8 py-4 rounded-lg font-bold text-lg hover:bg-gray-100 shadow-xl transform hover:scale-105 transition-all">
            📥 Tải Menu PDF
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dishService } from '@/services/dishService'
import { categoryService } from '@/services/categoryService'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const notification = useNotificationStore()

const selectedCategory = ref('all')
const loading = ref(true)
const categories = ref([])
const dishes = ref([])

const filteredDishes = computed(() => {
  console.log('Filtering - selectedCategory:', selectedCategory.value)
  console.log('Total dishes:', dishes.value.length)
  
  if (selectedCategory.value === 'all') {
    console.log('Showing all dishes:', dishes.value.length)
    return dishes.value
  }
  
  const filtered = dishes.value.filter(dish => dish.categoryId === selectedCategory.value)
  console.log(`Filtered dishes for category ${selectedCategory.value}:`, filtered.length)
  console.log('Filtered dishes:', filtered.map(d => ({name: d.name, categoryId: d.categoryId})))
  
  return filtered
})

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function fixCategoryName(name) {
  const fixes = {
    'L?u': 'Lẩu',
    'H?i s?n': 'Hải sản',
    'Th?t': 'Thịt',
    'Rau c?': 'Rau & Nấm',
    'N?m': 'Nấm',
    'Nu?c u?ng': 'Nước uống',
    'Tráng mi?ng': 'Tráng miệng'
  }
  return fixes[name] || name
}

function getCategoryIcon(categoryName) {
  const icons = {
    'Lẩu': '🍲',
    'Rau & Nấm': '🥬',
    'Hải sản': '🦐',
    'Thịt': '🥩',
    'Nấm': '🍄',
    'Đồ ăn kèm': '🍜',
    'Nước uống': '🥤',
    'Tráng miệng': '🍰'
  }
  return icons[categoryName] || '🍽️'
}

function getDishIcon(dishName) {
  const name = dishName.toLowerCase()
  if (name.includes('lẩu thai') || name.includes('thái')) return '🥘'
  if (name.includes('tôm')) return '🦐'
  if (name.includes('bò')) return '🥩'
  if (name.includes('hải sản')) return '🦐'
  if (name.includes('nấm')) return '🍄'
  if (name.includes('rau')) return '🥬'
  if (name.includes('mì') || name.includes('bún')) return '🍜'
  if (name.includes('nước') || name.includes('trà') || name.includes('bia')) return '🥤'
  if (name.includes('cá')) return '🐟'
  if (name.includes('cua')) return '🦀'
  if (name.includes('gà')) return '🐔'
  if (name.includes('heo') || name.includes('sườn')) return '🐷'
  return '🍽️'
}

function handleOrder(dish) {
  notification.info(`Vui lòng đặt bàn để order món: ${dish.name}`)
  router.push('/reservation')
}

onMounted(async () => {
  try {
    loading.value = true
    
    // Load categories and dishes from API
    const [categoriesRes, dishesRes] = await Promise.all([
      categoryService.getAll(),
      dishService.getAvailable()
    ])

    if (categoriesRes.success && dishesRes.success) {
      console.log('Categories API response:', categoriesRes.data)
      console.log('Dishes API response:', dishesRes.data)
      
      // Map categories - Fix encoding issues
      categories.value = [
        { id: 'all', name: 'Tất cả', icon: '🍽️' },
        ...categoriesRes.data.map(cat => ({
          id: cat.id,
          name: fixCategoryName(cat.name), // Fix encoding
          icon: getCategoryIcon(fixCategoryName(cat.name))
        }))
      ]

      // Map dishes - Fix categoryId mapping
      dishes.value = dishesRes.data.map(dish => ({
        id: dish.id,
        name: dish.name,
        description: dish.description || 'Món ăn ngon',
        price: dish.price,
        categoryId: dish.category?.id || dish.categoryId, // Fix category mapping
        icon: getDishIcon(dish.name),
        rating: 4.5,
        reviews: Math.floor(Math.random() * 100) + 20,
        isNew: dish.isPromotion,
        isPopular: dish.isPromotion
      }))
      
      console.log('Mapped categories:', categories.value)
      console.log('Mapped dishes:', dishes.value)
    }
  } catch (error) {
    console.error('Error loading menu:', error)
    notification.error('Không thể tải thực đơn')
  } finally {
    loading.value = false
    
    // Setup scroll animation
    setTimeout(() => {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('animate-in')
          }
        })
      }, { threshold: 0.1 })
      
      document.querySelectorAll('.scroll-animate').forEach(el => observer.observe(el))
    }, 100)
  }
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

.animate-in {
  animation: fade-in-up 0.6s ease-out forwards;
}
</style>
