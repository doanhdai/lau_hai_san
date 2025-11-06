import api from './api'

export const feedbackService = {
  async getAll() {
    const response = await api.get('/feedbacks')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/feedbacks/${id}`)
    return response.data
  },

  async getByCustomer(customerId) {
    const response = await api.get(`/feedbacks/customer/${customerId}`)
    return response.data
  },

  async create(feedback) {
    const response = await api.post('/feedbacks', feedback)
    return response.data
  },

  async update(id, feedback) {
    const response = await api.put(`/feedbacks/${id}`, feedback)
    return response.data
  },

  async delete(id) {
    const response = await api.delete(`/feedbacks/${id}`)
    return response.data
  },

  async respond(id, response) {
    const resp = await api.put(`/feedbacks/${id}/respond`, { response })
    return resp.data
  }
}
