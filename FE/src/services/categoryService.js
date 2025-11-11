import api from './api'

export const categoryService = {
  async getAll() {
    const response = await api.get('/categories')
    return response.data
  },

  async getActive() {
    const response = await api.get('/categories/active')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/categories/${id}`)
    return response.data
  },

  async create(category) {
    const response = await api.post('/categories', category)
    return response.data
  },

  async update(id, category) {
    const response = await api.put(`/categories/${id}`, category)
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/categories/${id}`)
    return response.data
  },

  async toggleStatus(id) {
    try {
      const response = await api.patch(`/categories/${id}/toggle-status`)
      // API may return: { success, message, data } or just data
      if (response.data && response.data.success && response.data.data) {
        return {
          success: true,
          data: response.data.data,
          message: response.data.message || 'Cập nhật trạng thái thành công'
        }
      }
      // Fallback: return response as is
      return {
        success: true,
        data: response.data,
        message: 'Cập nhật trạng thái thành công'
      }
    } catch (error) {
      console.error('Error toggling category status:', error)
      throw error
    }
  }
}
