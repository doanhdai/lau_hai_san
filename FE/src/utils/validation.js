
export function isValidEmail(email) {
  if (!email || !email.trim()) return false
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email.trim())
}

export function isValidPhone(phone) {
  if (!phone || !phone.trim()) return false
  
  const cleaned = phone.trim().replace(/[\s\-\.]/g, '')
  
  const phoneRegex = /^(0|\+84|84)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-6|8|9]|9[0-9])[0-9]{7}$/
  
  return phoneRegex.test(cleaned)
}

export function validateEmail(email, required = false) {
  if (required && (!email || !email.trim())) {
    return 'Vui lòng nhập email'
  }
  
  if (email && email.trim() && !isValidEmail(email)) {
    return 'Email không hợp lệ. Ví dụ: email@example.com'
  }
  
  return null
}


export function validatePhone(phone, required = false) {
  if (required && (!phone || !phone.trim())) {
    return 'Vui lòng nhập số điện thoại'
  }
  
  if (phone && phone.trim() && !isValidPhone(phone)) {
    return 'Số điện thoại không hợp lệ. Ví dụ: 0912345678 hoặc 0123456789'
  }
  
  return null
}

