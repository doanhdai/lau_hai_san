-- MySQL Script converted from SQL Server
-- Database: QuanLyNhaHangLau

CREATE DATABASE IF NOT EXISTS QuanLyNhaHangLau CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QuanLyNhaHangLau;

-- Table: customer_feedbacks
CREATE TABLE `customer_feedbacks`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`comment` VARCHAR(2000) NULL,
	`created_at` DATETIME(6) NOT NULL,
	`rating` INT NOT NULL,
	`resolved` TINYINT(1) NULL DEFAULT 0,
	`resolved_at` DATETIME(6) NULL,
	`response` VARCHAR(1000) NULL,
	`customer_id` BIGINT NOT NULL,
	`order_id` BIGINT NULL,
	`resolved_by` BIGINT NULL,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: customers
CREATE TABLE `customers`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`active` TINYINT(1) NULL DEFAULT 1,
	`address` VARCHAR(500) NULL,
	`blocked` TINYINT(1) NULL DEFAULT 0,
	`created_at` DATETIME(6) NOT NULL,
	`customer_code` VARCHAR(20) NULL,
	`email` VARCHAR(100) NULL,
	`full_name` VARCHAR(100) NOT NULL,
	`is_vip` TINYINT(1) NULL DEFAULT 1,
	`notes` VARCHAR(500) NULL,
	`phone` VARCHAR(15) NOT NULL,
	`updated_at` DATETIME(6) NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UKm3iom37efaxd5eucmxjqqcbe9` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: dish_categories
CREATE TABLE `dish_categories`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`active` TINYINT(1) NULL DEFAULT 1,
	`description` VARCHAR(500) NULL,
	`name` VARCHAR(100) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UKh6uvo8uk78dtgf703w61g92gh` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: dishes
CREATE TABLE `dishes`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`active` TINYINT(1) NULL DEFAULT 1,
	`created_at` DATETIME(6) NOT NULL,
	`description` VARCHAR(1000) NULL,
	`image_url` VARCHAR(500) NULL,
	`is_promotion` TINYINT(1) NULL DEFAULT 0,
	`name` VARCHAR(200) NOT NULL,
	`price` DECIMAL(10, 2) NOT NULL,
	`status` VARCHAR(255) NOT NULL,
	`updated_at` DATETIME(6) NULL,
	`category_id` BIGINT NOT NULL,
	`promotion_id` BIGINT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `CHK_dishes_status` CHECK (`status`='UNAVAILABLE' OR `status`='AVAILABLE')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: order_details
CREATE TABLE `order_details`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`notes` VARCHAR(500) NULL,
	`price` DECIMAL(10, 2) NOT NULL,
	`quantity` INT NOT NULL,
	`subtotal` DECIMAL(10, 2) NOT NULL,
	`dish_id` BIGINT NOT NULL,
	`order_id` BIGINT NOT NULL,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: orders
CREATE TABLE `orders`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`completed_at` DATETIME(6) NULL,
	`created_at` DATETIME(6) NOT NULL,
	`discount` DECIMAL(10, 2) NULL,
	`notes` VARCHAR(1000) NULL,
	`order_number` VARCHAR(50) NOT NULL,
	`status` VARCHAR(255) NOT NULL,
	`subtotal` DECIMAL(10, 2) NOT NULL,
	`tax` DECIMAL(10, 2) NULL,
	`total` DECIMAL(10, 2) NOT NULL,
	`updated_at` DATETIME(6) NULL,
	`created_by` BIGINT NULL,
	`customer_id` BIGINT NULL,
	`reservation_id` BIGINT NULL,
	`room_id` BIGINT NULL,
	`table_id` BIGINT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UKnthkiu7pgmnqnu86i2jyoe2v7` (`order_number`),
	CONSTRAINT `CHK_orders_status` CHECK (`status`='CANCELLED' OR `status`='COMPLETED' OR `status`='SERVED' OR `status`='READY' OR `status`='PREPARING' OR `status`='CONFIRMED' OR `status`='PENDING')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: promotions
CREATE TABLE `promotions`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`active` TINYINT(1) NULL DEFAULT 1,
	`created_at` DATETIME(6) NOT NULL,
	`description` VARCHAR(1000) NULL,
	`discount_amount` DECIMAL(10, 2) NULL,
	`discount_percent` DECIMAL(5, 2) NULL,
	`end_date` DATETIME(6) NOT NULL,
	`name` VARCHAR(200) NOT NULL,
	`start_date` DATETIME(6) NOT NULL,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: reservations
CREATE TABLE `reservations`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`confirmed_at` DATETIME(6) NULL,
	`created_at` DATETIME(6) NOT NULL,
	`email_sent` TINYINT(1) NULL DEFAULT 0,
	`notes` VARCHAR(1000) NULL,
	`number_of_guests` INT NOT NULL,
	`reservation_time` DATETIME(6) NOT NULL,
	`special_requests` VARCHAR(1000) NULL,
	`status` VARCHAR(255) NOT NULL,
	`updated_at` DATETIME(6) NULL,
	`confirmed_by` BIGINT NULL,
	`customer_id` BIGINT NOT NULL,
	`room_id` BIGINT NULL,
	`table_id` BIGINT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `CHK_reservations_status` CHECK (`status`='NO_SHOW' OR `status`='COMPLETED' OR `status`='CANCELLED' OR `status`='CONFIRMED' OR `status`='PENDING')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: restaurant_tables
CREATE TABLE `restaurant_tables`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`active` TINYINT(1) NULL DEFAULT 1,
	`capacity` INT NOT NULL,
	`location` VARCHAR(100) NULL,
	`notes` VARCHAR(500) NULL,
	`status` VARCHAR(255) NOT NULL,
	`table_number` VARCHAR(50) NOT NULL,
	`room_id` BIGINT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK1t9vy8tg8eidmfw4k887ibeu1` (`table_number`),
	CONSTRAINT `CHK_restaurant_tables_status` CHECK (`status`='MAINTENANCE' OR `status`='OCCUPIED' OR `status`='RESERVED' OR `status`='AVAILABLE')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: roles
