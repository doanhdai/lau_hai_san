<template>
  <div>
    <!-- Hero -->
    <section class="relative h-80 flex items-center justify-center bg-slate-900">
      <div class="absolute inset-0 bg-black/40"></div>
      <div class="relative z-10 text-center text-white px-4 max-w-4xl mx-auto">
        <h1 class="text-4xl md:text-5xl font-bold mb-4 tracking-tight">Thực Đơn</h1>
        <p class="text-lg md:text-xl text-slate-200">Khám phá hương vị đặc sắc với hơn 50 món lẩu và toppings cao cấp</p>
      </div>
    </section>

    <!-- Loading State -->
    <div v-if="loading" class="py-20 text-center">
      <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
      <p class="mt-4 text-slate-600 text-sm">Đang tải thực đơn...</p>
    </div>
    
    <div v-if="!loading">
      <!-- Categories Filter -->
      <section class="py-6 bg-white border-b border-gray-200 sticky top-20 z-40">
        <div class="container mx-auto px-4">
          <div class="flex gap-3 overflow-x-auto pb-2">
            <button
              v-for="cat in categories"
              :key="cat.id"
              @click="handleCategoryClick(cat.id)"
              class="px-5 py-2.5 rounded-lg font-medium whitespace-nowrap transition-all duration-200 flex items-center gap-2 border"
              :class="isCategorySelected(cat.id) 
                ? 'bg-slate-900 text-white border-slate-900 shadow-sm' 
                : 'bg-white text-slate-700 border-gray-300 hover:border-slate-900 hover:text-slate-900'"
            >
              <i :class="['fas', cat.icon]" class="text-sm"></i>
              <span>{{ cat.name }}</span>
              <span v-if="cat.count !== undefined" class="text-xs opacity-70">({{ cat.count }})</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Menu Grid -->
      <section class="py-16 bg-white">
        <div class="container mx-auto px-4">
          <!-- Loading Dishes State -->
          <div v-if="loadingDishes" class="py-20 text-center">
            <div class="inline-block w-10 h-10 border-2 border-slate-900 border-t-transparent rounded-full animate-spin"></div>
            <p class="mt-4 text-slate-600 text-sm">Đang tải món ăn...</p>
          </div>
          
          <template v-else>
            <!-- Dishes Grid -->
            <div v-if="filteredDishes.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              <div
                v-for="dish in filteredDishes"
                :key="dish.id"
                class="group"
              >
                <div class="bg-white rounded-lg overflow-hidden border border-gray-200 hover:border-slate-900 hover:shadow-lg transition-all duration-200">
                  <!-- Image -->
                  <div class="aspect-square bg-gray-100 overflow-hidden relative">
                    <img 
                      v-if="dish.imageUrl && dish.imageUrl.trim() !== '' && dish.imageUrl.startsWith('http')"
                      :src="dish.imageUrl" 
                      :alt="dish.name" 
                      class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                      @error="handleImageError"
                    >
                    <div v-else class="w-full h-full flex items-center justify-center bg-gray-50">
                      <i :class="['fas', getDishIcon(dish.name)]" class="text-5xl opacity-40"></i>
                    </div>
                    <div v-if="dish.isPromotion" class="absolute top-3 right-3 bg-red-600 text-white text-xs font-semibold px-2.5 py-1 rounded-md shadow-sm z-10">
                      KHUYẾN MÃI
                    </div>
                  </div>
                  
                  <!-- Info -->
                  <div class="p-4">
                    <h3 class="text-lg font-semibold text-slate-900 mb-1.5 line-clamp-1">
                      {{ dish.name }}
                    </h3>
                    <p class="text-sm text-slate-600 mb-3 line-clamp-2 leading-relaxed">{{ dish.description }}</p>
                    
                    <!-- Rating -->
                    <div class="flex items-center gap-1 mb-3">
                      <i v-for="i in 5" :key="i" :class="['text-amber-500 text-xs', i <= dish.rating ? 'fas fa-star' : 'far fa-star']"></i>
                      <span class="text-xs text-slate-500 ml-1">({{ dish.reviews }})</span>
                    </div>

                    <div class="flex items-center justify-between pt-3 border-t border-gray-100">
                      <div>
                        <span class="text-xl font-bold text-slate-900">{{ formatPrice(dish.price) }}</span>
                        <div v-if="dish.promotionName" class="text-xs text-red-600 font-medium mt-0.5">
                          {{ dish.promotionName }}
                        </div>
                      </div>
                      <button @click="handleOrder(dish)" class="bg-slate-900 hover:bg-slate-800 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
                        Đặt món
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Empty State -->
            <div v-else class="text-center py-20 w-full">
              <i class="fas fa-utensils text-5xl mb-4 opacity-30 text-slate-400"></i>
              <p class="text-lg text-slate-500 font-medium">Không tìm thấy món ăn</p>
              <p class="text-sm text-slate-400 mt-2">Vui lòng thử chọn danh mục khác</p>
            </div>
          </template>
        </div>
      </section>

      <!-- Download Menu CTA -->
      <section class="py-16 bg-slate-50 border-t border-gray-200">
        <div class="container mx-auto px-4 text-center max-w-2xl">
          <h2 class="text-3xl font-bold text-slate-900 mb-3">Tải Menu Đầy Đủ</h2>
          <p class="text-slate-600 mb-6">Xem thực đơn chi tiết với hình ảnh và giá cả</p>
          <button class="bg-slate-900 hover:bg-slate-800 text-white px-8 py-3 rounded-lg font-semibold text-base transition-colors shadow-sm flex items-center gap-2 mx-auto">
            <i class="fas fa-download"></i>
            <span>Tải Menu PDF</span>
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { dishService } from '@/services/dishService'
import { categoryService } from '@/services/categoryService'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const notification = useNotificationStore()

