<template>
  <div class="space-y-3 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold text-slate-900">Cài đặt Sơ đồ Bàn</h1>
      <div class="flex items-center gap-2">
        <button 
          @click="showCreateModal = true" 
          class="bg-orange-500 hover:bg-orange-600 text-white px-3 py-1.5 rounded-lg text-sm font-medium flex items-center gap-1.5 transition-colors"
        >
          <i class="fas fa-plus text-xs"></i>
          <span>Thêm Bàn</span>
        </button>
        <button 
          @click="toggleEditMode" 
          class="bg-white hover:bg-gray-50 border border-gray-300 text-slate-700 px-3 py-1.5 rounded-lg text-sm font-medium flex items-center gap-1.5 transition-colors"
          :class="{ 'bg-blue-50 border-blue-300 text-blue-700': editMode }"
        >
          <i class="fas fa-edit text-xs"></i>
          <i class="fas fa-times text-xs"></i>
          <span>Chỉnh sửa Vị trí</span>
        </button>
      </div>
    </div>

    <!-- Filter and Summary Section -->
    <div class="space-y-2">
      <!-- Filters -->
      <!-- <div class="bg-white border border-gray-200 rounded-lg p-2">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-2">
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Lọc theo khu vực:</label>
            <select v-model="filterArea" class="w-full px-2 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent text-xs">
              <option value="">Tất cả khu vực</option>
              <option value="VIP">Phòng VIP</option>
              <option value="NORMAL">Bàn thường</option>
              <option value="OUTDOOR">Ngoài trời</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Đặt bàn online:</label>
            <select v-model="filterOnline" class="w-full px-2 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent text-xs">
              <option value="">Tất cả</option>
              <option value="true">Cho phép</option>
              <option value="false">Không cho phép</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Tìm bàn:</label>
            <div class="flex gap-1.5">
              <input 
                v-model="searchTable" 
                type="text" 
                placeholder="Nhập số bàn..." 
                class="flex-1 px-2 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent text-xs"
              />
              <button 
                @click="loadTables" 
                class="bg-purple-600 hover:bg-purple-700 text-white px-3 py-1.5 rounded-lg text-xs font-medium transition-colors"
              >
                Tìm
              </button>
            </div>
          </div>
          <div class="flex items-end">
            <button 
              @click="resetFilters" 
              class="w-full bg-gray-100 hover:bg-gray-200 text-slate-700 px-2 py-1.5 rounded-lg text-xs font-medium flex items-center justify-center gap-1.5 transition-colors"
            >
              <i class="fas fa-sync-alt text-xs"></i>
              <span>Làm mới</span>
            </button>
          </div>
        </div>
      </div> -->

      <!-- Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-5 gap-2">
        <div class="bg-white border border-gray-200 rounded-lg p-2">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 bg-slate-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-chair text-slate-600 text-sm"></i>
          </div>
          <div>
              <p class="text-xs text-slate-500 mb-0.5">Tổng bàn</p>
              <p class="text-lg font-bold text-slate-900">{{ tables.length }}</p>
          </div>
        </div>
      </div>
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-2">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-wifi text-blue-600 text-sm"></i>
          </div>
          <div>
              <p class="text-xs text-blue-600 mb-0.5">Cho phép đặt online</p>
              <p class="text-lg font-bold text-slate-900">{{ onlineReservationCount }}</p>
          </div>
        </div>
      </div>
        <div class="bg-green-50 border border-green-200 rounded-lg p-2">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-check-circle text-green-600 text-sm"></i>
          </div>
          <div>
              <p class="text-xs text-green-600 mb-0.5">Trống</p>
              <p class="text-lg font-bold text-slate-900">{{ availableCount }}</p>
          </div>
        </div>
      </div>
        <div class="bg-red-50 border border-red-200 rounded-lg p-2">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 bg-red-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-users text-red-600 text-sm"></i>
          </div>
          <div>
              <p class="text-xs text-red-600 mb-0.5">Có khách</p>
              <p class="text-lg font-bold text-slate-900">{{ occupiedCount }}</p>
          </div>
        </div>
      </div>
        <div class="bg-amber-50 border border-amber-200 rounded-lg p-2">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 bg-amber-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-clock text-amber-600 text-sm"></i>
          </div>
          <div>
              <p class="text-xs text-amber-600 mb-0.5">Đã đặt</p>
              <p class="text-lg font-bold text-slate-900">{{ reservedCount }}</p>
          </div>
        </div>
      </div>
    </div>

      <!-- Legend -->
      <div class="bg-white border border-gray-200 rounded-lg p-2">
        <p class="text-xs font-semibold text-slate-700 mb-1.5">Chú thích:</p>
        <div class="grid grid-cols-2 md:grid-cols-5 gap-2">
          <div class="flex items-center gap-1.5">
            <div class="w-4 h-4 bg-green-500 rounded border border-green-600"></div>
            <span class="text-xs text-slate-700">Trống</span>
          </div>
          <div class="flex items-center gap-1.5">
            <div class="w-4 h-4 bg-red-500 rounded border border-red-600"></div>
            <span class="text-xs text-slate-700">Có khách</span>
          </div>
          <div class="flex items-center gap-1.5">
            <div class="w-4 h-4 bg-blue-500 rounded border border-blue-600"></div>
            <span class="text-xs text-slate-700">Đã đặt</span>
          </div>
          <div class="flex items-center gap-1.5">
            <div class="w-4 h-4 bg-yellow-500 rounded border border-yellow-600"></div>
            <span class="text-xs text-slate-700">Đang dọn</span>
          </div>
          <div class="flex items-center gap-1.5">
            <i class="fas fa-wifi text-blue-600 text-xs"></i>
            <span class="text-xs text-slate-700">Đặt Online</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content: Map and Table List -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-3">
      <!-- Visual Table Layout Area (Left - 2 columns) -->
      <div class="lg:col-span-2">
        <div class="bg-white border border-gray-200 rounded-lg p-3">
          <!-- Edit Mode Instruction -->
          <div v-if="editMode" class="mb-2 p-2 bg-blue-50 border border-blue-200 rounded-lg">
            <p class="text-xs text-blue-700">
              <i class="fas fa-info-circle mr-1.5 text-xs"></i>
              Chế độ chỉnh sửa đang bật. Kéo thả để sắp xếp vị trí các bàn.
            </p>
          </div>

        <div
          ref="mapContainer"
            class="relative bg-white rounded-lg border-2 border-dashed border-gray-300"
            style="height: calc(100vh - 400px); min-height: 500px; background-image: repeating-linear-gradient(0deg, transparent, transparent 49px, #e2e8f0 49px, #e2e8f0 50px), repeating-linear-gradient(90deg, transparent, transparent 49px, #e2e8f0 49px, #e2e8f0 50px); background-size: 50px 50px;"
            @mousemove="onMouseMove"
            @mouseup="onMouseUp"
            @mouseleave="onMouseUp"
          >
          <!-- Tables -->
          <div
              v-for="table in filteredTables"
            :key="table.id"
            :style="{
              position: 'absolute',
              left: table.positionX + 'px',
              top: table.positionY + 'px',
                  transform: isDragging && draggedTable?.id === table.id ? 'scale(1.05)' : 'scale(1)',
                  zIndex: isDragging && draggedTable?.id === table.id ? 1000 : (selectedTable?.id === table.id ? 100 : 1),
                  transition: isDragging && draggedTable?.id === table.id ? 'none' : 'all 0.2s',
                  cursor: editMode ? 'move' : 'pointer'
            }"
            :class="[
                  'table-block',
                  isDragging && draggedTable?.id === table.id ? 'opacity-80 shadow-2xl' : 'hover:shadow-lg',
                  selectedTable?.id === table.id ? 'ring-2 ring-blue-500 ring-offset-2' : ''
                ]"
                @mousedown="editMode ? onMouseDown(table, $event) : selectTable(table)"
                @click.stop="!editMode && selectTable(table)"
              >
                <!-- Table Block (Rectangular) -->
                <div 
                  class="relative rounded-lg shadow-md border-2 min-w-[100px] min-h-[80px] flex flex-col items-center justify-center p-3 pointer-events-none"
                  :class="getTableBlockClass(table.status)"
                >
                  <!-- Table Label (Top) -->
                  <div class="absolute -top-3 left-1/2 transform -translate-x-1/2 whitespace-nowrap">
                    <span class="bg-white px-2 py-0.5 rounded border-2 font-bold text-sm inline-block" :class="getTableLabelClass(table.status)">
                      {{ table.tableNumber }}
                    </span>
              </div>

                  <!-- Capacity (Center) -->
                  <div class="text-center mt-2">
                    <p class="text-xs font-semibold" :class="getTableTextClass(table.status)">
                      ({{ table.capacity }} chỗ)
                    </p>
              </div>

                  <!-- Online reservation icon (Top Right) -->
                  <div v-if="table.allowOnlineReservation" class="absolute top-1 right-1">
                    <i class="fas fa-wifi text-blue-600 text-xs bg-white rounded-full p-1 shadow-sm"></i>
              </div>

                  <!-- Edit icon when selected -->
                  <div v-if="selectedTable?.id === table.id && editMode" class="absolute bottom-1 right-1">
                    <i class="fas fa-pencil-alt text-blue-600 text-xs"></i>
              </div>
            </div>
          </div>

          <!-- Empty state -->
            <div v-if="filteredTables.length === 0" class="absolute inset-0 flex items-center justify-center">
            <div class="text-center text-gray-400">
                <i class="fas fa-chair text-6xl mb-3 opacity-50"></i>
              <p class="text-lg font-medium">Chưa có bàn nào</p>
                <p class="text-sm">Thêm bàn từ nút "Thêm Bàn"</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Table List Section (Right - 1 column) -->
      <div class="lg:col-span-1">
        <div class="bg-white border border-gray-200 rounded-lg">
          <div class="p-4 border-b border-gray-200">
            <h3 class="text-lg font-bold text-slate-900">Danh sách Bàn</h3>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">
                    <div class="flex items-center gap-1">
                      BÀN
                      <i class="fas fa-sort text-xs text-gray-400"></i>
                    </div>
                  </th>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">GHẾ</th>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">TRẠNG THÁI</th>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">ĐẶT ONLINE</th>
                  <th class="px-4 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">THAO TÁC</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr 
                  v-for="table in filteredTables" 
                  :key="table.id"
                  class="hover:bg-gray-50 transition-colors"
                  :class="{ 'bg-blue-50': selectedTable?.id === table.id }"
                  @click="selectTable(table)"
                >
                  <td class="px-4 py-3 whitespace-nowrap">
                    <div class="font-semibold text-slate-900">{{ table.tableNumber }}</div>
                  </td>
                  <td class="px-4 py-3 whitespace-nowrap text-sm text-slate-600">
                    {{ table.capacity }}
                  </td>
                  <td class="px-4 py-3 whitespace-nowrap">
                    <span class="px-2 py-1 text-xs font-medium rounded-full" :class="getStatusBadgeClass(table.status)">
                      {{ getStatusText(table.status) }}
                    </span>
                  </td>
                  <td class="px-4 py-3 whitespace-nowrap">
                    <span v-if="table.allowOnlineReservation" class="text-green-600 text-sm">
                      <i class="fas fa-check-circle"></i> Cho phép
                    </span>
                    <span v-else class="text-gray-500 text-sm">
                      Không cho phép
                    </span>
                  </td>
                  <td class="px-4 py-3 whitespace-nowrap">
                    <button 
                      @click.stop="editTable(table)"
                      class="text-blue-600 hover:text-blue-800 text-sm font-medium"
                    >
                      Sửa
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="filteredTables.length === 0" class="p-8 text-center text-gray-400">
              <i class="fas fa-chair text-4xl mb-2 opacity-50"></i>
              <p class="text-sm">Không có bàn nào</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table Detail Modal -->
    <Teleport to="body">
      <div
        v-if="selectedTable"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]"
        @click.self="selectedTable = null"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-md p-6 animate-slide-up">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-2xl font-bold text-slate-900">Bàn {{ selectedTable.tableNumber }}</h3>
            <button @click="selectedTable = null" class="text-gray-400 hover:text-gray-600">
              <i class="fas fa-times"></i>
            </button>
          </div>

          <div class="space-y-4">
            <!-- Status -->
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">Trạng thái</label>
              <select v-model="selectedTable.status" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent" @change="updateTableStatus">
                <option value="AVAILABLE">Trống</option>
                <option value="OCCUPIED">Có khách</option>
                <option value="RESERVED">Đã đặt</option>
                <option value="MAINTENANCE">Đang dọn</option>
              </select>
            </div>

            <!-- Info -->
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gray-50 rounded-lg p-3">
                <p class="text-xs text-gray-600 mb-1">Sức chứa</p>
                <div class="flex items-center gap-1 text-lg font-bold text-slate-900">
                  <i class="fas fa-users"></i>
                  <span>{{ selectedTable.capacity }} người</span>
                </div>
              </div>
              <div class="bg-gray-50 rounded-lg p-3">
                <p class="text-xs text-gray-600 mb-1">Vị trí</p>
                <div class="text-lg font-bold text-slate-900">
                  <span>{{ selectedTable.location || 'Chưa rõ' }}</span>
                </div>
              </div>
            </div>

            <!-- Online Reservation -->
            <div>
              <label class="flex items-center gap-2">
                <input 
                  type="checkbox" 
                  v-model="selectedTable.allowOnlineReservation"
                  @change="updateTableOnlineReservation"
                  class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <span class="text-sm text-slate-700">Cho phép đặt bàn online</span>
              </label>
            </div>

            <!-- Notes -->
            <div v-if="selectedTable.notes" class="bg-amber-50 border-l-4 border-amber-400 p-3">
              <p class="text-sm text-amber-700">
                <strong>Ghi chú:</strong> {{ selectedTable.notes }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Create Table Modal -->
    <Teleport to="body">
      <div
        v-if="showCreateModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]"
        @click.self="showCreateModal = false"
      >
        <div class="bg-white rounded-lg shadow-2xl w-full max-w-md p-6 animate-slide-up">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-2xl font-bold text-slate-900">Thêm Bàn Mới</h3>
            <button @click="showCreateModal = false" class="text-gray-400 hover:text-gray-600">
              <i class="fas fa-times"></i>
            </button>
          </div>

          <form @submit.prevent="createTable" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">
                Số bàn <span class="text-red-500">*</span>
              </label>
              <input
                v-model="newTable.tableNumber"
                type="text"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent"
                placeholder="T01"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">
                Sức chứa <span class="text-red-500">*</span>
              </label>
              <input
                v-model.number="newTable.capacity"
                type="number"
                required
                min="1"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent"
                placeholder="4"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">Loại bàn</label>
              <select v-model="newTable.tableType" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent">
                <option value="NORMAL">Bàn thường</option>
                <option value="VIP">Phòng VIP</option>
                <option value="OUTDOOR">Ngoài trời</option>
              </select>
            </div>

            <div>
              <label class="flex items-center gap-2">
                <input 
                  type="checkbox" 
                  v-model="newTable.allowOnlineReservation"
                  class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <span class="text-sm text-slate-700">Cho phép đặt bàn online</span>
              </label>
            </div>

            <div class="flex gap-3 pt-4">
              <button
                type="button"
                @click="showCreateModal = false"
                class="flex-1 bg-gray-100 hover:bg-gray-200 text-slate-700 px-4 py-2 rounded-lg font-medium transition-colors"
              >
                Hủy
              </button>
              <button
                type="submit"
                :disabled="creating"
                class="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="!creating">Thêm Bàn</span>
                <span v-else>Đang thêm...</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { tableService } from '@/services/tableService'
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()

const tables = ref([])
const selectedTable = ref(null)
const isDragging = ref(false)
const draggedTable = ref(null)
const dragOffset = ref({ x: 0, y: 0 })
const editMode = ref(false)
const showCreateModal = ref(false)
const creating = ref(false)
const mapContainer = ref(null)

// Filters
const filterArea = ref('')
const filterOnline = ref('')
const searchTable = ref('')

// New table form
const newTable = ref({
  tableNumber: '',
  capacity: 4,
  tableType: 'NORMAL',
  allowOnlineReservation: true,
  status: 'AVAILABLE'
})

// Computed
const availableCount = computed(() => 
  tables.value.filter(t => t.status === 'AVAILABLE').length
)

const occupiedCount = computed(() => 
  tables.value.filter(t => t.status === 'OCCUPIED').length
)

const reservedCount = computed(() => 
  tables.value.filter(t => t.status === 'RESERVED').length
)

const onlineReservationCount = computed(() => 
  tables.value.filter(t => t.allowOnlineReservation === true).length
)

const filteredTables = computed(() => {
  let result = [...tables.value]

  // Filter by area/table type
  if (filterArea.value) {
    result = result.filter(t => {
      if (filterArea.value === 'VIP') return t.tableType === 'VIP'
      if (filterArea.value === 'NORMAL') return t.tableType === 'NORMAL'
      if (filterArea.value === 'OUTDOOR') return t.tableType === 'OUTDOOR'
      return true
    })
  }

  // Filter by online reservation
  if (filterOnline.value !== '') {
    const allowOnline = filterOnline.value === 'true'
    result = result.filter(t => t.allowOnlineReservation === allowOnline)
  }

  // Search by table number
  if (searchTable.value) {
    const search = searchTable.value.toLowerCase()
    result = result.filter(t => 
      t.tableNumber?.toLowerCase().includes(search)
    )
  }

  return result
})

// Group tables by area/prefix (A, B, VIP, etc.)
const groupedTables = computed(() => {
  const groups = {}
  
  filteredTables.value.forEach(table => {
    // Extract prefix from table number (e.g., "A1" -> "A", "VIP1" -> "VIP", "T01" -> "T")
    const match = table.tableNumber?.match(/^([A-Z]+)/i)
    const prefix = match ? match[1].toUpperCase() : 'OTHER'
    
    if (!groups[prefix]) {
      groups[prefix] = []
    }
    groups[prefix].push(table)
  })
  
  return groups
})

// Methods
const loadTables = async () => {
  try {
    const response = await tableService.getAll()
    
    // Handle different response structures
    let tablesData = []
    if (response.success && response.data) {
      tablesData = Array.isArray(response.data) ? response.data : []
    } else if (Array.isArray(response.data)) {
      tablesData = response.data
    } else if (response.data?.data) {
      tablesData = response.data.data
    }
    
    console.log('Loaded tables:', tablesData.length)
    
    // Initialize positions in a grid layout if not set
    tables.value = tablesData.map((table, index) => {
      let positionX = table.positionX
      let positionY = table.positionY
      
      // If position not set (null or undefined), arrange in a grid
      if (positionX === null || positionX === undefined || positionY === null || positionY === undefined) {
        const cols = 5 // 5 columns
        const row = Math.floor(index / cols)
        const col = index % cols
        const spacing = 140 // spacing between tables (increased from 120)
        const startX = 60
        const startY = 60
        
        positionX = startX + (col * spacing)
        positionY = startY + (row * spacing)
        
        console.log(`Table ${table.tableNumber}: initialized position (${positionX}, ${positionY})`)
      } else {
        console.log(`Table ${table.tableNumber}: using saved position (${positionX}, ${positionY})`)
      }
      
      return {
      ...table,
        positionX,
        positionY,
        allowOnlineReservation: table.allowOnlineReservation ?? false
      }
    })
    
    console.log('Tables after positioning:', tables.value.length)
  } catch (error) {
    console.error('Error loading tables:', error)
    notificationStore.error('Không thể tải danh sách bàn: ' + (error.response?.data?.message || error.message))
  }
}

const resetFilters = () => {
  filterArea.value = ''
  filterOnline.value = ''
  searchTable.value = ''
  loadTables()
}

const toggleEditMode = () => {
  editMode.value = !editMode.value
  // Reset dragging when toggling edit mode
  if (!editMode.value) {
    isDragging.value = false
    draggedTable.value = null
  }
}

// Mouse-based drag and drop
const dragStartPos = ref({ x: 0, y: 0 })

const onMouseDown = (table, event) => {
  if (!editMode.value) return
  
  event.preventDefault()
  event.stopPropagation()
  
  isDragging.value = true
  draggedTable.value = table
  
  // Get the table block element
  const tableElement = event.currentTarget
  const rect = tableElement.getBoundingClientRect()
  
  // Get map container
  const container = mapContainer.value
  const mapRect = container?.getBoundingClientRect() || { left: 0, top: 0 }
  
  // Calculate offset from mouse position to element's top-left corner
  dragOffset.value = {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
  
  // Store initial mouse position relative to map container
  dragStartPos.value = {
    x: event.clientX - mapRect.left,
    y: event.clientY - mapRect.top
  }
}

const onMouseMove = (event) => {
  if (!isDragging.value || !draggedTable.value || !editMode.value) return
  
  event.preventDefault()
  
  const container = mapContainer.value
  if (!container) return
  
  const rect = container.getBoundingClientRect()
  
  // Calculate new position
  let newX = event.clientX - rect.left - dragOffset.value.x
  let newY = event.clientY - rect.top - dragOffset.value.y

  // Constrain within bounds
  newX = Math.max(10, Math.min(newX, rect.width - 110))
  newY = Math.max(10, Math.min(newY, rect.height - 90))

  // Snap to grid (50px)
  newX = Math.round(newX / 50) * 50
  newY = Math.round(newY / 50) * 50

  // Update table position in real-time
  const tableIndex = tables.value.findIndex(t => t.id === draggedTable.value.id)
  if (tableIndex !== -1) {
    tables.value[tableIndex].positionX = newX
    tables.value[tableIndex].positionY = newY
  }
}

const onMouseUp = (event) => {
  if (!isDragging.value || !draggedTable.value) return
  
  // Save position when mouse is released
  const table = draggedTable.value
  saveTablePosition(table)
  
  // Reset dragging state
  isDragging.value = false
  draggedTable.value = null
  dragStartPos.value = { x: 0, y: 0 }
}

// Debounce timer for saving positions
let saveTimer = null
const pendingSaves = new Set()

const saveTablePosition = async (table) => {
  if (!table || !table.id) return
  
  // Add to pending saves
  pendingSaves.add(table.id)
  
  // Clear existing timer
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  
  // Debounce: wait 500ms before saving to avoid too many API calls
  saveTimer = setTimeout(async () => {
    try {
      // Use new position API with query params
      const response = await tableService.updatePosition(
        table.id, 
        table.positionX, 
        table.positionY
      )
      
      if (response.success !== false) {
        // Only show notification if there are multiple pending saves
        if (pendingSaves.size > 1) {
          notificationStore.success(`Đã lưu vị trí ${pendingSaves.size} bàn`)
        } else {
          notificationStore.success(`Đã lưu vị trí bàn ${table.tableNumber}`)
        }
      } else {
        notificationStore.error(response.message || 'Không thể lưu vị trí bàn')
      }
      
      pendingSaves.clear()
    } catch (error) {
      console.error('Error saving table position:', error)
      const errorMessage = error.response?.data?.message || error.message || 'Không thể lưu vị trí bàn'
      notificationStore.error(errorMessage)
      pendingSaves.clear()
    }
  }, 500)
}

const selectTable = (table) => {
  selectedTable.value = { ...table }
}

const editTable = (table) => {
  selectedTable.value = { ...table }
}

const updateTableStatus = async () => {
  try {
    await tableService.updateStatus(selectedTable.value.id, selectedTable.value.status)
    const tableIndex = tables.value.findIndex(t => t.id === selectedTable.value.id)
    if (tableIndex !== -1) {
      tables.value[tableIndex].status = selectedTable.value.status
    }
    notificationStore.success('Cập nhật trạng thái thành công')
  } catch (error) {
    notificationStore.error('Cập nhật trạng thái thất bại')
  }
}

const updateTableOnlineReservation = async () => {
  try {
    await tableService.update(selectedTable.value.id, {
      allowOnlineReservation: selectedTable.value.allowOnlineReservation
    })
    const tableIndex = tables.value.findIndex(t => t.id === selectedTable.value.id)
    if (tableIndex !== -1) {
      tables.value[tableIndex].allowOnlineReservation = selectedTable.value.allowOnlineReservation
    }
    notificationStore.success('Cập nhật thành công')
  } catch (error) {
    notificationStore.error('Cập nhật thất bại')
  }
}

const createTable = async () => {
  creating.value = true
  try {
    const response = await tableService.create(newTable.value)
    if (response.success) {
      notificationStore.success('Thêm bàn thành công')
      showCreateModal.value = false
      newTable.value = {
        tableNumber: '',
        capacity: 4,
        tableType: 'NORMAL',
        allowOnlineReservation: true,
        status: 'AVAILABLE'
      }
  loadTables()
    } else {
      notificationStore.error(response.message || 'Thêm bàn thất bại')
    }
  } catch (error) {
    notificationStore.error(error.response?.data?.message || 'Thêm bàn thất bại')
  } finally {
    creating.value = false
  }
}

// Styling helpers
const getTableClass = (status) => {
  const classes = {
    AVAILABLE: 'bg-green-50 border-2 border-green-300',
    OCCUPIED: 'bg-red-50 border-2 border-red-300',
    RESERVED: 'bg-blue-50 border-2 border-blue-300',
    MAINTENANCE: 'bg-yellow-50 border-2 border-yellow-300'
  }
  return classes[status] || 'bg-gray-50 border-2 border-gray-300'
}

const getTableBgClass = (status) => {
  const classes = {
    AVAILABLE: 'bg-green-500',
    OCCUPIED: 'bg-red-500',
    RESERVED: 'bg-blue-500',
    MAINTENANCE: 'bg-yellow-500'
  }
  return classes[status] || 'bg-gray-500'
}

const getTableIcon = (status) => {
  const icons = {
    AVAILABLE: 'fa-check-circle',
    OCCUPIED: 'fa-users',
    RESERVED: 'fa-clock',
    MAINTENANCE: 'fa-broom'
  }
  return icons[status] || 'fa-chair'
}

const getTableIconColor = (status) => {
  const colors = {
    AVAILABLE: 'text-white',
    OCCUPIED: 'text-white',
    RESERVED: 'text-white',
    MAINTENANCE: 'text-white'
  }
  return colors[status] || 'text-white'
}

const getBorderClass = (status) => {
  const classes = {
    AVAILABLE: 'border-green-500',
    OCCUPIED: 'border-red-500',
    RESERVED: 'border-blue-500',
    MAINTENANCE: 'border-yellow-500'
  }
  return classes[status] || 'border-gray-500'
}

const getTextClass = (status) => {
  const classes = {
    AVAILABLE: 'text-green-700',
    OCCUPIED: 'text-red-700',
    RESERVED: 'text-blue-700',
    MAINTENANCE: 'text-yellow-700'
  }
  return classes[status] || 'text-gray-700'
}

const getStatusBadgeClass = (status) => {
  const classes = {
    AVAILABLE: 'bg-green-100 text-green-700 border border-green-200',
    OCCUPIED: 'bg-red-100 text-red-700 border border-red-200',
    RESERVED: 'bg-blue-100 text-blue-700 border border-blue-200',
    MAINTENANCE: 'bg-yellow-100 text-yellow-700 border border-yellow-200'
  }
  return classes[status] || 'bg-gray-100 text-gray-700 border border-gray-200'
}

const getStatusText = (status) => {
  const texts = {
    AVAILABLE: 'Trống',
    OCCUPIED: 'Có khách',
    RESERVED: 'Đã đặt',
    MAINTENANCE: 'Đang dọn'
  }
  return texts[status] || status
}

// New styling methods for table blocks
const getTableBlockClass = (status) => {
  const classes = {
    AVAILABLE: 'bg-green-500 border-green-600',
    OCCUPIED: 'bg-red-500 border-red-600',
    RESERVED: 'bg-blue-500 border-blue-600',
    MAINTENANCE: 'bg-yellow-500 border-yellow-600'
  }
  return classes[status] || 'bg-gray-500 border-gray-600'
}

const getTableLabelClass = (status) => {
  const classes = {
    AVAILABLE: 'border-green-600 text-green-700',
    OCCUPIED: 'border-red-600 text-red-700',
    RESERVED: 'border-blue-600 text-blue-700',
    MAINTENANCE: 'border-yellow-600 text-yellow-700'
  }
  return classes[status] || 'border-gray-600 text-gray-700'
}

const getTableTextClass = (status) => {
  const classes = {
    AVAILABLE: 'text-white',
    OCCUPIED: 'text-white',
    RESERVED: 'text-white',
    MAINTENANCE: 'text-white'
  }
  return classes[status] || 'text-white'
}

onMounted(() => {
  loadTables().then(() => {
    const savedLayout = localStorage.getItem('tableLayout')
    if (savedLayout) {
      const layout = JSON.parse(savedLayout)
      tables.value.forEach(table => {
        const saved = layout.find(l => l.id === table.id)
        if (saved) {
          table.positionX = saved.positionX
          table.positionY = saved.positionY
        }
      })
    }
  })
})
</script>

<style scoped>
.table-item {
  width: 120px;
  padding: 12px;
  border-radius: 12px;
  user-select: none;
}

.table-block {
  user-select: none;
  touch-action: none;
  pointer-events: auto;
}

.table-block:active {
  cursor: grabbing !important;
}

/* Prevent table label from wrapping */
.table-block > div > div:first-child {
  white-space: nowrap;
  overflow: visible;
}

.table-block > div > div:first-child > span {
  display: inline-block;
  white-space: nowrap;
  max-width: none;
}

.animate-slide-up {
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
