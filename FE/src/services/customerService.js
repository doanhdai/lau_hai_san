import api from './api'

export const customerService = {
  async getAll() {
    const response = await api.get('/customers')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/customers/${id}`)
    return response.data
  },

  async getVip() {
    const response = await api.get('/customers/vip')
    return response.data
  },

  async create(customer) {
    const response = await api.post('/customers', customer)
    return response.data
  },

  async update(id, customer) {
    const response = await api.put(`/customers/${id}`, customer)
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/customers/${id}`)
    return response.data
  },

  async block(id) {
    const response = await api.put(`/customers/${id}/block`)
    return response.data
  },

  async unblock(id) {
    const response = await api.put(`/customers/${id}/unblock`)
    return response.data
  },
  
  async getReservations(customerId) {
  return await apiClient.get(`/customers/${customerId}/reservations`)
}
}