const selectedCategory = ref('all')
const loading = ref(false)
const loadingDishes = ref(false)
const categories = ref([])
const dishes = ref([])
// Cache dishes by category để tránh call lại
const dishesCache = ref(new Map())
const allDishes = ref([])

// Dishes hiển thị - được set từ API calls
const filteredDishes = computed(() => {
  if (!dishes.value) return []
  return Array.isArray(dishes.value) ? dishes.value : []
})

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function getCategoryIcon(categoryName) {
  if (!categoryName) return 'fa-utensils'
  
  const name = categoryName.toLowerCase().trim()
  const icons = {
    'lẩu': 'fa-bowl-food',
    'rau & nấm': 'fa-leaf',
    'rau củ': 'fa-leaf',
    'hải sản': 'fa-fish',
    'hải sảnnn': 'fa-fish', // Fix typo in API
    'thịt': 'fa-drumstick-bite',
    'nấm': 'fa-seedling',
    'đồ ăn kèm': 'fa-bowl-rice',
    'nước uống': 'fa-glass-water',
    'tráng miệng': 'fa-ice-cream'
  }
  
  // Check exact match first
  if (icons[name]) {
    return icons[name]
  }
  
  // Check partial match
  for (const [key, icon] of Object.entries(icons)) {
    if (name.includes(key) || key.includes(name)) {
      return icon
    }
  }
  
  return 'fa-utensils'
}

function getDishIcon(dishName) {
  if (!dishName) return 'fa-utensils'
  const name = dishName.toLowerCase()
  if (name.includes('lẩu thai') || name.includes('thái')) return 'fa-bowl-food'
  if (name.includes('tôm')) return 'fa-shrimp'
  if (name.includes('bò')) return 'fa-drumstick-bite'
  if (name.includes('hải sản')) return 'fa-fish'
  if (name.includes('nấm')) return 'fa-seedling'
  if (name.includes('rau')) return 'fa-leaf'
  if (name.includes('mì') || name.includes('bún')) return 'fa-bowl-rice'
  if (name.includes('nước') || name.includes('trà') || name.includes('bia')) return 'fa-glass-water'
  if (name.includes('cá')) return 'fa-fish'
  if (name.includes('cua')) return 'fa-crab'
  if (name.includes('gà')) return 'fa-drumstick-bite'
  if (name.includes('heo') || name.includes('sườn')) return 'fa-drumstick-bite'
  return 'fa-utensils'
}

function handleOrder(dish) {
  notification.info(`Vui lòng đặt bàn để order món: ${dish.name}`)
  router.push('/reservation')
}

function handleImageError(event) {
  // Hide broken image and show icon placeholder
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent && !parent.querySelector('.icon-placeholder')) {
    const iconPlaceholder = document.createElement('div')
    iconPlaceholder.className = 'icon-placeholder w-full h-full flex items-center justify-center bg-gray-50'
    const dishName = event.target.alt || 'Dish'
    const iconClass = getDishIcon(dishName)
    iconPlaceholder.innerHTML = `<i class="fas ${iconClass} text-5xl opacity-40"></i>`
    parent.appendChild(iconPlaceholder)
  }
}

async function handleCategoryClick(categoryId) {
  selectedCategory.value = categoryId
  dishes.value = []
  await nextTick()
  await loadDishesByCategory(categoryId)
}

function isCategorySelected(categoryId) {
  // So sánh trực tiếp trước (cho trường hợp 'all')
  if (selectedCategory.value === categoryId) {
    return true
  }
  
  // Nếu một trong hai là 'all', return false (vì đã check ở trên)
  if (selectedCategory.value === 'all' || categoryId === 'all') {
    return false
  }
  
  // Convert cả hai về number để so sánh
  const selectedNum = Number(selectedCategory.value)
  const catNum = Number(categoryId)
  
  // So sánh số
  if (!isNaN(selectedNum) && !isNaN(catNum)) {
    return selectedNum === catNum
  }
  
  return false
}

