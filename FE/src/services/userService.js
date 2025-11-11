import api from './api'

export const userService = {
  // Get all users with pagination and filters
  async getAll(params = {}) {
    try {
      const response = await api.get('/users', { params })
      // API returns: { success, message, data: { content, number, totalPages, ... } }
      // Return the data directly if response has success and data
      if (response.data && response.data.success && response.data.data) {
        return {
          success: true,
          data: response.data.data
        }
      }
      // Fallback: return response as is
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error fetching users:', error)
      throw error
    }
  },

  // Get user by ID
  async getById(id) {
    try {
      const response = await api.get(`/users/${id}`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error fetching user:', error)
      throw error
    }
  },

  // Create new user
  async create(userData) {
    try {
      const response = await api.post('/users', userData)
      return {
        success: true,
        data: response.data,
        message: 'Tạo người dùng thành công'
      }
    } catch (error) {
      console.error('Error creating user:', error)
      throw error
    }
  },

  // Update user
  async update(id, userData) {
    try {
      const response = await api.put(`/users/${id}`, userData)
      return {
        success: true,
        data: response.data,
        message: 'Cập nhật người dùng thành công'
      }
    } catch (error) {
      console.error('Error updating user:', error)
      throw error
    }
  },

  // Delete user
  async delete(id) {
    try {
      await api.delete(`/users/${id}`)
      return {
        success: true,
        message: 'Xóa người dùng thành công'
      }
    } catch (error) {
      console.error('Error deleting user:', error)
      throw error
    }
  },

  // Toggle user status (active/inactive)
  async toggleStatus(id) {
    try {
      const response = await api.patch(`/users/${id}/toggle-status`)
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
      console.error('Error toggling user status:', error)
      throw error
    }
  },

  // Change user password
  async changePassword(id, passwordData) {
    try {
      const response = await api.patch(`/users/${id}/change-password`, passwordData)
      return {
        success: true,
        message: 'Đổi mật khẩu thành công'
      }
    } catch (error) {
      console.error('Error changing password:', error)
      throw error
    }
  },

  // Get user statistics
  async getStats() {
    try {
      const response = await api.get('/users/stats')
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error fetching user stats:', error)
      throw error
    }
  },

  // Search users
  async search(query) {
    try {
      const response = await api.get('/users/search', {
        params: { q: query }
      })
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error searching users:', error)
      throw error
    }
  },

  // Get users by role
  async getByRole(role) {
    try {
      const response = await api.get('/users/by-role', {
        params: { role }
      })
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      console.error('Error fetching users by role:', error)
      throw error
    }
  },

  // Bulk operations
  async bulkDelete(userIds) {
    try {
      const response = await api.post('/users/bulk-delete', { ids: userIds })
      return {
        success: true,
        message: 'Xóa người dùng thành công'
      }
    } catch (error) {
      console.error('Error bulk deleting users:', error)
      throw error
    }
  },

  async bulkToggleStatus(userIds, active) {
    try {
      const response = await api.post('/users/bulk-toggle-status', { 
        ids: userIds, 
        active 
      })
      return {
        success: true,
        message: 'Cập nhật trạng thái thành công'
      }
    } catch (error) {
      console.error('Error bulk toggling user status:', error)
      throw error
    }
  }
}
