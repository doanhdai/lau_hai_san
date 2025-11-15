# Hướng dẫn cấu hình Email cho Gmail

## Cách 1: Sử dụng App Password (Khuyến nghị - An toàn hơn)

### Bước 1: Bật xác thực 2 bước (2-Step Verification)
1. Truy cập: https://myaccount.google.com/security
2. Tìm mục **"2-Step Verification"** (Xác minh 2 bước)
3. Nhấn **"Get Started"** hoặc **"Bắt đầu"**
4. Làm theo hướng dẫn để bật xác thực 2 bước (có thể dùng số điện thoại)

### Bước 2: Tạo App Password
1. Sau khi bật xác thực 2 bước, truy cập: https://myaccount.google.com/apppasswords
   - Hoặc vào: https://myaccount.google.com/security → Tìm **"App passwords"** (Mật khẩu ứng dụng)
2. Chọn **"Select app"** → Chọn **"Mail"**
3. Chọn **"Select device"** → Chọn **"Other (Custom name)"** → Nhập tên: **"Spring Boot App"**
4. Nhấn **"Generate"** (Tạo)
5. Google sẽ tạo cho bạn một mật khẩu 16 ký tự (có khoảng trắng), ví dụ: `abcd efgh ijkl mnop`
6. **Copy mật khẩu này** (bỏ khoảng trắng hoặc giữ nguyên đều được)

### Bước 3: Cấu hình trong application.properties
Mở file `src/main/resources/application.properties` và cập nhật:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=abcdefghijklmnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Lưu ý:**
- `spring.mail.username`: Email Gmail của bạn (ví dụ: `duy@gmail.com`)
- `spring.mail.password`: App Password vừa tạo (16 ký tự, có thể bỏ khoảng trắng)

---

## Cách 2: Sử dụng mật khẩu thông thường (Không khuyến nghị)

⚠️ **Cảnh báo:** Google đã tắt tính năng "Less secure app access" từ tháng 5/2022, nên cách này **KHÔNG CÒN HOẠT ĐỘNG** với Gmail thông thường.

Nếu bạn dùng email khác (không phải Gmail), có thể thử:
1. Bật "Less secure app access" trong cài đặt email
2. Sử dụng mật khẩu email thông thường

---

## Cách 3: Sử dụng Gmail với OAuth2 (Phức tạp hơn)

Nếu muốn dùng OAuth2, cần:
1. Tạo OAuth2 credentials trong Google Cloud Console
2. Cấu hình phức tạp hơn

**Khuyến nghị:** Dùng **Cách 1 (App Password)** vì đơn giản và an toàn nhất.

---

## Kiểm tra cấu hình

Sau khi cấu hình xong, khởi động lại ứng dụng và test bằng cách:
1. Tạo một reservation
2. Gán bàn cho reservation đó
3. Kiểm tra email của khách hàng có nhận được email không

## Xử lý lỗi thường gặp

### Lỗi: "Username and Password not accepted"
- Kiểm tra lại App Password đã copy đúng chưa
- Đảm bảo đã bật xác thực 2 bước
- Thử tạo App Password mới

### Lỗi: "Could not connect to SMTP host"
- Kiểm tra kết nối internet
- Kiểm tra firewall không chặn port 587
- Thử đổi port thành 465 và dùng SSL thay vì STARTTLS

