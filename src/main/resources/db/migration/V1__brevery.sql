-- ============================================================
-- Bakery & Beverage E-commerce — V1 Init Schema
-- SQL Server 2019
-- ============================================================

-- ==================== AUTH ====================

CREATE TABLE Users (
    UserId       BIGINT IDENTITY(1,1) PRIMARY KEY,
    Email        NVARCHAR(100) NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    FullName     NVARCHAR(100) NOT NULL,
    Phone        NVARCHAR(20)  NULL,
    AvatarUrl    NVARCHAR(500) NULL,
    Role         NVARCHAR(10)  NOT NULL DEFAULT 'USER',
    IsActive     BIT NOT NULL DEFAULT 1,
    IsEmailVerified BIT NOT NULL DEFAULT 0,
    CreatedAt    DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt    DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Users_Email UNIQUE (Email)
);

CREATE TABLE RefreshTokens (
    TokenId   BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId    BIGINT NOT NULL,
    Token     NVARCHAR(500) NOT NULL,
    ExpiresAt DATETIME2 NOT NULL,
    IsRevoked BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_RefreshTokens_Token UNIQUE (Token),
    CONSTRAINT FK_RefreshTokens_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
);

CREATE TABLE EmailVerifications (
    Id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId    BIGINT NOT NULL,
    Token     NVARCHAR(500) NOT NULL,
    Type      NVARCHAR(20)  NOT NULL,
    ExpiresAt DATETIME2 NOT NULL,
    IsUsed    BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_EmailVerifications_Token UNIQUE (Token),
    CONSTRAINT FK_EmailVerifications_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
);

-- ==================== ADDRESS ====================

CREATE TABLE UserAddresses (
    AddressId     BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId        BIGINT NOT NULL,
    RecipientName NVARCHAR(100) NOT NULL,
    Phone         NVARCHAR(20)  NOT NULL,
    Province      NVARCHAR(50)  NOT NULL,
    District      NVARCHAR(50)  NOT NULL,
    Ward          NVARCHAR(50)  NOT NULL,
    AddressDetail NVARCHAR(255) NOT NULL,
    IsDefault     BIT NOT NULL DEFAULT 0,
    CreatedAt     DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_UserAddresses_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
);

-- ==================== PRODUCT ====================

CREATE TABLE Categories (
    CategoryId  BIGINT IDENTITY(1,1) PRIMARY KEY,
    Name        NVARCHAR(100) NOT NULL,
    Description NVARCHAR(500) NULL,
    ImageUrl    NVARCHAR(500) NULL,
    IsActive    BIT NOT NULL DEFAULT 1,
    CreatedAt   DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Categories_Name UNIQUE (Name)
);

CREATE TABLE Products (
    ProductId   BIGINT IDENTITY(1,1) PRIMARY KEY,
    CategoryId  BIGINT NOT NULL,
    Name        NVARCHAR(200) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    IsAvailable BIT NOT NULL DEFAULT 1,
    TotalSold   INT NOT NULL DEFAULT 0,
    CreatedAt   DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt   DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Products_Categories FOREIGN KEY (CategoryId) REFERENCES Categories(CategoryId)
);

CREATE TABLE ProductVariants (
    VariantId   BIGINT IDENTITY(1,1) PRIMARY KEY,
    ProductId   BIGINT NOT NULL,
    Size        NVARCHAR(50)  NOT NULL,
    Price       DECIMAL(12,0) NOT NULL,
    Stock       INT NOT NULL DEFAULT 0,
    IsAvailable BIT NOT NULL DEFAULT 1,

    CONSTRAINT FK_ProductVariants_Products FOREIGN KEY (ProductId) REFERENCES Products(ProductId) ON DELETE CASCADE
);

CREATE TABLE ProductImages (
    ImageId   BIGINT IDENTITY(1,1) PRIMARY KEY,
    ProductId BIGINT NOT NULL,
    ImageUrl  NVARCHAR(500) NOT NULL,
    IsPrimary BIT NOT NULL DEFAULT 0,
    SortOrder INT DEFAULT 0,

    CONSTRAINT FK_ProductImages_Products FOREIGN KEY (ProductId) REFERENCES Products(ProductId) ON DELETE CASCADE
);

