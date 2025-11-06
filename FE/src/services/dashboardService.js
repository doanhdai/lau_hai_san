import api from './api'

export const dashboardService = {
  async getStats() {
    const response = await api.get('/dashboard/stats')
    return response.data
  },

  async getRevenue(startDate, endDate) {
    const response = await api.get(`/dashboard/revenue?startDate=${startDate}&endDate=${endDate}`)
    return response.data
  },

  async getBestSellingDishes(limit = 10) {
    const response = await api.get(`/dashboard/best-selling-dishes?limit=${limit}`)
    return response.data
  },

  async getMonthlyRevenue(year) {
    const response = await api.get(`/dashboard/monthly-revenue?year=${year}`)
    return response.data
  }
}
