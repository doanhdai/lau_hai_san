import api from './api'

export const tableService = {
  async getAll() {
    const response = await api.get('/tables')
    return response.data
  },

  async getAvailable() {
    const response = await api.get('/tables/available')
    return response.data
  },

  async getSuitable(numberOfGuests) {
    const response = await api.get(`/tables/suitable?numberOfGuests=${numberOfGuests}`)
    return response.data
  },

  async getAvailableByTime(reservationTime) {
    const response = await api.get(`/tables/filter?reservationTime=${reservationTime}`)
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/tables/${id}`)
    return response.data
  },

  async create(table) {
    const response = await api.post('/tables', table)
    return response.data
  },

  async update(id, table) {
    const response = await api.put(`/tables/${id}`, table)
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/tables/${id}`)
    return response.data
  },

  async updateStatus(id, status) {
    const response = await api.put(`/tables/${id}/status?status=${status}`)
    return response.data
  }
}
