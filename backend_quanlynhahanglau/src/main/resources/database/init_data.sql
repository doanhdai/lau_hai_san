-- =============================================
-- Script khởi tạo dữ liệu cho hệ thống Quản lý Nhà hàng Lẫu
-- =============================================

USE QuanLyNhaHangLau;
GO

-- =============================================
-- 1. Insert Roles
-- =============================================
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
BEGIN
    INSERT INTO roles (name, description) VALUES 
    ('ROLE_ADMIN', N'Quản trị viên - Toàn quyền hệ thống'),
    ('ROLE_MANAGER', N'Quản lý - Quản lý nhà hàng'),
    ('ROLE_STAFF', N'Nhân viên - Phục vụ khách hàng'),
    ('ROLE_CUSTOMER', N'Khách hàng');
    
    PRINT 'Đã thêm roles thành công!';
END
GO

-- =============================================
-- 2. Insert Default Admin User
-- Password: admin123 (đã mã hóa BCrypt)
-- =============================================
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
BEGIN
    DECLARE @AdminUserId BIGINT;
    DECLARE @AdminRoleId BIGINT;
    
    -- Insert admin user
    INSERT INTO users (username, password, email, full_name, phone, active, created_at, updated_at)
    VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Rogg.HEhFvtYpKTkOv96qZ4W', 
            'admin@restaurant.com', N'Administrator', '0901234567', 1, GETDATE(), GETDATE());
    
    SET @AdminUserId = SCOPE_IDENTITY();
    
    -- Get admin role id
    SELECT @AdminRoleId = id FROM roles WHERE name = 'ROLE_ADMIN';
    
    -- Assign admin role
    INSERT INTO user_roles (user_id, role_id)
    VALUES (@AdminUserId, @AdminRoleId);
    
    PRINT 'Đã tạo tài khoản admin thành công! (Username: admin, Password: admin123)';
END
GO

-- =============================================
-- 3. Insert Sample Dish Categories
-- =============================================
IF NOT EXISTS (SELECT 1 FROM dish_categories)
BEGIN
    INSERT INTO dish_categories (name, description, active) VALUES
    (N'Lẩu', N'Các loại lẩu đặc sản', 1),
    (N'Hải sản', N'Hải sản tươi sống', 1),
    (N'Thịt', N'Thịt bò, heo, gà các loại', 1),
    (N'Rau củ', N'Rau củ tươi ngon', 1),
    (N'Nấm', N'Các loại nấm', 1),
    (N'Đồ uống', N'Nước ngọt, bia, rượu', 1),
    (N'Tráng miệng', N'Món tráng miệng', 1);
    
    PRINT 'Đã thêm danh mục món ăn thành công!';
END
GO

