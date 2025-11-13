-- Migration: Thêm cột position_x và position_y vào bảng restaurant_tables để lưu vị trí bàn
-- Date: 2025-11-11

-- Thêm cột position_x và position_y vào bảng restaurant_tables (nullable)
ALTER TABLE restaurant_tables 
ADD COLUMN position_x INTEGER DEFAULT NULL,
ADD COLUMN position_y INTEGER DEFAULT NULL;

-- Comment cho các cột
ALTER TABLE restaurant_tables 
MODIFY COLUMN position_x INTEGER DEFAULT NULL 
COMMENT 'Vị trí X trên bản đồ (pixels)';

ALTER TABLE restaurant_tables 
MODIFY COLUMN position_y INTEGER DEFAULT NULL 
COMMENT 'Vị trí Y trên bản đồ (pixels)';

