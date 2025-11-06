import api from './api'

export const roomService = {
  async getAll() {
    const response = await api.get('/rooms')
    return response.data
  },

  async getAvailable() {
    const response = await api.get('/rooms/available')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/rooms/${id}`)
    return response.data
  },

  async create(room) {
    const response = await api.post('/rooms', room)
    return response.data
  },

  async update(id, room) {
    const response = await api.put(`/rooms/${id}`, room)
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/rooms/${id}`)
    return response.data
  },

  async updateStatus(id, status) {
    const response = await api.put(`/rooms/${id}/status?status=${status}`)
    return response.data
  }
}
