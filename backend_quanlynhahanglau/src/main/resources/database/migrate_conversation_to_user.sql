-- Migration script: Tạo lại bảng conversations và chat_messages với cấu trúc mới
-- Mỗi user sẽ có 1 conversation với system (admin ID = 1)
-- Logic: 1 user = 1 conversation với hệ thống

-- ========== FOR MYSQL ==========

-- Bước 1: Tắt foreign key checks để xóa bảng dễ dàng
SET FOREIGN_KEY_CHECKS = 0;

-- Bước 2: Xóa các bảng cũ nếu tồn tại (đã xóa rồi nhưng để chắc chắn)
DROP TABLE IF EXISTS `chat_messages`;
DROP TABLE IF EXISTS `conversations`;

-- Bước 3: Tạo lại bảng conversations với cấu trúc mới
CREATE TABLE `conversations`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`user_id` BIGINT NOT NULL COMMENT 'User ID (mỗi user có 1 conversation với system)',
	`user_name` VARCHAR(100) NOT NULL COMMENT 'Tên user (để hiển thị nhanh)',
	`customer_id` BIGINT NULL COMMENT 'Customer ID (nếu có, để lấy thông tin chi tiết)',
	`customer_name` VARCHAR(100) NULL COMMENT 'Tên customer (nếu có)',
	`is_active` TINYINT(1) DEFAULT 1 COMMENT 'Cuộc trò chuyện còn hoạt động không',
	`last_message_at` DATETIME(6) NULL COMMENT 'Thời gian tin nhắn cuối cùng',
	`unread_count` BIGINT DEFAULT 0 COMMENT 'Số tin nhắn chưa đọc từ user (cho staff/admin)',
	`created_at` DATETIME(6) NOT NULL,
	`updated_at` DATETIME(6) NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_user_conversation` (`user_id`) COMMENT 'Mỗi user chỉ có 1 conversation',
	INDEX `idx_user_id` (`user_id`),
	INDEX `idx_last_message_at` (`last_message_at`),
	INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu các cuộc trò chuyện giữa user và system (admin)';

-- Bước 4: Tạo lại bảng chat_messages
CREATE TABLE `chat_messages`(
	`id` BIGINT AUTO_INCREMENT NOT NULL,
	`conversation_id` BIGINT NOT NULL COMMENT 'ID của conversation',
	`sender_type` VARCHAR(20) NOT NULL COMMENT 'SYSTEM (staff/admin) hoặc CUSTOMER (user)',
	`sender_id` BIGINT NOT NULL COMMENT 'ID của User (nếu SYSTEM) hoặc User ID (nếu CUSTOMER)',
	`sender_name` VARCHAR(100) NULL COMMENT 'Tên người gửi (để hiển thị nhanh)',
	`content` VARCHAR(2000) NOT NULL COMMENT 'Nội dung tin nhắn',
	`is_read` TINYINT(1) DEFAULT 0 COMMENT 'Đã đọc chưa',
	`read_at` DATETIME(6) NULL COMMENT 'Thời gian đọc',
	`created_at` DATETIME(6) NOT NULL,
	`sender_user_id` BIGINT NULL COMMENT 'User ID của người gửi (luôn có giá trị)',
	PRIMARY KEY (`id`),
	INDEX `idx_conversation_id` (`conversation_id`),
	INDEX `idx_sender` (`sender_type`, `sender_id`),
	INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu các tin nhắn trong conversations';

-- Bước 5: Thêm foreign keys
-- Foreign key cho conversations.user_id -> users.id
ALTER TABLE `conversations` 
ADD CONSTRAINT `FK_conversation_user` 
FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE;

-- Foreign key cho conversations.customer_id -> customers.id (nullable)
ALTER TABLE `conversations` 
ADD CONSTRAINT `FK_conversation_customer` 
FOREIGN KEY (`customer_id`) REFERENCES `customers`(`id`) ON DELETE SET NULL;

-- Foreign key cho chat_messages.conversation_id -> conversations.id
ALTER TABLE `chat_messages` 
ADD CONSTRAINT `FK_chat_message_conversation` 
FOREIGN KEY (`conversation_id`) REFERENCES `conversations`(`id`) ON DELETE CASCADE;

-- Foreign key cho chat_messages.sender_user_id -> users.id
ALTER TABLE `chat_messages` 
ADD CONSTRAINT `FK_chat_sender_user` 
FOREIGN KEY (`sender_user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL;

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ========== FOR SQL SERVER ==========
/*
-- Bước 1: Xóa các bảng cũ nếu tồn tại
IF OBJECT_ID(N'[dbo].[chat_messages]', 'U') IS NOT NULL
    DROP TABLE [dbo].[chat_messages];
GO

IF OBJECT_ID(N'[dbo].[conversations]', 'U') IS NOT NULL
    DROP TABLE [dbo].[conversations];
GO

-- Bước 2: Tạo lại bảng conversations
CREATE TABLE [dbo].[conversations](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NOT NULL,
	[user_name] [nvarchar](100) NOT NULL,
	[customer_id] [bigint] NULL,
	[customer_name] [nvarchar](100) NULL,
	[is_active] [bit] NULL DEFAULT 1,
	[last_message_at] [datetime2](6) NULL,
	[unread_count] [bigint] NULL DEFAULT 0,
	[created_at] [datetime2](6) NOT NULL,
	[updated_at] [datetime2](6) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
CONSTRAINT [uk_user_conversation] UNIQUE NONCLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [idx_conversation_user_id] ON [dbo].[conversations]
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [IDX_conversations_last_message_at] ON [dbo].[conversations]
(
	[last_message_at] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [idx_is_active] ON [dbo].[conversations]
(
	[is_active] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

-- Bước 3: Tạo lại bảng chat_messages
CREATE TABLE [dbo].[chat_messages](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[conversation_id] [bigint] NOT NULL,
	[sender_type] [varchar](20) NOT NULL,
	[sender_id] [bigint] NOT NULL,
	[sender_name] [varchar](100) NULL,
	[content] [varchar](2000) NOT NULL,
	[is_read] [bit] NULL DEFAULT 0,
	[read_at] [datetime2](6) NULL,
	[created_at] [datetime2](6) NOT NULL,
	[sender_user_id] [bigint] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [idx_conversation_id] ON [dbo].[chat_messages]
(
	[conversation_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [idx_sender] ON [dbo].[chat_messages]
(
	[sender_type] ASC,
	[sender_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [idx_created_at] ON [dbo].[chat_messages]
(
	[created_at] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

-- Bước 4: Thêm foreign keys
ALTER TABLE [dbo].[conversations]  WITH CHECK ADD  CONSTRAINT [FK_conversation_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([id]) ON DELETE CASCADE
GO
ALTER TABLE [dbo].[conversations] CHECK CONSTRAINT [FK_conversation_user]
GO

ALTER TABLE [dbo].[conversations]  WITH CHECK ADD  CONSTRAINT [FK_conversation_customer] FOREIGN KEY([customer_id])
REFERENCES [dbo].[customers] ([id]) ON DELETE SET NULL
GO
ALTER TABLE [dbo].[conversations] CHECK CONSTRAINT [FK_conversation_customer]
GO

ALTER TABLE [dbo].[chat_messages]  WITH CHECK ADD  CONSTRAINT [FK_chat_message_conversation] FOREIGN KEY([conversation_id])
REFERENCES [dbo].[conversations] ([id]) ON DELETE CASCADE
GO
ALTER TABLE [dbo].[chat_messages] CHECK CONSTRAINT [FK_chat_message_conversation]
GO

ALTER TABLE [dbo].[chat_messages]  WITH CHECK ADD  CONSTRAINT [FK_chat_sender_user] FOREIGN KEY([sender_user_id])
REFERENCES [dbo].[users] ([id]) ON DELETE SET NULL
GO
ALTER TABLE [dbo].[chat_messages] CHECK CONSTRAINT [FK_chat_sender_user]
GO
*/