CREATE TABLE `roles`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`description` VARCHAR(500) NULL,
	`name` VARCHAR(20) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UKofx66keruapi6vyqpv6f2or37` (`name`),
	CONSTRAINT `CHK_roles_name` CHECK (`name`='ROLE_CUSTOMER' OR `name`='ROLE_STAFF' OR `name`='ROLE_MANAGER' OR `name`='ROLE_ADMIN')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: rooms
CREATE TABLE `rooms`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`active` TINYINT(1) NULL DEFAULT 1,
	`capacity` INT NOT NULL,
	`description` VARCHAR(500) NULL,
	`name` VARCHAR(100) NOT NULL,
	`room_number` VARCHAR(50) NOT NULL,
	`status` VARCHAR(255) NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK7ljglxlj90ln3lbas4kl983m2` (`room_number`),
	CONSTRAINT `CHK_rooms_status` CHECK (`status`='MAINTENANCE' OR `status`='OCCUPIED' OR `status`='RESERVED' OR `status`='AVAILABLE')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: table_histories
CREATE TABLE `table_histories`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`created_at` DATETIME(6) NOT NULL,
	`end_time` DATETIME(6) NULL,
	`notes` VARCHAR(500) NULL,
	`start_time` DATETIME(6) NULL,
	`status` VARCHAR(255) NOT NULL,
	`customer_id` BIGINT NULL,
	`order_id` BIGINT NULL,
	`room_id` BIGINT NULL,
	`table_id` BIGINT NOT NULL,
	PRIMARY KEY (`id`),
	CONSTRAINT `CHK_table_histories_status` CHECK (`status`='MAINTENANCE' OR `status`='OCCUPIED' OR `status`='RESERVED' OR `status`='AVAILABLE')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_roles
