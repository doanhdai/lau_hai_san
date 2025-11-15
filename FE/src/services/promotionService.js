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
    // Map frontend format to backend format
    // Format date as LocalDateTime: YYYY-MM-DDTHH:mm:ss (without timezone)
    const formatDateForBackend = (dateString) => {
      if (!dateString) return null
      // If already in YYYY-MM-DD format, add time 00:00:00
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
        return `${dateString}T00:00:00`
      }
      // If it's a full datetime string, extract date and time
      return dateString
    }
    
    const request = {
      name: promotion.promotionName || promotion.name,
      description: promotion.description,
      startDate: formatDateForBackend(promotion.startDate),
      endDate: formatDateForBackend(promotion.endDate),
      discountPercent: promotion.discountType === 'PERCENTAGE' ? promotion.discountValue : null,
      discountAmount: promotion.discountType === 'FIXED' ? promotion.discountValue : null,
      minOrderValue: promotion.minOrderValue || 0,
      maxDiscount: promotion.maxDiscount || 0
    }
    
    const response = await api.post('/promotions', request)
    return response.data
  },

  async update(id, promotion) {
    // Map frontend format to backend format
    // Format date as LocalDateTime: YYYY-MM-DDTHH:mm:ss (without timezone)
    const formatDateForBackend = (dateString) => {
      if (!dateString) return null
      // If already in YYYY-MM-DD format, add time 00:00:00
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
        return `${dateString}T00:00:00`
      }
      // If it's a full datetime string, extract date and time
      return dateString
    }
    
    const request = {
      name: promotion.promotionName || promotion.name,
      description: promotion.description,
      startDate: formatDateForBackend(promotion.startDate),
      endDate: formatDateForBackend(promotion.endDate),
      discountPercent: promotion.discountType === 'PERCENTAGE' ? promotion.discountValue : null,
      discountAmount: promotion.discountType === 'FIXED' ? promotion.discountValue : null,
      minOrderValue: promotion.minOrderValue || 0,
      maxDiscount: promotion.maxDiscount || 0
    }
    
    const response = await api.put(`/promotions/${id}`, request)
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
