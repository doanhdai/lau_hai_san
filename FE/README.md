# Frontend Quản Lý Nhà Hàng Lẫu

Frontend hiện đại cho hệ thống quản lý nhà hàng lẫu, được xây dựng với Vue.js 3 và TailwindCSS.

## 🎨 Công nghệ sử dụng

- **Vue 3** (Composition API)
- **Vue Router** - Điều hướng
- **Pinia** - State Management
- **Axios** - HTTP Client
- **TailwindCSS** - UI Framework
- **Heroicons** - Icons
- **Chart.js** - Biểu đồ
- **Vite** - Build Tool

## 🚀 Cài đặt

### Yêu cầu
- Node.js >= 16
- npm hoặc yarn
- Backend API đang chạy tại `http://localhost:8080`

### Các bước cài đặt

1. **Cài đặt dependencies:**
```bash
npm install
```

2. **Chạy development server:**
```bash
npm run dev
```

Ứng dụng sẽ chạy tại: `http://localhost:3000`

3. **Build cho production:**
```bash
npm run build
```

## 📁 Cấu trúc dự án

```
src/
├── assets/           # CSS và tài nguyên tĩnh
├── components/       # Components tái sử dụng
│   └── modals/      # Modal components
├── layouts/         # Layout components
├── router/          # Vue Router configuration
├── services/        # API services
├── stores/          # Pinia stores
├── views/           # Page components
│   ├── Auth/       # Login, Register
│   ├── Customers/  # Quản lý khách hàng
│   ├── Dishes/     # Quản lý món ăn
│   ├── Orders/     # Quản lý đơn hàng
│   ├── Reservations/ # Quản lý đặt bàn
│   └── ...
└── App.vue          # Root component
```

## 🎯 Tính năng chính

### 1. Dashboard
- Tổng quan thống kê hệ thống
- Biểu đồ doanh thu
- Món ăn bán chạy
- Trạng thái bàn/phòng
- Đánh giá trung bình

### 2. Quản lý Khách hàng
- Danh sách khách hàng
- Thêm/sửa/xóa khách hàng
- Quản lý khách VIP
- Chặn/bỏ chặn khách hàng
- Tìm kiếm và lọc

### 3. Quản lý Bàn & Phòng
- Danh sách bàn/phòng
- Cập nhật trạng thái
- Tìm bàn phù hợp theo số người
- Quản lý vị trí

### 4. Quản lý Thực đơn
- Danh sách món ăn
- Danh mục món ăn
- Thêm/sửa/xóa món
- Cập nhật trạng thái
- Tìm kiếm món ăn

### 5. Quản lý Đơn hàng
- Tạo đơn hàng mới
- Thêm món vào đơn
- Cập nhật trạng thái
- Theo dõi đơn hàng
- Lịch sử đơn hàng

### 6. Quản lý Đặt bàn
- Tạo đặt bàn mới
- Xác nhận/hủy đặt bàn
- Tìm kiếm theo ngày
- Lịch sử đặt bàn

### 7. Quản lý Khuyến mãi
- Tạo chương trình khuyến mãi
- Cập nhật khuyến mãi
- Áp dụng cho món ăn

### 8. Phản hồi Khách hàng
- Xem đánh giá
- Phản hồi khách hàng
- Quản lý feedback

### 9. Báo cáo
- Báo cáo doanh thu
- Thống kê món ăn bán chạy
- Doanh thu theo tháng
- Xuất báo cáo

## 🎨 Giao diện

### Design Principles
- **Modern & Clean**: Giao diện hiện đại, sạch sẽ
- **Responsive**: Tương thích mọi thiết bị
- **Animations**: Hiệu ứng mượt mà, chuyên nghiệp
- **Color Scheme**: Màu đỏ chủ đạo (hotpot theme)
- **UX Friendly**: Dễ sử dụng, trực quan

### Animations
- Fade in/out transitions
- Slide animations
- Hover effects
- Loading spinners
- Smooth page transitions

## 🔐 Authentication

Hệ thống sử dụng JWT authentication:

1. **Login**: Đăng nhập với username/password
2. **Token Storage**: Token được lưu trong localStorage
3. **Auto-login**: Tự động đăng nhập nếu có token
4. **Protected Routes**: Routes yêu cầu authentication

### Tài khoản demo
```
Username: admin
Password: admin123
```

## 🌐 API Integration

Frontend kết nối với Backend API tại `http://localhost:8080/api`

### Proxy Configuration
Vite đã được cấu hình proxy để tránh CORS:
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

### API Services
Tất cả API calls được quản lý trong `src/services/`:
- `authService.js` - Authentication
- `customerService.js` - Khách hàng
- `dishService.js` - Món ăn
- `orderService.js` - Đơn hàng
- `reservationService.js` - Đặt bàn
- `dashboardService.js` - Dashboard & Reports

## 🎭 State Management

Sử dụng Pinia stores:

### Auth Store (`stores/auth.js`)
- User information
- Login/logout
- Token management
- Role checking

### Notification Store (`stores/notification.js`)
- Toast notifications
- Success/Error messages
- Auto-dismiss

## 🎨 Styling

### TailwindCSS Classes
Custom classes được định nghĩa trong `assets/main.css`:
- `.btn-primary` - Primary button
- `.btn-secondary` - Secondary button
- `.card` - Card component
- `.input-field` - Input field
- `.badge-*` - Status badges

### Color Palette
```css
Primary Red: #dc2626
Gradient: from-red-500 to-red-800
Success: green-600
Warning: yellow-600
Error: red-600
```

## 📱 Responsive Design

- **Mobile**: < 768px
- **Tablet**: 768px - 1024px
- **Desktop**: > 1024px

Sidebar tự động collapse trên mobile.

## 🔧 Configuration

### Environment Variables
Tạo file `.env.local`:
```
VITE_API_URL=http://localhost:8080/api
```

### Vite Config
Cấu hình trong `vite.config.js`:
- Port: 3000
- Proxy: /api -> http://localhost:8080
- Alias: @ -> ./src

## 🐛 Debugging

### Vue DevTools
Cài đặt Vue DevTools extension để debug:
- State inspection
- Component hierarchy
- Event tracking

### Network Inspection
Sử dụng Browser DevTools Network tab để xem API calls.

## 📦 Dependencies

### Production
```json
{
  "vue": "^3.4.21",
  "vue-router": "^4.3.0",
  "pinia": "^2.1.7",
  "axios": "^1.6.7",
  "chart.js": "^4.4.2"
}
```

### Development
```json
{
  "@vitejs/plugin-vue": "^5.0.4",
  "vite": "^5.2.0",
  "tailwindcss": "^3.4.1"
}
```

## 🚀 Deployment

### Build
```bash
npm run build
```

### Preview
```bash
npm run preview
```

### Deploy
Deploy thư mục `dist/` lên web server hoặc platform:
- Netlify
- Vercel
- Firebase Hosting
- Nginx

## 📝 Notes

- Đảm bảo Backend API đang chạy trước khi start Frontend
- Kiểm tra CORS configuration trên Backend
- Token hết hạn sau 24 giờ (có thể cấu hình)
- Tất cả API calls đều có error handling

## 🤝 Contributing

1. Fork the project
2. Create feature branch
3. Commit changes
4. Push to branch
5. Open Pull Request

## 📄 License

MIT License

## 👥 Author

Hệ thống quản lý nhà hàng lẫu - Đồ án tốt nghiệp
