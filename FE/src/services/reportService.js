import api from './api'

export const reportService = {
  async getRevenueSummary(startDate, endDate) {
    const response = await api.get(`/reports/revenue?startDate=${startDate}&endDate=${endDate}`)
    return response.data
  },

  async getTopDishes(startDate, endDate, limit = 10) {
    const response = await api.get(`/reports/top-dishes?startDate=${startDate}&endDate=${endDate}&limit=${limit}`)
    return response.data
  },

  async getCustomerStats() {
    const response = await api.get('/reports/customer-stats')
    return response.data
  },

  async getOrderStats(startDate, endDate) {
    const response = await api.get(`/reports/order-stats?startDate=${startDate}&endDate=${endDate}`)
    return response.data
  },

  async getTableUsage() {
    const response = await api.get('/reports/table-usage')
    return response.data
  },

  async getRevenueReport(startDate, endDate) {
    const response = await api.get(`/reports/revenue`, {
      params: { startDate, endDate }
    })
    return response.data
  },

  async getSalesReport(startDate, endDate) {
    const response = await api.get(`/reports/sales`, {
      params: { startDate, endDate }
    })
    return response.data
  }
}
