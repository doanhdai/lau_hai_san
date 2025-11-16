import api from './api'

export const paymentService = {
  async create(payment) {
    const response = await api.post('/payments', payment)
    return response.data
  },

  async getByOrderId(orderId) {
    const response = await api.get(`/payments/order/${orderId}`)
    return response.data
  },

  async getAll() {
    const response = await api.get('/payments')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/payments/${id}`)
    return response.data
  }
}