CREATE TABLE `user_roles`(
	`user_id` BIGINT NOT NULL,
	`role_id` BIGINT NOT NULL,
	PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: users
CREATE TABLE `users`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`active` TINYINT(1) NULL DEFAULT 1,
	`created_at` DATETIME(6) NOT NULL,
	`email` VARCHAR(100) NOT NULL,
	`full_name` VARCHAR(100) NOT NULL,
	`password` VARCHAR(255) NOT NULL,
	`phone` VARCHAR(15) NULL,
	`updated_at` DATETIME(6) NULL,
	`username` VARCHAR(50) NOT NULL,
	`address` VARCHAR(500) NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
	UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert data: customers
INSERT INTO `customers` (`id`, `active`, `address`, `blocked`, `created_at`, `customer_code`, `email`, `full_name`, `is_vip`, `notes`, `phone`, `updated_at`) VALUES 
(1, 1, '123 Đường ABC, Quận 1, TP.HCM', 0, CAST('2025-11-04 11:09:03.513333' AS DATETIME), 'C-231184', 'nguyenvana@email.com', 'Nguyễn Văn A', 1, 'Khách hàng thân thiết', '0912345678', CAST('2025-11-06 12:34:09.143451' AS DATETIME)),
(2, 1, '456 Đường DEF, Quận 2, TP.HCM', 0, CAST('2025-11-04 11:09:03.513333' AS DATETIME), 'C-526754', 'tranthib@email.com', 'Trần Thị B', 0, NULL, '0923456789', CAST('2025-11-06 12:34:09.175312' AS DATETIME)),
(3, 1, '789 Đường GHI, Quận 3, TP.HCM', 0, CAST('2025-11-04 11:09:03.513333' AS DATETIME), 'C-123568', 'levanc@email.com', 'Lê Văn C', 1, 'Khách VIP', '0934567890', CAST('2025-11-06 12:34:09.186293' AS DATETIME)),
(4, 1, NULL, 0, CAST('2025-11-04 11:16:25.569379' AS DATETIME), 'C-533281', 'duy@gmail.com', 'tran ha', 1, NULL, '0123456789', CAST('2025-11-05 20:28:04.781612' AS DATETIME)),
(6, 1, NULL, 0, CAST('2025-11-06 12:48:31.705296' AS DATETIME), 'C-470683', 'duy88008@gmail.com', 'duy', 0, NULL, '0987654321', CAST('2025-11-06 12:48:31.705296' AS DATETIME)),
(8, 1, NULL, 0, CAST('2025-11-06 12:58:11.113858' AS DATETIME), 'C-496812', 'duy88889@gmail.com', 'Trần Văn', 0, NULL, '0888888888', CAST('2025-11-06 12:58:11.113858' AS DATETIME)),
(9, 1, NULL, 0, CAST('2025-11-06 13:37:55.847364' AS DATETIME), 'C-942898', NULL, 'duy', 0, NULL, '0352923442', CAST('2025-11-06 13:37:55.847364' AS DATETIME)),
(10, 1, NULL, 0, CAST('2025-11-06 13:51:28.727511' AS DATETIME), 'C-615754', 'khong@gmail.com', 'nguyen van', 0, NULL, '0968403505', CAST('2025-11-06 13:51:28.727511' AS DATETIME)),
(11, 1, NULL, 0, CAST('2025-11-06 13:52:48.117242' AS DATETIME), 'C-325454', NULL, 'dd', 0, NULL, '0098765443', CAST('2025-11-06 13:52:48.117242' AS DATETIME));

-- Insert data: dish_categories
INSERT INTO `dish_categories` (`id`, `active`, `description`, `name`) VALUES 
(1, 1, 'Các loại lẩu đặc sản', 'Lẩu'),
(2, 1, 'Hải sản tươi sống', 'Hải sản'),
(3, 1, 'Thịt bò, heo, gà các loại', 'Thịt'),
(4, 1, 'Rau củ tươi ngon', 'Rau củ'),
(5, 1, 'Các loại nấm', 'Nấm'),
(6, 1, 'Nước ngọt, bia, rượu', 'Nước uống'),
(7, 1, 'Món tráng miệng', 'Tráng miệng');

-- Insert data: dishes
INSERT INTO `dishes` (`id`, `active`, `created_at`, `description`, `image_url`, `is_promotion`, `name`, `price`, `status`, `updated_at`, `category_id`, `promotion_id`) VALUES 
(13, 1, CAST('2025-11-04 11:11:43.150000' AS DATETIME), 'Lẩu Thái chua cay đặc trưng với hải sản tươi sống', 'https://via.placeholder.com/400x400?text=Lau+Thai', 1, 'cua hoàng đế', CAST(350000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-06 14:13:33.800385' AS DATETIME), 1, NULL),
(14, 1, CAST('2025-11-04 11:11:43.150000' AS DATETIME), 'Lẩu nấm thanh mát với nhiều loại nấm cao cấp', 'https://via.placeholder.com/400x400?text=Lau+Nam', 0, 'Lẩu Nấm', CAST(280000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.150000' AS DATETIME), 1, NULL),
(15, 1, CAST('2025-11-04 11:11:43.150000' AS DATETIME), 'Lẩu cá hồi Nhật Bản cao cấp', 'https://via.placeholder.com/400x400?text=Lau+Ca+Hoi', 1, 'Lẩu Cá Hồi', CAST(450000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.150000' AS DATETIME), 1, NULL),
(16, 1, CAST('2025-11-04 11:11:43.150000' AS DATETIME), 'Lẩu Hàn Quốc cay nóng đặc trưng', 'https://via.placeholder.com/400x400?text=Lau+Tokbokki', 0, 'Lẩu Tokbokki', CAST(320000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.150000' AS DATETIME), 1, NULL),
(17, 1, CAST('2025-11-04 11:11:43.150000' AS DATETIME), 'Lẩu hải sản kiểu Pháp với sốt kem béo ngậy', 'https://via.placeholder.com/400x400?text=Lau+Hai+San+Phap', 1, 'Lẩu Hải Sản Pháp', CAST(480000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.150000' AS DATETIME), 1, NULL),
(18, 1, CAST('2025-11-04 11:11:43.150000' AS DATETIME), 'Lẩu bò Úc cao cấp với thịt thăn nội', 'https://via.placeholder.com/400x400?text=Lau+Bo+Uc', 1, 'Lẩu Bò Úc', CAST(420000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.150000' AS DATETIME), 1, NULL),
(19, 1, CAST('2025-11-04 11:11:43.150000' AS DATETIME), 'Lẩu chua cá lóc miền Tây Nam Bộ', 'https://via.placeholder.com/400x400?text=Lau+Ca+Loc', 0, 'Lẩu Chua Cá Lóc', CAST(290000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.150000' AS DATETIME), 1, NULL),
(20, 1, CAST('2025-11-04 11:11:43.150000' AS DATETIME), 'Lẩu dê thuốc bắc bổ dưỡng', 'https://via.placeholder.com/400x400?text=Lau+De', 0, 'Lẩu Dê', CAST(350000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.150000' AS DATETIME), 1, NULL),
(21, 1, CAST('2025-11-04 11:11:43.166667' AS DATETIME), 'Đĩa rau củ đa dạng: rau muống, cải, bắp cải...', 'https://via.placeholder.com/400x400?text=Rau+Cu', 0, 'Rau Củ Tổng Hợp', CAST(45000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.166667' AS DATETIME), 2, NULL),
(22, 1, CAST('2025-11-04 11:11:43.166667' AS DATETIME), 'Nấm kim châm tươi ngon', 'https://via.placeholder.com/400x400?text=Nam+Kim+Cham', 0, 'Nấm Kim Châm', CAST(35000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.166667' AS DATETIME), 2, NULL),
(23, 1, CAST('2025-11-04 11:11:43.166667' AS DATETIME), 'Nấm đùi gà thơm ngon', 'https://via.placeholder.com/400x400?text=Nam+Dui+Ga', 0, 'Nấm Đùi Gà', CAST(40000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.166667' AS DATETIME), 2, NULL),
(24, 1, CAST('2025-11-04 11:11:43.166667' AS DATETIME), 'Nấm linh chi cao cấp', 'https://via.placeholder.com/400x400?text=Nam+Linh+Chi', 0, 'Nấm Linh Chi', CAST(65000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.166667' AS DATETIME), 2, NULL),
(25, 1, CAST('2025-11-04 11:11:43.166667' AS DATETIME), 'Rau mồng tơi hữu cơ', 'https://via.placeholder.com/400x400?text=Mong+Toi', 0, 'Rau Mồng Tơi', CAST(30000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.166667' AS DATETIME), 2, NULL),
(26, 1, CAST('2025-11-04 11:11:43.166667' AS DATETIME), 'Măng tây xanh nhập khẩu', 'https://via.placeholder.com/400x400?text=Mang+Tay', 0, 'Măng Tây', CAST(55000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.166667' AS DATETIME), 2, NULL),
(27, 1, CAST('2025-11-04 11:11:43.173333' AS DATETIME), 'Tôm sú size lớn tươi sống', 'https://via.placeholder.com/400x400?text=Tom+Su', 1, 'Tôm Sú', CAST(180000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.173333' AS DATETIME), 3, NULL),
(28, 1, CAST('2025-11-04 11:11:43.173333' AS DATETIME), 'Mực ống tươi ngon', 'https://via.placeholder.com/400x400?text=Muc+Ong', 0, 'Mực Ống', CAST(120000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.173333' AS DATETIME), 3, NULL),
(29, 1, CAST('2025-11-04 11:11:43.173333' AS DATETIME), 'Nghêu sống tươi rửa sạch', 'https://via.placeholder.com/400x400?text=Ngheu', 0, 'Nghêu Sống', CAST(85000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.173333' AS DATETIME), 3, NULL),
(30, 1, CAST('2025-11-04 11:11:43.173333' AS DATETIME), 'Cua hoàng đế Alaska', 'https://via.placeholder.com/400x400?text=Cua+Hoang+De', 1, 'Cua Hoàng Đế', CAST(450000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.173333' AS DATETIME), 3, NULL),
(31, 1, CAST('2025-11-04 11:11:43.173333' AS DATETIME), 'Bạch tuộc baby tươi', 'https://via.placeholder.com/400x400?text=Bach+Tuoc', 0, 'Bạch Tuộc', CAST(150000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.173333' AS DATETIME), 3, NULL),
(32, 1, CAST('2025-11-04 11:11:43.173333' AS DATETIME), 'Cá hồi Na Uy phi lê', 'https://via.placeholder.com/400x400?text=Ca+Hoi', 1, 'Cá Hồi Phi Lê', CAST(220000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.173333' AS DATETIME), 3, NULL),
(33, 1, CAST('2025-11-04 11:11:43.173333' AS DATETIME), 'Ốc hương luộc sẵn', 'https://via.placeholder.com/400x400?text=Oc+Huong', 0, 'Ốc Hương', CAST(95000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.173333' AS DATETIME), 3, NULL),
(34, 1, CAST('2025-11-04 11:11:43.183333' AS DATETIME), 'Thịt bò Úc thái lát mỏng', 'https://via.placeholder.com/400x400?text=Bo+Uc', 1, 'Thịt Bò Úc', CAST(250000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.183333' AS DATETIME), 4, NULL),
(35, 1, CAST('2025-11-04 11:11:43.183333' AS DATETIME), 'Ba chỉ heo tươi thái mỏng', 'https://via.placeholder.com/400x400?text=Ba+Chi', 0, 'Ba Chỉ Heo', CAST(120000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.183333' AS DATETIME), 4, NULL),
(36, 1, CAST('2025-11-04 11:11:43.183333' AS DATETIME), 'Thịt gà ta thái lát', 'https://via.placeholder.com/400x400?text=Ga+Ta', 0, 'Thịt Gà Ta', CAST(95000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.183333' AS DATETIME), 4, NULL),
(37, 1, CAST('2025-11-04 11:11:43.183333' AS DATETIME), 'Bò Mỹ Wagyu cắt lát mỏng', 'https://via.placeholder.com/400x400?text=Bo+Wagyu', 1, 'Bò Mỹ Wagyu', CAST(450000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.183333' AS DATETIME), 4, NULL),
(38, 1, CAST('2025-11-04 11:11:43.183333' AS DATETIME), 'Sườn non heo ướp sẵn', 'https://via.placeholder.com/400x400?text=Suon+Heo', 0, 'Sườn Non Heo', CAST(135000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.183333' AS DATETIME), 4, NULL),
(39, 1, CAST('2025-11-04 11:11:43.183333' AS DATETIME), 'Thịt cừu New Zealand', 'https://via.placeholder.com/400x400?text=Cuu', 0, 'Thịt Cừu', CAST(280000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.183333' AS DATETIME), 4, NULL),
(40, 1, CAST('2025-11-04 11:11:43.200000' AS DATETIME), 'Mì ramen Nhật Bản', 'https://via.placeholder.com/400x400?text=Mi+Ramen', 0, 'Mì Ramen', CAST(25000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.200000' AS DATETIME), 5, NULL),
(41, 1, CAST('2025-11-04 11:11:43.200000' AS DATETIME), 'Bún tươi sợi nhỏ', 'https://via.placeholder.com/400x400?text=Bun', 0, 'Bún Tươi', CAST(20000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.200000' AS DATETIME), 5, NULL),
(42, 1, CAST('2025-11-04 11:11:43.200000' AS DATETIME), 'Miến dong trong suốt', 'https://via.placeholder.com/400x400?text=Mien', 0, 'Miến Dong', CAST(22000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.200000' AS DATETIME), 5, NULL),
(43, 1, CAST('2025-11-04 11:11:43.200000' AS DATETIME), 'Bánh đa đỏ Hải Phòng', 'https://via.placeholder.com/400x400?text=Banh+Da', 0, 'Bánh Đa', CAST(30000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.200000' AS DATETIME), 5, NULL),
(44, 1, CAST('2025-11-04 11:11:43.200000' AS DATETIME), 'Trứng gà đào', 'https://via.placeholder.com/400x400?text=Trung+Dao', 0, 'Trứng Đào', CAST(15000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.200000' AS DATETIME), 5, NULL),
(45, 1, CAST('2025-11-04 11:11:43.206667' AS DATETIME), 'Coca Cola lon 330ml', 'https://via.placeholder.com/400x400?text=Coca', 0, 'Coca Cola', CAST(15000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.206667' AS DATETIME), 6, NULL),
(46, 1, CAST('2025-11-04 11:11:43.206667' AS DATETIME), 'Bia Heineken lon 330ml', 'https://via.placeholder.com/400x400?text=Bia', 0, 'Bia Heineken', CAST(25000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.206667' AS DATETIME), 6, NULL),
(47, 1, CAST('2025-11-04 11:11:43.206667' AS DATETIME), 'Nước cam ép tươi 100%', 'https://via.placeholder.com/400x400?text=Nuoc+Cam', 0, 'Nước Cam Ép', CAST(35000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.206667' AS DATETIME), 6, NULL),
(48, 1, CAST('2025-11-04 11:11:43.206667' AS DATETIME), 'Trà xanh Không Độ', 'https://via.placeholder.com/400x400?text=Tra', 0, 'Trà Xanh', CAST(15000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.206667' AS DATETIME), 6, NULL),
(49, 1, CAST('2025-11-04 11:11:43.206667' AS DATETIME), 'Nước suối Lavie 500ml', 'https://via.placeholder.com/400x400?text=Nuoc+Suoi', 0, 'Nước Suối', CAST(10000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.206667' AS DATETIME), 6, NULL),
(50, 1, CAST('2025-11-04 11:11:43.206667' AS DATETIME), 'Sinh tố bơ sữa', 'https://via.placeholder.com/400x400?text=Sinh+To', 0, 'Sinh Tố Bơ', CAST(45000.00 AS DECIMAL(10, 2)), 'AVAILABLE', CAST('2025-11-04 11:11:43.206667' AS DATETIME), 6, NULL);

-- Insert data: order_details
INSERT INTO `order_details` (`id`, `notes`, `price`, `quantity`, `subtotal`, `dish_id`, `order_id`) VALUES 
(1, '', CAST(350000.00 AS DECIMAL(10, 2)), 1, CAST(350000.00 AS DECIMAL(10, 2)), 13, 1),
(2, '', CAST(280000.00 AS DECIMAL(10, 2)), 1, CAST(280000.00 AS DECIMAL(10, 2)), 14, 1),
(3, '', CAST(320000.00 AS DECIMAL(10, 2)), 1, CAST(320000.00 AS DECIMAL(10, 2)), 16, 1),
(4, '', CAST(350000.00 AS DECIMAL(10, 2)), 1, CAST(350000.00 AS DECIMAL(10, 2)), 20, 2),
(5, '', CAST(45000.00 AS DECIMAL(10, 2)), 1, CAST(45000.00 AS DECIMAL(10, 2)), 21, 2),
(6, '', CAST(55000.00 AS DECIMAL(10, 2)), 1, CAST(55000.00 AS DECIMAL(10, 2)), 26, 2);

-- Insert data: orders
INSERT INTO `orders` (`id`, `completed_at`, `created_at`, `discount`, `notes`, `order_number`, `status`, `subtotal`, `tax`, `total`, `updated_at`, `created_by`, `customer_id`, `reservation_id`, `room_id`, `table_id`) VALUES 
(1, NULL, CAST('2025-11-06 13:39:13.454270' AS DATETIME), CAST(0.00 AS DECIMAL(10, 2)), 's', 'ORD20251106133913', 'CONFIRMED', CAST(950000.00 AS DECIMAL(10, 2)), CAST(95000.00 AS DECIMAL(10, 2)), CAST(1045000.00 AS DECIMAL(10, 2)), CAST('2025-11-06 13:39:37.061364' AS DATETIME), 5, 6, 3, NULL, NULL),
(2, NULL, CAST('2025-11-06 13:56:48.151710' AS DATETIME), CAST(0.00 AS DECIMAL(10, 2)), 'ít thịt', 'ORD20251106135648', 'CONFIRMED', CAST(450000.00 AS DECIMAL(10, 2)), CAST(45000.00 AS DECIMAL(10, 2)), CAST(495000.00 AS DECIMAL(10, 2)), CAST('2025-11-06 13:56:57.579774' AS DATETIME), 5, 11, 9, NULL, NULL);

-- Insert data: reservations
INSERT INTO `reservations` (`id`, `confirmed_at`, `created_at`, `email_sent`, `notes`, `number_of_guests`, `reservation_time`, `special_requests`, `status`, `updated_at`, `confirmed_by`, `customer_id`, `room_id`, `table_id`) VALUES 
(1, NULL, CAST('2025-11-04 11:16:25.571434' AS DATETIME), 0, 'khong', 12, CAST('2025-11-04 13:30:00.000000' AS DATETIME), 'vip', 'PENDING', CAST('2025-11-04 11:16:25.571434' AS DATETIME), NULL, 4, NULL, NULL),
(2, NULL, CAST('2025-11-06 12:47:25.247315' AS DATETIME), 0, 'không', 13, CAST('2025-11-06 15:30:00.000000' AS DATETIME), 'vip', 'PENDING', CAST('2025-11-06 12:47:25.247315' AS DATETIME), NULL, 4, NULL, NULL),
(3, CAST('2025-11-06 12:48:44.709945' AS DATETIME), CAST('2025-11-06 12:48:31.711975' AS DATETIME), 0, 'khogno', 5, CAST('2025-11-06 16:30:00.000000' AS DATETIME), 'vip', 'CONFIRMED', CAST('2025-11-06 12:48:44.709945' AS DATETIME), 5, 6, NULL, NULL),
(4, NULL, CAST('2025-11-06 12:56:40.290644' AS DATETIME), 0, 'khogno', 5, CAST('2025-11-06 16:30:00.000000' AS DATETIME), 'vip', 'PENDING', CAST('2025-11-06 12:56:40.290644' AS DATETIME), NULL, 6, NULL, NULL),
(5, NULL, CAST('2025-11-06 12:57:16.959279' AS DATETIME), 0, 'khogno', 5, CAST('2025-11-06 16:30:00.000000' AS DATETIME), 'vip', 'PENDING', CAST('2025-11-06 12:57:16.959279' AS DATETIME), NULL, 6, NULL, NULL),
(6, NULL, CAST('2025-11-06 12:58:11.118967' AS DATETIME), 0, 'không', 17, CAST('2025-11-06 17:30:00.000000' AS DATETIME), 'normal', 'PENDING', CAST('2025-11-06 12:58:11.118967' AS DATETIME), NULL, 8, NULL, NULL),
(7, NULL, CAST('2025-11-06 13:37:55.896078' AS DATETIME), 0, 's', 4, CAST('2025-11-06 06:40:00.000000' AS DATETIME), NULL, 'PENDING', CAST('2025-11-06 13:37:55.896078' AS DATETIME), NULL, 9, NULL, NULL),
(8, NULL, CAST('2025-11-06 13:51:28.731749' AS DATETIME), 0, 'khogno', 14, CAST('2025-11-06 15:30:00.000000' AS DATETIME), 'normal', 'PENDING', CAST('2025-11-06 13:51:28.732974' AS DATETIME), NULL, 10, NULL, NULL),
(9, CAST('2025-11-06 13:52:57.213830' AS DATETIME), CAST('2025-11-06 13:52:48.120276' AS DATETIME), 0, 'không', 7, CAST('2025-11-06 09:54:00.000000' AS DATETIME), NULL, 'CONFIRMED', CAST('2025-11-06 13:52:57.213829' AS DATETIME), 5, 11, NULL, NULL);

-- Insert data: restaurant_tables
INSERT INTO `restaurant_tables` (`id`, `active`, `capacity`, `location`, `notes`, `status`, `table_number`, `room_id`) VALUES 
(1, 1, 4, 'Tầng 1 - Khu A', 'Bàn gần cửa sổ', 'AVAILABLE', 'T-01', NULL),
(2, 1, 4, 'Tầng 1 - Khu A', NULL, 'AVAILABLE', 'T-02', NULL),
(3, 1, 6, 'Tầng 1 - Khu B', NULL, 'AVAILABLE', 'T-03', NULL),
(4, 1, 6, 'Tầng 1 - Khu B', NULL, 'AVAILABLE', 'T-04', NULL),
(5, 1, 8, 'Tầng 1 - Khu C', 'Bàn lớn', 'AVAILABLE', 'T-05', NULL),
(6, 1, 2, 'Tầng 2 - Khu A', 'Bàn đôi', 'AVAILABLE', 'T-06', NULL),
(7, 1, 2, 'Tầng 2 - Khu A', 'Bàn đôi', 'AVAILABLE', 'T-07', NULL),
(8, 1, 4, 'Tầng 2 - Khu B', NULL, 'AVAILABLE', 'T-08', NULL),
(9, 1, 4, 'Tầng 2 - Khu B', NULL, 'AVAILABLE', 'T-09', NULL),
(10, 1, 6, 'Tầng 2 - Khu C', NULL, 'AVAILABLE', 'T-10', NULL);

-- Insert data: roles
INSERT INTO `roles` (`id`, `description`, `name`) VALUES 
(1, 'Quản trị viên hệ thống - Quyền cao nhất', 'ROLE_ADMIN'),
(2, 'Quản lý nhà hàng - Quản lý hoạt động', 'ROLE_MANAGER'),
(3, 'Nhân viên - Phục vụ và xử lý đơn', 'ROLE_STAFF'),
(4, 'Khách hàng - Đặt bàn và đánh giá', 'ROLE_CUSTOMER');

-- Insert data: rooms
INSERT INTO `rooms` (`id`, `active`, `capacity`, `description`, `name`, `room_number`, `status`) VALUES 
(1, 1, 10, 'Phòng VIP với tầm nhìn đẹp', 'Phòng VIP 1', 'VIP-01', 'AVAILABLE'),
(2, 1, 12, 'Phòng VIP rộng rãi', 'Phòng VIP 2', 'VIP-02', 'AVAILABLE'),
(3, 1, 8, 'Phòng gia đình ấm cúng', 'Phòng Gia Đình 1', 'FAMILY-01', 'AVAILABLE'),
(4, 1, 8, 'Phòng gia đình tiện nghi', 'Phòng Gia Đình 2', 'FAMILY-02', 'AVAILABLE');

-- Insert data: user_roles
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES 
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 1);

-- Insert data: users
INSERT INTO `users` (`id`, `active`, `created_at`, `email`, `full_name`, `password`, `phone`, `updated_at`, `username`, `address`) VALUES 
(1, 1, CAST('2025-11-04 11:03:29.526545' AS DATETIME), 'admin@nhahang.com', 'Administrator', '$2a$10$StDoOUIOQ4eB/zuNonddjeYKe2o09WjuoWOIwJeSdJraus4.zjIhi', '0123456789', CAST('2025-11-04 11:03:29.528553' AS DATETIME), 'admin', NULL),
(2, 1, CAST('2025-11-04 11:07:18.646667' AS DATETIME), 'manager@restaurant.com', 'Nguyễn Văn Quản Lý', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0987654321', CAST('2025-11-04 11:07:18.646667' AS DATETIME), 'manager', NULL),
(3, 1, CAST('2025-11-04 11:07:18.646667' AS DATETIME), 'staff@restaurant.com', 'Trần Thị Nhân Viên', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0912345678', CAST('2025-11-04 11:07:18.646667' AS DATETIME), 'staff', NULL),
(4, 1, CAST('2025-11-04 11:07:18.650000' AS DATETIME), 'customer@example.com', 'Lê Văn Khách Hàng', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0909123456', CAST('2025-11-04 11:07:18.650000' AS DATETIME), 'customer', NULL),
(5, 1, CAST('2025-11-04 11:12:59.577544' AS DATETIME), 'duy@gmail.com', 'duy', '$2a$10$AWcyUut9ZC19BEQcnq9.2OOIfagyexMNHU/bKh9oKhxBiifXapo4q', '0972923552', CAST('2025-11-04 11:12:59.577544' AS DATETIME), 'duydemo', NULL);

-- Foreign Keys
ALTER TABLE `customer_feedbacks` ADD CONSTRAINT `FK1qmb4v3tgelpmbxwtowbkxjgb` FOREIGN KEY(`order_id`) REFERENCES `orders`(`id`);
ALTER TABLE `customer_feedbacks` ADD CONSTRAINT `FKc4jodevqea3h29ho0e0wa36yn` FOREIGN KEY(`customer_id`) REFERENCES `customers`(`id`);
ALTER TABLE `customer_feedbacks` ADD CONSTRAINT `FKki0bo5b5kl6gps7v8q8h2axsi` FOREIGN KEY(`resolved_by`) REFERENCES `users`(`id`);

ALTER TABLE `dishes` ADD CONSTRAINT `FKhjecv877f7mugty2rqdb97xox` FOREIGN KEY(`category_id`) REFERENCES `dish_categories`(`id`);
ALTER TABLE `dishes` ADD CONSTRAINT `FKkxnhsu0tl0vpr62mhr94y1tkx` FOREIGN KEY(`promotion_id`) REFERENCES `promotions`(`id`);

ALTER TABLE `order_details` ADD CONSTRAINT `FK1f9qgvn9fugqkk40bxkgy6g7p` FOREIGN KEY(`dish_id`) REFERENCES `dishes`(`id`);
ALTER TABLE `order_details` ADD CONSTRAINT `FKjyu2qbqt8gnvno9oe9j2s2ldk` FOREIGN KEY(`order_id`) REFERENCES `orders`(`id`);

ALTER TABLE `orders` ADD CONSTRAINT `FK8nathddqyn5kpjldujiu5lhhv` FOREIGN KEY(`table_id`) REFERENCES `restaurant_tables`(`id`);
ALTER TABLE `orders` ADD CONSTRAINT `FKbn1idyqvshli7bj1rxurlsnju` FOREIGN KEY(`reservation_id`) REFERENCES `reservations`(`id`);
ALTER TABLE `orders` ADD CONSTRAINT `FKmvji5dgxi79luuluamunmw73h` FOREIGN KEY(`room_id`) REFERENCES `rooms`(`id`);
ALTER TABLE `orders` ADD CONSTRAINT `FKpxtb8awmi0dk6smoh2vp1litg` FOREIGN KEY(`customer_id`) REFERENCES `customers`(`id`);
ALTER TABLE `orders` ADD CONSTRAINT `FKtjwuphstqm46uffgc7l1r27a9` FOREIGN KEY(`created_by`) REFERENCES `users`(`id`);

ALTER TABLE `reservations` ADD CONSTRAINT `FK7m03pg0gvesrflloshk7h40d2` FOREIGN KEY(`confirmed_by`) REFERENCES `users`(`id`);
ALTER TABLE `reservations` ADD CONSTRAINT `FK8eccffekcj27jkdiyw2e9r8ks` FOREIGN KEY(`customer_id`) REFERENCES `customers`(`id`);
ALTER TABLE `reservations` ADD CONSTRAINT `FKljt6q1tp205b0h26eiegc5mx6` FOREIGN KEY(`room_id`) REFERENCES `rooms`(`id`);
ALTER TABLE `reservations` ADD CONSTRAINT `FKlmsyoaj81pfgb83w1jhupfq0g` FOREIGN KEY(`table_id`) REFERENCES `restaurant_tables`(`id`);

ALTER TABLE `restaurant_tables` ADD CONSTRAINT `FKlsybn8s0hr5pfo45rh1brli9s` FOREIGN KEY(`room_id`) REFERENCES `rooms`(`id`);

ALTER TABLE `table_histories` ADD CONSTRAINT `FKhhjt4cgqni5icrwsnaitym4f3` FOREIGN KEY(`order_id`) REFERENCES `orders`(`id`);
ALTER TABLE `table_histories` ADD CONSTRAINT `FKndm4c1o1gj80ywv5upt4gxv7d` FOREIGN KEY(`customer_id`) REFERENCES `customers`(`id`);
ALTER TABLE `table_histories` ADD CONSTRAINT `FKomki21350myibn4fkmo5wni6l` FOREIGN KEY(`table_id`) REFERENCES `restaurant_tables`(`id`);
ALTER TABLE `table_histories` ADD CONSTRAINT `FKsk2qwjmygxwdke2x41uyb4fq8` FOREIGN KEY(`room_id`) REFERENCES `rooms`(`id`);

ALTER TABLE `user_roles` ADD CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY(`role_id`) REFERENCES `roles`(`id`);
ALTER TABLE `user_roles` ADD CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY(`user_id`) REFERENCES `users`(`id`);

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



