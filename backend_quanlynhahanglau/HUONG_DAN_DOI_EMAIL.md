# Hướng dẫn đổi Email để gửi thông báo

## 📍 VỊ TRÍ CẤU HÌNH

File: `backend_quanlynhahanglau/src/main/resources/application.properties`

Dòng: **101-107**

## 🔧 CÁC THÔNG TIN CẦN ĐỔI

### 1. Nếu dùng Gmail (Khuyến nghị)

```properties
# Mail Configuration (for sending reservation confirmations)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com          # ← Đổi email của bạn
spring.mail.password=your_app_password            # ← Đổi App Password (16 ký tự)
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Các bước:**
1. **Đổi `spring.mail.username`**: Thay `your_email@gmail.com` bằng email Gmail của bạn
2. **Tạo App Password** (xem hướng dẫn bên dưới)
3. **Đổi `spring.mail.password`**: Dán App Password vào đây

### 2. Nếu dùng email khác (Outlook, Yahoo, v.v.)

#### Outlook/Hotmail:
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your_email@outlook.com
spring.mail.password=your_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### Yahoo Mail:
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=your_email@yahoo.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### Email doanh nghiệp (tùy chỉnh):
```properties
spring.mail.host=smtp.your-domain.com          # ← SMTP server của bạn
spring.mail.port=587                           # ← Hoặc 465 (SSL)
spring.mail.username=your_email@your-domain.com
spring.mail.password=your_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 📋 HƯỚNG DẪN TẠO APP PASSWORD CHO GMAIL

### Bước 1: Bật xác thực 2 bước
1. Truy cập: https://myaccount.google.com/security
2. Tìm mục **"Xác minh 2 bước"** (2-Step Verification)
3. Nhấn **"Bắt đầu"** và làm theo hướng dẫn

### Bước 2: Tạo App Password
1. Truy cập: https://myaccount.google.com/apppasswords
2. Chọn **"Chọn ứng dụng"** → **"Thư"** (Mail)
3. Chọn **"Chọn thiết bị"** → **"Khác (Tên tùy chỉnh)"**
4. Nhập tên: **"Spring Boot App"**
5. Nhấn **"Tạo"**
6. **Copy mật khẩu 16 ký tự** (chỉ thấy 1 lần!)

### Bước 3: Cập nhật application.properties
- Dán App Password vào `spring.mail.password=...`

---

## ✅ SAU KHI ĐỔI

1. **Lưu file** `application.properties`
2. **Khởi động lại backend** (dừng và chạy lại)
3. **Test gửi email:**
   - Tạo một đặt bàn (reservation)
   - Gán bàn cho đặt bàn đó
   - Kiểm tra email khách hàng có nhận được không

---

## 🔍 KIỂM TRA CẤU HÌNH HIỆN TẠI

Mở file `application.properties` và tìm:

```properties
# Mail Configuration (for sending reservation confirmations)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=daido6323@gmail.com        # ← Email hiện tại
spring.mail.password=espesndghiuzhgap          # ← App Password hiện tại
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## ⚠️ LƯU Ý

- **Gmail**: Bắt buộc dùng App Password (không dùng mật khẩu email thông thường)
- **Outlook/Yahoo**: Có thể cần App Password hoặc mật khẩu tùy cấu hình
- **Email doanh nghiệp**: Liên hệ IT để lấy thông tin SMTP server
- **Bảo mật**: Không commit mật khẩu lên Git công khai

---

## ❌ XỬ LÝ LỖI

### Lỗi: "Username and Password not accepted"
- Kiểm tra email và mật khẩu đã đúng chưa
- Nếu dùng Gmail: Đảm bảo dùng App Password, không phải mật khẩu email
- Kiểm tra đã bật xác thực 2 bước (nếu dùng Gmail)

### Lỗi: "Could not connect to SMTP host"
- Kiểm tra `spring.mail.host` đã đúng chưa
- Kiểm tra port (587 hoặc 465)
- Kiểm tra firewall không chặn port
- Kiểm tra kết nối internet

