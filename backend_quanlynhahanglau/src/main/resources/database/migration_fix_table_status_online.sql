-- Migration: Cập nhật các bàn có status = 'ONLINE' (cũ) thành 'AVAILABLE'
-- Vì ONLINE giờ là type, không phải status nữa
-- Date: 2025-11-14

-- Cập nhật các bàn có status = 'ONLINE' (cũ) thành 'AVAILABLE'
UPDATE restaurant_tables 
SET status = 'AVAILABLE' 
WHERE status = 'ONLINE';

-- Cập nhật các bàn có status = 'MAINTENANCE' (nếu có) thành 'AVAILABLE'
-- Vì MAINTENANCE đã bị loại bỏ khỏi enum
UPDATE restaurant_tables 
SET status = 'AVAILABLE' 
WHERE status = 'MAINTENANCE';

