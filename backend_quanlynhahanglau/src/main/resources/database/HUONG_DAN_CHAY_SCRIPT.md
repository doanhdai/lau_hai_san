# Hướng dẫn chạy script cập nhật constraint cho dishes.status

## Cách 2: Sử dụng sqlcmd (Command Line)

### Bước 1: Mở PowerShell hoặc Command Prompt

Nhấn `Win + R`, gõ `powershell` hoặc `cmd` và nhấn Enter.

### Bước 2: Di chuyển đến thư mục chứa script

```powershell
cd "D:\hoc\lau_hais\lau_hais\lau_hai_san\backend_quanlynhahanglau\src\main\resources\database"
```

### Bước 3: Chạy lệnh sqlcmd

**Nếu dùng Windows Authentication (khuyến nghị):**
```powershell
sqlcmd -S localhost -d QuanLyNhaHangLau -E -i "update_dishes_status_constraint.sql"
```

**Nếu dùng SQL Server Authentication:**
```powershell
sqlcmd -S localhost -d QuanLyNhaHangLau -U sa -P "mat_khau_cua_ban" -i "update_dishes_status_constraint.sql"
```

### Giải thích các tham số:

- `-S localhost`: Tên server SQL Server (thay `localhost` bằng tên server nếu khác)
- `-d QuanLyNhaHangLau`: Tên database
- `-E`: Sử dụng Windows Authentication (đăng nhập bằng tài khoản Windows hiện tại)
- `-U sa`: Tên đăng nhập SQL Server (chỉ dùng khi không dùng -E)
- `-P "mat_khau"`: Mật khẩu SQL Server (chỉ dùng khi không dùng -E)
- `-i "update_dishes_status_constraint.sql"`: Đường dẫn đến file SQL script

### Cách đơn giản nhất: Chạy file .bat

1. Mở file `run_update_constraint.bat` trong thư mục database
2. Nhấp đúp để chạy
3. Kiểm tra kết quả

### Kiểm tra kết quả:

Nếu thành công, bạn sẽ thấy các thông báo:
- "Đã xóa constraint: ..."
- "Đã tạo constraint mới CK_dishes_status với DISCONTINUED"
- "Hoàn tất cập nhật constraint cho cột status của bảng dishes"

### Lưu ý:

- Đảm bảo SQL Server đang chạy
- Đảm bảo bạn có quyền thay đổi schema của database
- Nếu gặp lỗi kết nối, kiểm tra lại tên server và tên database

