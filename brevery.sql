-- 1. EP DONG KET NOI VA XOA DATABASE CU
USE [master];
GO

IF EXISTS (SELECT * FROM sys.databases WHERE name = 'brevery')
BEGIN
    ALTER DATABASE [brevery] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [brevery];
END
GO

-- 2. TAO LAI DATABASE MOI TINH
CREATE DATABASE [brevery];
GO

USE [brevery];
GO

-- 3. TAO CAC BANG
CREATE TABLE [dbo].[categories](
    [category_id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] VARCHAR(100) NOT NULL UNIQUE,
    [description] VARCHAR(500) NULL,
    [image_url] VARCHAR(500) NULL,
    [is_active] BIT NOT NULL DEFAULT 1,
    [created_at] DATETIME2(6) NULL
);
GO

CREATE TABLE [dbo].[products](
    [product_id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [category_id] BIGINT NOT NULL FOREIGN KEY REFERENCES [dbo].[categories]([category_id]),
    [name] VARCHAR(200) NOT NULL,
    [description] TEXT NULL,
    [status] VARCHAR(20) NULL,
    [is_available] BIT NOT NULL DEFAULT 1,
    [total_sold] INT NOT NULL DEFAULT 0,
    [low_stock_threshold] INT NOT NULL DEFAULT 10,
    [created_at] DATETIME2(6) NULL,
    [updated_at] DATETIME2(6) NULL
);
GO

CREATE TABLE [dbo].[product_variants](
    [variant_id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [product_id] BIGINT NOT NULL FOREIGN KEY REFERENCES [dbo].[products]([product_id]),
    [size] VARCHAR(50) NOT NULL,
    [price] NUMERIC(12, 0) NOT NULL,
    [sale_price] NUMERIC(12, 0) NULL,
    [stock] INT NOT NULL DEFAULT 0,
    [is_available] BIT NOT NULL DEFAULT 1
);
GO

CREATE TABLE [dbo].[users](
    [user_id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [email] VARCHAR(100) NOT NULL UNIQUE,
    [password_hash] VARCHAR(255) NOT NULL,
    [full_name] VARCHAR(100) NOT NULL,
    [phone] VARCHAR(20) NULL,
    [avatar_url] VARCHAR(500) NULL,
    [role] VARCHAR(10) NOT NULL CHECK ([role] IN ('ADMIN', 'USER')),
    [is_active] BIT NOT NULL DEFAULT 1,
    [is_email_verified] BIT NOT NULL DEFAULT 0,
    [created_at] DATETIME2(6) NULL,
    [updated_at] DATETIME2(6) NULL
);
GO

CREATE TABLE [dbo].[orders](
    [order_id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [user_id] BIGINT NULL FOREIGN KEY REFERENCES [dbo].[users]([user_id]),
    [order_code] VARCHAR(20) NOT NULL UNIQUE,
    [status] VARCHAR(15) NOT NULL CHECK ([status] IN ('PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPED', 'DELIVERING', 'COMPLETED', 'CANCELLED')),
    [payment_method] VARCHAR(10) NOT NULL CHECK ([payment_method] IN ('COD', 'MOMO', 'VNPAY')),
    [payment_status] VARCHAR(10) NOT NULL CHECK ([payment_status] IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    [sub_total] NUMERIC(12, 0) NOT NULL,
    [shipping_fee] NUMERIC(12, 0) NOT NULL,
    [discount_amount] NUMERIC(12, 0) NOT NULL DEFAULT 0,
    [total_amount] NUMERIC(12, 0) NOT NULL,
    [note] VARCHAR(500) NULL,
    [tracking_token] VARCHAR(100) NULL UNIQUE,
    [created_at] DATETIME2(6) NULL,
    [updated_at] DATETIME2(6) NULL
);
GO

CREATE TABLE [dbo].[order_details](
    [detail_id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [order_id] BIGINT NOT NULL FOREIGN KEY REFERENCES [dbo].[orders]([order_id]),
    [variant_id] BIGINT NOT NULL FOREIGN KEY REFERENCES [dbo].[product_variants]([variant_id]),
    [product_name] VARCHAR(200) NOT NULL,
    [variant_info] VARCHAR(100) NOT NULL,
    [quantity] INT NOT NULL,
    [unit_price] NUMERIC(12, 0) NOT NULL,
    [sub_total] NUMERIC(12, 0) NOT NULL
);
GO

-- 4. BOM DU LIEU MAU
INSERT INTO [dbo].[categories] ([name], [description], [is_active], [created_at]) VALUES
('Banh dong hop', 'Danh muc banh hop', 1, GETDATE()),
('Nuoc uong', 'Danh muc nuoc', 1, GETDATE());
GO

INSERT INTO [dbo].[products] ([category_id], [name], [description], [is_available], [total_sold], [low_stock_threshold], [created_at]) VALUES
(1, 'Banh Dan Mach', 'Sieu ngon', 1, 150, 10, GETDATE()),
(2, 'Coca Cola', 'Giai khat', 1, 300, 10, GETDATE());
GO

INSERT INTO [dbo].[product_variants] ([product_id], [size], [price], [stock], [is_available]) VALUES
(1, 'Hop 500g', 250000, 20, 1),
(2, 'Loc 6 lon', 45000, 50, 1);
GO

INSERT INTO [dbo].[users] ([email], [password_hash], [full_name], [phone], [role], [is_active], [is_email_verified], [created_at]) VALUES
('admin@brevery.vn', '$2a$10$DowXhM1.q4mC9M5l0Q3D5.kXfSgK9X2Y6O7N0r3k7rR7P1E0T0h1y', 'Admin', '0909000111', 'ADMIN', 1, 1, GETDATE()),
('customer@gmail.com', '$2a$10$DowXhM1.q4mC9M5l0Q3D5.kXfSgK9X2Y6O7N0r3k7rR7P1E0T0h1y', 'Khach', '0909000001', 'USER', 1, 1, GETDATE());
GO

INSERT INTO [dbo].[orders] ([user_id], [order_code], [status], [payment_method], [payment_status], [sub_total], [shipping_fee], [total_amount], [created_at]) VALUES
(2, 'BRV-1700001', 'COMPLETED', 'VNPAY', 'PAID', 295000, 30000, 325000, GETDATE());
GO

INSERT INTO [dbo].[order_details] ([order_id], [variant_id], [product_name], [variant_info], [quantity], [unit_price], [sub_total]) VALUES
(1, 1, 'Banh Dan Mach', 'Hop 500g', 1, 250000, 250000),
(1, 2, 'Coca Cola', 'Loc 6 lon', 1, 45000, 45000);
GO
