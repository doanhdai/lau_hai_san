import api from './api'

export const dishService = {
  async getAll() {
    const response = await api.get('/dishes')
    return response.data
  },

  async getAvailable() {
    const response = await api.get('/dishes/available')
    return response.data
  },

  async getByCategory(categoryId) {
    const response = await api.get(`/dishes/category/${categoryId}`)
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/dishes/${id}`)
    return response.data
  },

  async search(keyword) {
    const response = await api.get(`/dishes/search?keyword=${keyword}`)
    return response.data
  },

  async create(dish) {
    const response = await api.post('/dishes', dish)
    return response.data
  },

  async createWithImage(formData) {
    const response = await api.post('/dishes/form', formData, {
      headers: {
        // KHÔNG set Content-Type header, browser sẽ tự động set với boundary
      }
    })
    return response.data
  },

  async update(id, dish) {
    const response = await api.put(`/dishes/${id}`, dish)
    return response.data
  },

  async updateWithImage(id, formData) {
    const response = await api.put(`/dishes/${id}/form`, formData, {
      headers: {
        // KHÔNG set Content-Type header, browser sẽ tự động set với boundary
      }
    })
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/dishes/${id}`)
    return response.data
  },

  async updateStatus(id, status) {
    const response = await api.put(`/dishes/${id}/status?status=${status}`)
    return response.data
  }
}
