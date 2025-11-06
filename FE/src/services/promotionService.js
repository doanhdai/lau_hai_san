import api from './api'

export const promotionService = {
  async getAll() {
    const response = await api.get('/promotions')
    return response.data
  },

  async getActive() {
    const response = await api.get('/promotions/active')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/promotions/${id}`)
    return response.data
  },

  async create(promotion) {
    const response = await api.post('/promotions', promotion)
    return response.data
  },

  async update(id, promotion) {
    const response = await api.put(`/promotions/${id}`, promotion)
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/promotions/${id}`)
    return response.data
  },

  async activate(id) {
    const response = await api.put(`/promotions/${id}/activate`)
    return response.data
  },

  async deactivate(id) {
    const response = await api.put(`/promotions/${id}/deactivate`)
    return response.data
  }
}