CREATE TABLE Reviews (
    ReviewId  BIGINT IDENTITY(1,1) PRIMARY KEY,
    ProductId BIGINT NOT NULL,
    UserId    BIGINT NOT NULL,
    OrderId   BIGINT NOT NULL,
    Rating    INT NOT NULL,
    Comment   NVARCHAR(1000) NULL,
    IsVisible BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Reviews_Products FOREIGN KEY (ProductId) REFERENCES Products(ProductId),
    CONSTRAINT FK_Reviews_Users    FOREIGN KEY (UserId)    REFERENCES Users(UserId),
    CONSTRAINT CK_Reviews_Rating   CHECK (Rating >= 1 AND Rating <= 5)
);

-- ==================== CART ====================

CREATE TABLE CartItems (
    CartItemId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId     BIGINT NOT NULL,
    VariantId  BIGINT NOT NULL,
    Quantity   INT NOT NULL,
    CreatedAt  DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_CartItems_Users    FOREIGN KEY (UserId)    REFERENCES Users(UserId) ON DELETE CASCADE,
    CONSTRAINT FK_CartItems_Variants FOREIGN KEY (VariantId) REFERENCES ProductVariants(VariantId),
    CONSTRAINT UQ_CartItems_User_Variant UNIQUE (UserId, VariantId)
);

-- ==================== COUPON ====================

CREATE TABLE Coupons (
    CouponId      BIGINT IDENTITY(1,1) PRIMARY KEY,
    Code          NVARCHAR(30) NOT NULL,
    DiscountType  NVARCHAR(10) NOT NULL,
    DiscountValue DECIMAL(12,2) NOT NULL,
    MaxDiscount   DECIMAL(12,0) NULL,
    MinOrderAmount DECIMAL(12,0) NULL,
    UsageLimit    INT NOT NULL DEFAULT 0,
    UsedCount     INT NOT NULL DEFAULT 0,
    ExpiryDate    DATETIME2 NOT NULL,
    IsActive      BIT NOT NULL DEFAULT 1,
    CreatedAt     DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Coupons_Code UNIQUE (Code)
);

-- ==================== ORDER ====================

CREATE TABLE Orders (
    OrderId       BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId        BIGINT NULL,
    OrderCode     NVARCHAR(20) NOT NULL,
    SubTotal      DECIMAL(12,0) NOT NULL,
    ShippingFee   DECIMAL(12,0) NOT NULL DEFAULT 0,
    DiscountAmount DECIMAL(12,0) NOT NULL DEFAULT 0,
    TotalAmount   DECIMAL(12,0) NOT NULL,
    CouponId      BIGINT NULL,
    Status        NVARCHAR(15) NOT NULL DEFAULT 'PENDING',
    PaymentMethod NVARCHAR(10) NOT NULL,
    PaymentStatus NVARCHAR(10) NOT NULL DEFAULT 'PENDING',
    Note          NVARCHAR(500) NULL,
    TrackingToken NVARCHAR(100) NULL,
    CreatedAt     DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt     DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT UQ_Orders_OrderCode UNIQUE (OrderCode),
    CONSTRAINT UQ_Orders_TrackingToken UNIQUE (TrackingToken),
    CONSTRAINT FK_Orders_Users   FOREIGN KEY (UserId)   REFERENCES Users(UserId),
    CONSTRAINT FK_Orders_Coupons FOREIGN KEY (CouponId) REFERENCES Coupons(CouponId)
);

-- Thêm FK cho Reviews → Orders (sau khi Orders đã tạo)
ALTER TABLE Reviews ADD CONSTRAINT FK_Reviews_Orders FOREIGN KEY (OrderId) REFERENCES Orders(OrderId);

CREATE TABLE OrderDetails (
    DetailId    BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderId     BIGINT NOT NULL,
    VariantId   BIGINT NOT NULL,
    ProductName NVARCHAR(200) NOT NULL,
    VariantInfo NVARCHAR(100) NOT NULL,
    Quantity    INT NOT NULL,
    UnitPrice   DECIMAL(12,0) NOT NULL,
    SubTotal    DECIMAL(12,0) NOT NULL,

    CONSTRAINT FK_OrderDetails_Orders   FOREIGN KEY (OrderId)   REFERENCES Orders(OrderId) ON DELETE CASCADE,
    CONSTRAINT FK_OrderDetails_Variants FOREIGN KEY (VariantId) REFERENCES ProductVariants(VariantId)
);

