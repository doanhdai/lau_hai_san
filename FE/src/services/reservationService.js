import api from './api'

export const reservationService = {
  async getAll() {
    const response = await api.get('/reservations')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/reservations/${id}`)
    return response.data
  },

  async getByDate(startDate, endDate) {
    const response = await api.get(`/reservations/by-date?startDate=${startDate}&endDate=${endDate}`)
    return response.data
  },

  async search(customerName) {
    const response = await api.get(`/reservations/search?customerName=${customerName}`)
    return response.data
  },

  async createPublic(reservation) {
    // Public endpoint - không cần authentication
    const response = await api.post('/reservations/public', reservation)
    return response.data
  },

  async create(reservation) {
    const response = await api.post('/reservations', reservation)
    return response.data
  },

  async update(id, reservation) {
    const response = await api.put(`/reservations/${id}`, reservation)
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/reservations/${id}`)
    return response.data
  },

  async confirm(id) {
    const response = await api.put(`/reservations/${id}/confirm`)
    return response.data
  },

  async cancel(id) {
    const response = await api.put(`/reservations/${id}/cancel`)
    return response.data
  }
}