// Function để map dish data từ API
function mapDishData(dish) {
  const categoryId = dish.categoryId !== undefined && dish.categoryId !== null ? Number(dish.categoryId) : null
  
  // Xử lý imageUrl: nếu là empty string thì set thành null
  let imageUrl = dish.imageUrl
  if (!imageUrl || imageUrl === '' || imageUrl.trim() === '') {
    imageUrl = null
  }
  
  return {
    id: dish.id,
    name: dish.name,
    description: dish.description || 'Món ăn ngon',
    price: Number(dish.price) || 0,
    categoryId: categoryId,
    categoryName: dish.categoryName || dish.category?.name || 'Không xác định',
    imageUrl: imageUrl,
    isPromotion: dish.isPromotion || false,
    promotionName: dish.promotionName || null,
    rating: 4.5,
    reviews: Math.floor(Math.random() * 100) + 20
  }
}

// Function để parse API response
function parseDishesResponse(response) {
  let dishesData = []
  if (response && response.success && Array.isArray(response.data)) {
    dishesData = response.data
  } else if (Array.isArray(response)) {
    dishesData = response
  } else if (response && Array.isArray(response.data)) {
    dishesData = response.data
  }
  
  // Filter và map dishes: chỉ lấy active và AVAILABLE
  return dishesData
    .filter(dish => {
      const isActive = dish.active === true || dish.active === 'true' || dish.active === 1
      const isAvailable = dish.status === 'AVAILABLE' || dish.status === 'available'
      return isActive && isAvailable
    })
    .map(mapDishData)
    .filter(dish => dish.categoryId !== null && dish.categoryId !== undefined)
}

// Load dishes theo category
async function loadDishesByCategory(categoryId) {
  loadingDishes.value = true
  
  try {
    let resultDishes = []
    
    if (categoryId === 'all') {
      if (allDishes.value.length > 0) {
        resultDishes = Array.from(allDishes.value)
      } else {
        const dishesRes = await dishService.getAvailable()
        resultDishes = parseDishesResponse(dishesRes)
        allDishes.value = Array.from(resultDishes)
      }
    } else {
      const categoryIdNum = Number(categoryId)
      if (dishesCache.value.has(categoryIdNum)) {
        resultDishes = Array.from(dishesCache.value.get(categoryIdNum))
      } else {
        const dishesRes = await dishService.getByCategory(categoryIdNum)
        resultDishes = parseDishesResponse(dishesRes)
        dishesCache.value.set(categoryIdNum, Array.from(resultDishes))
      }
    }
    
    // Force reactivity bằng cách tạo array mới hoàn toàn
    dishes.value = []
    await nextTick()
    dishes.value = resultDishes
    
  } catch (error) {
    console.error('Error loading dishes by category:', error)
    notification.error('Không thể tải món ăn')
    dishes.value = []
  } finally {
    loadingDishes.value = false
  }
}

// Load categories từ API
async function loadCategories() {
  try {
    const categoriesRes = await categoryService.getActive()
    
    // Handle API response structure
    let categoriesData = []
    if (categoriesRes && categoriesRes.success && Array.isArray(categoriesRes.data)) {
      categoriesData = categoriesRes.data
    } else if (Array.isArray(categoriesRes)) {
      categoriesData = categoriesRes
    } else if (categoriesRes && Array.isArray(categoriesRes.data)) {
      categoriesData = categoriesRes.data
    }
    
    // Filter chỉ lấy categories active
    const activeCategories = categoriesData
      .filter(cat => cat.active === true || cat.active === 1 || cat.active === 'true')
      .map(cat => ({
        id: Number(cat.id),
        name: cat.name || cat.categoryName,
        icon: getCategoryIcon(cat.name || cat.categoryName),
        count: 0 // Sẽ update sau khi load dishes
      }))
      .sort((a, b) => a.id - b.id)
    
    categories.value = [
      { id: 'all', name: 'Tất cả', icon: '🍽️', count: 0 },
      ...activeCategories
    ]
    
  } catch (error) {
    console.error('Error loading categories:', error)
    notification.error('Không thể tải danh mục')
  }
}

onMounted(async () => {
  try {
    loading.value = true
    
    // Load categories trước
    await loadCategories()
    
    // Load dishes cho category mặc định (all)
    await loadDishesByCategory('all')
    
    // Update count cho categories (từ allDishes)
    if (allDishes.value.length > 0) {
      const categoryCountMap = new Map()
      allDishes.value.forEach(dish => {
        const catId = Number(dish.categoryId)
        categoryCountMap.set(catId, (categoryCountMap.get(catId) || 0) + 1)
      })
      
      categories.value.forEach(cat => {
        if (cat.id === 'all') {
          cat.count = allDishes.value.length
        } else {
          cat.count = categoryCountMap.get(cat.id) || 0
        }
      })
    }
    
  } catch (error) {
    console.error('Error loading menu:', error)
    notification.error('Không thể tải thực đơn')
  } finally {
    loading.value = false
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
  opacity: 1;
}

.animate-in {
  animation: fade-in-up 0.6s ease-out forwards;
}
</style>
