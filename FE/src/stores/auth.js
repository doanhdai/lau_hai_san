import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
// Remove router import to avoid circular dependency
// Router will handle redirects via navigation guards

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.roles?.includes('ROLE_ADMIN'))
  const isManager = computed(() => user.value?.roles?.includes('ROLE_MANAGER'))
  const isStaff = computed(() => user.value?.roles?.includes('ROLE_STAFF'))

  function initAuth() {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
    }
  }

  async function login(credentials) {
    try {
      loading.value = true
      error.value = null
      
      const response = await authService.login(credentials)
      
      if (response.success) {
        const { token: authToken, ...userData } = response.data
        token.value = authToken
        user.value = userData
        
        localStorage.setItem('token', authToken)
        localStorage.setItem('user', JSON.stringify(userData))
        
        // Return success - let login component handle redirect
        return { success: true, userData }
      }
      
      error.value = response.message || 'Đăng nhập thất bại'
      return false
    } catch (err) {
      error.value = err.response?.data?.message || 'Đã xảy ra lỗi'
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(userData) {
    try {
      loading.value = true
      error.value = null
      
      const response = await authService.register(userData)
      
      if (response.success) {
        return { success: true }
      }
      
      error.value = response.message || 'Đăng ký thất bại'
      return false
    } catch (err) {
      error.value = err.response?.data?.message || 'Đã xảy ra lỗi'
      return false
    } finally {
      loading.value = false
    }
  }

  function logout() {
    authService.logout()
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    // Component will handle redirect to login
  }

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    isAdmin,
    isManager,
    isStaff,
    initAuth,
    login,
    register,
    logout
  }
})
