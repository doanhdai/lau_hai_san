-- Migration: Thêm cột user_id vào bảng customers để liên kết với bảng users
-- Date: 2025-11-11

-- Thêm cột user_id vào bảng customers (nullable vì không phải tất cả customer đều có user)
ALTER TABLE customers 
ADD COLUMN user_id BIGINT NULL;

-- Thêm khóa ngoại
ALTER TABLE customers 
ADD CONSTRAINT fk_customer_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE SET NULL 
ON UPDATE CASCADE;

-- Thêm index để tối ưu truy vấn
CREATE INDEX idx_customer_user_id ON customers(user_id);

-- Comment cho cột
ALTER TABLE customers 
MODIFY COLUMN user_id BIGINT NULL 
COMMENT 'Liên kết với bảng users - User ID của khách hàng (nếu có tài khoản)';