CREATE TABLE ShippingDetails (
    ShippingId    BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderId       BIGINT NOT NULL,
    RecipientName NVARCHAR(100) NOT NULL,
    Phone         NVARCHAR(20) NOT NULL,
    Province      NVARCHAR(50) NOT NULL,
    District      NVARCHAR(50) NOT NULL,
    Ward          NVARCHAR(50) NOT NULL,
    AddressDetail NVARCHAR(255) NOT NULL,

    CONSTRAINT UQ_ShippingDetails_OrderId UNIQUE (OrderId),
    CONSTRAINT FK_ShippingDetails_Orders FOREIGN KEY (OrderId) REFERENCES Orders(OrderId) ON DELETE CASCADE
);

-- ==================== COUPON USAGE ====================

CREATE TABLE UserCouponUsage (
    Id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId   BIGINT NOT NULL,
    CouponId BIGINT NOT NULL,
    OrderId  BIGINT NOT NULL,
    UsedAt   DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_UserCouponUsage_Users   FOREIGN KEY (UserId)   REFERENCES Users(UserId),
    CONSTRAINT FK_UserCouponUsage_Coupons FOREIGN KEY (CouponId) REFERENCES Coupons(CouponId),
    CONSTRAINT FK_UserCouponUsage_Orders  FOREIGN KEY (OrderId)  REFERENCES Orders(OrderId),
    CONSTRAINT UQ_UserCouponUsage UNIQUE (UserId, CouponId)
);

-- ==================== NOTIFICATION ====================

CREATE TABLE Notifications (
    NotificationId BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserId         BIGINT NULL,
    Title          NVARCHAR(200) NOT NULL,
    Message        NVARCHAR(1000) NOT NULL,
    Type           NVARCHAR(50) NULL,
    ReferenceId    NVARCHAR(50) NULL,
    IsRead         BIT NOT NULL DEFAULT 0,
    CreatedAt      DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Notifications_Users FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
);

-- ==================== CONTACT ====================

CREATE TABLE ContactMessages (
    MessageId BIGINT IDENTITY(1,1) PRIMARY KEY,
    Name      NVARCHAR(100) NOT NULL,
    Email     NVARCHAR(100) NOT NULL,
    Phone     NVARCHAR(20) NULL,
    Subject   NVARCHAR(200) NOT NULL,
    Message   NVARCHAR(2000) NOT NULL,
    IsRead    BIT NOT NULL DEFAULT 0,
    RepliedAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- ==================== INDEXES ====================

CREATE INDEX IX_Users_Email           ON Users(Email);
CREATE INDEX IX_Users_Role            ON Users(Role);
CREATE INDEX IX_Products_CategoryId   ON Products(CategoryId);
CREATE INDEX IX_Products_IsAvailable  ON Products(IsAvailable);
CREATE INDEX IX_Products_TotalSold    ON Products(TotalSold DESC);
CREATE INDEX IX_ProductVariants_ProductId ON ProductVariants(ProductId);
CREATE INDEX IX_Reviews_ProductId     ON Reviews(ProductId);
CREATE INDEX IX_Reviews_UserId        ON Reviews(UserId);
CREATE INDEX IX_CartItems_UserId      ON CartItems(UserId);
CREATE INDEX IX_Orders_UserId         ON Orders(UserId);
CREATE INDEX IX_Orders_Status         ON Orders(Status);
CREATE INDEX IX_Orders_CreatedAt      ON Orders(CreatedAt DESC);
CREATE INDEX IX_Orders_TrackingToken  ON Orders(TrackingToken);
CREATE INDEX IX_OrderDetails_OrderId  ON OrderDetails(OrderId);
CREATE INDEX IX_Notifications_UserId  ON Notifications(UserId);
CREATE INDEX IX_Notifications_IsRead  ON Notifications(IsRead);
CREATE INDEX IX_Coupons_Code          ON Coupons(Code);
CREATE INDEX IX_RefreshTokens_UserId  ON RefreshTokens(UserId);
CREATE INDEX IX_RefreshTokens_Token   ON RefreshTokens(Token);
