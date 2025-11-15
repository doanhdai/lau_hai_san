-- Migration: Thêm cột is_deleted vào bảng restaurant_tables để hỗ trợ xóa mềm
-- Date: 2025-11-14

-- Thêm cột is_deleted vào bảng restaurant_tables với default 0 (false)
-- Lưu ý: Nếu cột đã tồn tại, sẽ báo lỗi. Bỏ qua lỗi và chạy tiếp các câu lệnh sau.
ALTER TABLE restaurant_tables 
ADD COLUMN is_deleted BIT DEFAULT 0;

-- Cập nhật tất cả các bàn có is_deleted IS NULL thành false (0)
UPDATE restaurant_tables 
SET is_deleted = 0 
WHERE is_deleted IS NULL;

-- Đảm bảo default value là 0 cho các bàn mới
ALTER TABLE restaurant_tables 
MODIFY COLUMN is_deleted BIT DEFAULT 0 
COMMENT 'Xóa mềm: 0 = chưa xóa, 1 = đã xóa';

