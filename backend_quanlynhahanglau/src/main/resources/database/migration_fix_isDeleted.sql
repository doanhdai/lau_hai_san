-- =============================================
-- Migration: Cập nhật cột is_deleted cho bảng dishes
-- Database: MySQL
-- =============================================

USE QuanLyNhaHangLau;

-- Đảm bảo cột is_deleted tồn tại
SET @dbname = DATABASE();
SET @tablename = 'dishes';
SET @columnname = 'is_deleted';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname
        AND TABLE_NAME = @tablename
        AND COLUMN_NAME = @columnname
    ) > 0,
    'SELECT "Column is_deleted already exists" AS result;',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIT NOT NULL DEFAULT 0;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- QUAN TRỌNG: Cập nhật tất cả các món ăn hiện tại để is_deleted = 0 (nếu là NULL)
UPDATE dishes SET is_deleted = 0 WHERE is_deleted IS NULL;

-- Đảm bảo cột không cho phép NULL và có giá trị mặc định
ALTER TABLE dishes MODIFY COLUMN is_deleted BIT NOT NULL DEFAULT 0;

SELECT CONCAT('Migration completed! Đã cập nhật ', ROW_COUNT(), ' records.') AS result;

