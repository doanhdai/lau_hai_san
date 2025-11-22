-- Script để cập nhật CHECK constraint cho cột status trong bảng dishes
-- Thêm giá trị DISCONTINUED vào constraint

USE [QuanLyNhaHangLau]
GO

-- Tìm và xóa tất cả các CHECK constraint trên cột status của bảng dishes
DECLARE @constraintName NVARCHAR(200)
DECLARE @sql NVARCHAR(MAX)

DECLARE constraint_cursor CURSOR FOR
SELECT name 
FROM sys.check_constraints 
WHERE parent_object_id = OBJECT_ID('dbo.dishes') 
  AND parent_column_id = COLUMNPROPERTY(OBJECT_ID('dbo.dishes'), 'status', 'ColumnId')

OPEN constraint_cursor
FETCH NEXT FROM constraint_cursor INTO @constraintName

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @sql = 'ALTER TABLE [dbo].[dishes] DROP CONSTRAINT [' + @constraintName + ']'
    EXEC sp_executesql @sql
    PRINT 'Đã xóa constraint: ' + @constraintName
    FETCH NEXT FROM constraint_cursor INTO @constraintName
END

CLOSE constraint_cursor
DEALLOCATE constraint_cursor
GO

-- Tạo constraint mới với DISCONTINUED
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_dishes_status')
BEGIN
    ALTER TABLE [dbo].[dishes] 
    WITH CHECK ADD CONSTRAINT [CK_dishes_status] 
    CHECK (([status]='UNAVAILABLE' OR [status]='AVAILABLE' OR [status]='DISCONTINUED'))
    PRINT 'Đã tạo constraint mới CK_dishes_status với DISCONTINUED'
END
ELSE
BEGIN
    PRINT 'Constraint CK_dishes_status đã tồn tại'
END
GO

PRINT 'Hoàn tất cập nhật constraint cho cột status của bảng dishes'
GO

