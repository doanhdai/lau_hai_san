# Hướng dẫn tạo App Password cho Gmail - Từng bước chi tiết

## ⚠️ QUAN TRỌNG:
**App Password KHÔNG phải là mật khẩu email của bạn!**
- Mật khẩu email thông thường: `doanhdai01` ❌ (KHÔNG dùng được)
- App Password: `abcd efgh ijkl mnop` ✅ (16 ký tự, do Google tạo)

---

## 📋 BƯỚC 1: Bật Xác thực 2 bước (Bắt buộc)

1. **Mở trình duyệt**, truy cập: https://myaccount.google.com/security
2. Đăng nhập bằng tài khoản Gmail của bạn (`daido6323@gmail.com`)
3. Tìm mục **"Xác minh 2 bước"** (hoặc "2-Step Verification")
4. Nhấn vào **"Bắt đầu"** hoặc **"Get Started"**
5. Làm theo hướng dẫn:
   - Nhập số điện thoại của bạn
   - Google sẽ gửi mã xác minh qua SMS
   - Nhập mã xác minh để hoàn tất

**Lưu ý:** Bạn PHẢI bật xác thực 2 bước trước khi tạo App Password!

---

## 📋 BƯỚC 2: Tạo App Password

### Cách 1: Truy cập trực tiếp
1. Truy cập: https://myaccount.google.com/apppasswords
2. Nếu chưa bật xác thực 2 bước, sẽ yêu cầu bật trước

### Cách 2: Từ trang Bảo mật
1. Truy cập: https://myaccount.google.com/security
2. Tìm mục **"Mật khẩu ứng dụng"** (App passwords)
3. Nhấn vào **"Mật khẩu ứng dụng"**

### Tạo App Password:
1. Chọn **"Chọn ứng dụng"** → Chọn **"Thư"** (Mail)
2. Chọn **"Chọn thiết bị"** → Chọn **"Khác (Tên tùy chỉnh)"**
3. Nhập tên: **"Spring Boot App"** (hoặc tên bất kỳ)
4. Nhấn **"Tạo"** (Generate)
5. Google sẽ hiển thị mật khẩu 16 ký tự, ví dụ:
   ```
   abcd efgh ijkl mnop
   ```
6. **COPY mật khẩu này ngay** (bạn chỉ thấy 1 lần duy nhất!)
   - Có thể copy với khoảng trắng: `abcd efgh ijkl mnop`
   - Hoặc bỏ khoảng trắng: `abcdefghijklmnop`
   - Cả 2 cách đều được

---

## 📋 BƯỚC 3: Cập nhật application.properties

Mở file: `src/main/resources/application.properties`

Tìm dòng:
```properties
spring.mail.password=doanhdai01
```

Thay thế bằng App Password vừa tạo:
```properties
spring.mail.password=abcdefghijklmnop
```

**Ví dụ đầy đủ:**
```properties
# Mail Configuration (for sending reservation confirmations)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=daido6323@gmail.com
spring.mail.password=abcdefghijklmnop  # ← Dán App Password vào đây
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## ✅ KIỂM TRA

1. **Lưu file** `application.properties`
2. **Khởi động lại** ứng dụng Spring Boot
3. **Test gửi email:**
   - Tạo một reservation
   - Gán bàn cho reservation đó
   - Kiểm tra email của khách hàng có nhận được email không

---

## ❌ XỬ LÝ LỖI

### Lỗi: "Username and Password not accepted"
**Nguyên nhân:**
- Chưa bật xác thực 2 bước
- Dùng sai mật khẩu (dùng mật khẩu email thay vì App Password)
- App Password copy sai

**Giải pháp:**
1. Kiểm tra đã bật xác thực 2 bước chưa
2. Tạo App Password mới
3. Copy lại App Password vào `application.properties`

### Lỗi: "Could not connect to SMTP host"
**Nguyên nhân:**
- Firewall chặn port 587
- Kết nối internet

**Giải pháp:**
- Kiểm tra kết nối internet
- Thử đổi port 465 (SSL) thay vì 587 (STARTTLS)

---

## 📝 TÓM TẮT

1. ✅ Bật xác thực 2 bước: https://myaccount.google.com/security
2. ✅ Tạo App Password: https://myaccount.google.com/apppasswords
3. ✅ Copy App Password (16 ký tự)
4. ✅ Dán vào `application.properties` → `spring.mail.password=...`
5. ✅ Khởi động lại ứng dụng
6. ✅ Test gửi email

---

## 🔒 BẢO MẬT

- **KHÔNG** chia sẻ App Password với ai
- **KHÔNG** commit App Password lên Git (đã có trong file, nên cẩn thận!)
- Nếu App Password bị lộ, xóa nó và tạo mới
- App Password chỉ dùng cho ứng dụng, không dùng để đăng nhập Gmail