-- =============================================
-- 4. Insert Sample Dishes
-- =============================================
IF NOT EXISTS (SELECT 1 FROM dishes)
BEGIN
    DECLARE @LauCategoryId BIGINT, @HaiSanCategoryId BIGINT, @ThitCategoryId BIGINT;
    DECLARE @RauCategoryId BIGINT, @NamCategoryId BIGINT, @DoUongCategoryId BIGINT;
    
    SELECT @LauCategoryId = id FROM dish_categories WHERE name = N'Lẩu';
    SELECT @HaiSanCategoryId = id FROM dish_categories WHERE name = N'Hải sản';
    SELECT @ThitCategoryId = id FROM dish_categories WHERE name = N'Thịt';
    SELECT @RauCategoryId = id FROM dish_categories WHERE name = N'Rau củ';
    SELECT @NamCategoryId = id FROM dish_categories WHERE name = N'Nấm';
    SELECT @DoUongCategoryId = id FROM dish_categories WHERE name = N'Đồ uống';
    
    -- Lẩu
    INSERT INTO dishes (name, description, price, category_id, status, is_promotion, active, created_at, updated_at)
    VALUES
    (N'Lẩu Thái', N'Lẩu Thái chua cay đặc trưng', 299000, @LauCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Lẩu Tứ Xuyên', N'Lẩu tứ xuyên cay nồng', 349000, @LauCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Lẩu Hải Sản', N'Lẩu hải sản tươi sống', 399000, @LauCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE());
    
    -- Hải sản
    INSERT INTO dishes (name, description, price, category_id, status, is_promotion, active, created_at, updated_at)
    VALUES
    (N'Tôm sú', N'Tôm sú tươi (500g)', 250000, @HaiSanCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Mực ống', N'Mực ống tươi (500g)', 180000, @HaiSanCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Cá basa', N'Cá basa phi lê (500g)', 120000, @HaiSanCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE());
    
    -- Thịt
    INSERT INTO dishes (name, description, price, category_id, status, is_promotion, active, created_at, updated_at)
    VALUES
    (N'Thịt bò Mỹ', N'Thịt bò Mỹ nhập khẩu (300g)', 180000, @ThitCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Ba chỉ bò', N'Ba chỉ bò Úc (300g)', 150000, @ThitCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Thịt heo ba rọi', N'Thịt heo ba rọi (300g)', 90000, @ThitCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE());
    
    -- Rau củ
    INSERT INTO dishes (name, description, price, category_id, status, is_promotion, active, created_at, updated_at)
    VALUES
    (N'Rau tổng hợp', N'Rau tổng hợp cho lẩu', 50000, @RauCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Đậu hủ non', N'Đậu hủ non', 30000, @RauCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE());
    
    -- Nấm
    INSERT INTO dishes (name, description, price, category_id, status, is_promotion, active, created_at, updated_at)
    VALUES
    (N'Nấm tổng hợp', N'Nấm đùi gà, nấm kim châm, nấm bào ngư', 60000, @NamCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE());
    
    -- Đồ uống
    INSERT INTO dishes (name, description, price, category_id, status, is_promotion, active, created_at, updated_at)
    VALUES
    (N'Coca Cola', N'Coca Cola lon', 15000, @DoUongCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Bia Tiger', N'Bia Tiger lon', 20000, @DoUongCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE()),
    (N'Nước chanh dây', N'Nước chanh dây tươi', 25000, @DoUongCategoryId, 'AVAILABLE', 0, 1, GETDATE(), GETDATE());
    
    PRINT 'Đã thêm món ăn mẫu thành công!';
END
GO

-- =============================================
-- 5. Insert Sample Rooms
-- =============================================
IF NOT EXISTS (SELECT 1 FROM rooms)
BEGIN
    INSERT INTO rooms (room_number, name, capacity, status, description, active)
    VALUES
    ('VIP-01', N'Phòng VIP 1', 10, 'AVAILABLE', N'Phòng VIP với tầm nhìn đẹp', 1),
    ('VIP-02', N'Phòng VIP 2', 12, 'AVAILABLE', N'Phòng VIP rộng rãi', 1),
    ('FAMILY-01', N'Phòng Gia Đình 1', 8, 'AVAILABLE', N'Phòng gia đình ấm cúng', 1),
    ('FAMILY-02', N'Phòng Gia Đình 2', 8, 'AVAILABLE', N'Phòng gia đình tiện nghi', 1);
    
    PRINT 'Đã thêm phòng mẫu thành công!';
END
GO

-- =============================================
-- 6. Insert Sample Tables
-- =============================================
IF NOT EXISTS (SELECT 1 FROM restaurant_tables)
BEGIN
    INSERT INTO restaurant_tables (table_number, capacity, status, location, active, notes)
    VALUES
    ('T-01', 4, 'AVAILABLE', N'Tầng 1 - Khu A', 1, N'Bàn gần cửa sổ'),
    ('T-02', 4, 'AVAILABLE', N'Tầng 1 - Khu A', 1, NULL),
    ('T-03', 6, 'AVAILABLE', N'Tầng 1 - Khu B', 1, NULL),
    ('T-04', 6, 'AVAILABLE', N'Tầng 1 - Khu B', 1, NULL),
    ('T-05', 8, 'AVAILABLE', N'Tầng 1 - Khu C', 1, N'Bàn lớn'),
    ('T-06', 2, 'AVAILABLE', N'Tầng 2 - Khu A', 1, N'Bàn đôi'),
    ('T-07', 2, 'AVAILABLE', N'Tầng 2 - Khu A', 1, N'Bàn đôi'),
    ('T-08', 4, 'AVAILABLE', N'Tầng 2 - Khu B', 1, NULL),
    ('T-09', 4, 'AVAILABLE', N'Tầng 2 - Khu B', 1, NULL),
    ('T-10', 6, 'AVAILABLE', N'Tầng 2 - Khu C', 1, NULL);
    
    PRINT 'Đã thêm bàn mẫu thành công!';
END
GO

-- =============================================
-- 7. Insert Sample Customers
-- =============================================
IF NOT EXISTS (SELECT 1 FROM customers)
BEGIN
    INSERT INTO customers (full_name, phone, email, address, is_vip, active, blocked, notes, created_at, updated_at)
    VALUES
    (N'Nguyễn Văn A', '0912345678', 'nguyenvana@email.com', N'123 Đường ABC, Quận 1, TP.HCM', 1, 1, 0, N'Khách hàng thân thiết', GETDATE(), GETDATE()),
    (N'Trần Thị B', '0923456789', 'tranthib@email.com', N'456 Đường DEF, Quận 2, TP.HCM', 0, 1, 0, NULL, GETDATE(), GETDATE()),
    (N'Lê Văn C', '0934567890', 'levanc@email.com', N'789 Đường GHI, Quận 3, TP.HCM', 1, 1, 0, N'Khách VIP', GETDATE(), GETDATE());
    
    PRINT 'Đã thêm khách hàng mẫu thành công!';
END
GO

PRINT '============================================='
PRINT 'Khởi tạo dữ liệu hoàn tất!'
PRINT '============================================='
PRINT 'Tài khoản Admin:'
PRINT '  Username: admin'
PRINT '  Password: admin123'
PRINT '============================================='
GO
