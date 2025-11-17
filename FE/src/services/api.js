import axios from 'axios'
import router from '@/router'

const API_URL = 'http://localhost:8080/api'

const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Public API client - không yêu cầu authentication, không redirect khi 401
export const publicApiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor để thêm token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // Nếu data là FormData, xóa Content-Type để browser tự động set với boundary
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor để xử lý lỗi
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

// Public API client - chỉ có request interceptor để xử lý FormData, KHÔNG có response interceptor redirect
publicApiClient.interceptors.request.use(
  (config) => {
    // Nếu data là FormData, xóa Content-Type để browser tự động set với boundary
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Public API client KHÔNG có response interceptor - cho phép 401 mà không redirect
// Điều này cho phép gọi API public mà không cần đăng nhập

export default apiClient
