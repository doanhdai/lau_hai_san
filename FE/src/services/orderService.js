import api from './api'

export const orderService = {
  async getAll() {
    const response = await api.get('/orders')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/orders/${id}`)
    return response.data
  },

  async getByDate(startDate, endDate) {
    const response = await api.get(`/orders/by-date?startDate=${startDate}&endDate=${endDate}`)
    return response.data
  },

  async create(order) {
    const response = await api.post('/orders', order)
    return response.data
  },

  async update(id, order) {
    const response = await api.put(`/orders/${id}`, order)
    return response.data
  },

  async updateStatus(id, status) {
    const response = await api.put(`/orders/${id}/status?status=${status}`)
    return response.data
  },

  async addItem(id, item) {
    const response = await api.post(`/orders/${id}/items`, item)
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/orders/${id}`)
    return response.data
  },

  async createCounterOrder(order) {
    const response = await api.post('/orders/counter', order)
    return response.data
  },

  async getPending() {
    const response = await api.get('/orders/pending')
    return response.data
  },

  async getInvoiceHtml(orderId) {
    const response = await api.get(`/invoices/${orderId}/html`)
    return response.data
  },

  async downloadInvoice(orderId) {
    const response = await api.get(`/invoices/${orderId}/pdf`, {
      responseType: 'blob'
    })
    return response.data
  }
}
