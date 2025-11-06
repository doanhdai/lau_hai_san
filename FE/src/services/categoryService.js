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
  }
}
