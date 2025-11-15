-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.30 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.1.0.6537
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for quanlynhahanglau
CREATE DATABASE IF NOT EXISTS `quanlynhahanglau` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `quanlynhahanglau`;

-- Dumping structure for table quanlynhahanglau.restaurant_tables
CREATE TABLE IF NOT EXISTS `restaurant_tables` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) DEFAULT '1',
  `capacity` int NOT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `table_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `room_id` bigint DEFAULT NULL,
  `position_x` int DEFAULT NULL,
  `position_y` int DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT b'0',
  `type` enum('OFFLINE','ONLINE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1t9vy8tg8eidmfw4k887ibeu1` (`table_number`),
  KEY `FKlsybn8s0hr5pfo45rh1brli9s` (`room_id`),
  CONSTRAINT `FKlsybn8s0hr5pfo45rh1brli9s` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table quanlynhahanglau.restaurant_tables: ~11 rows (approximately)
INSERT INTO `restaurant_tables` (`id`, `active`, `capacity`, `location`, `notes`, `status`, `table_number`, `room_id`, `position_x`, `position_y`, `is_deleted`, `type`) VALUES
	(1, 1, 4, 'Tầng 1 - Khu A', 'Bàn gần cửa sổ', 'OCCUPIED', 'T-01', NULL, 50, 50, b'0', 'ONLINE'),
	(2, 1, 4, 'Tầng 1 - Khu A', NULL, 'OCCUPIED', 'T-02', NULL, 200, 50, b'0', 'ONLINE'),
	(3, 1, 6, 'Tầng 1 - Khu B', NULL, 'OCCUPIED', 'T-03', NULL, 350, 50, b'0', 'ONLINE'),
	(4, 1, 6, 'Tầng 1 - Khu B', NULL, 'AVAILABLE', 'T-04', NULL, 500, 50, b'0', 'ONLINE'),
	(5, 1, 8, 'Tầng 1 - Khu C', 'Bàn lớn', 'AVAILABLE', 'T-05', NULL, 650, 50, b'0', 'ONLINE'),
	(6, 1, 2, 'Tầng 2 - Khu A', 'Bàn đôi', 'AVAILABLE', 'T-06', NULL, 50, 300, b'0', 'ONLINE'),
	(7, 1, 2, 'Tầng 2 - Khu A', 'Bàn đôi', 'AVAILABLE', 'T-07', NULL, 200, 300, b'0', 'ONLINE'),
	(8, 1, 4, 'Tầng 2 - Khu B', NULL, 'RESERVED', 'T-08', NULL, 350, 300, b'0', 'ONLINE'),
	(9, 1, 4, 'Tầng 2 - Khu B', NULL, 'AVAILABLE', 'T-09', NULL, 500, 300, b'0', 'OFFLINE'),
	(10, 1, 6, 'Tầng 2 - Khu C', NULL, 'AVAILABLE', 'T-10', NULL, 650, 300, b'0', 'OFFLINE'),
	(15, 1, 4, NULL, NULL, 'AVAILABLE', '132', NULL, NULL, NULL, b'1', 'OFFLINE');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
