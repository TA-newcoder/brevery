# Cấu Trúc và Luồng Logic Chức Năng Backend (Dự án Brevery)

Tài liệu này giúp bạn hình dung rõ ràng luồng đi của dữ liệu (Data Flow) từ ngoài Frontend vào đến tận Database và đi ra. Biểu diễn bằng các mũi tên chuyển tiếp (➔) giữa các Class để bạn dễ dàng follow code.

---

## 1. Chức năng Đăng nhập (Login Flow)
*Luồng khi người dùng nhập Email/Mật khẩu để lấy Token đăng nhập.*

**Luồng đi (Flow):**
`Client` ➔ `AuthController` ➔ `AuthService` ➔ `UserRepository` ➔ `JwtTokenProvider` ➔ `AuthController` ➔ `Client`

**Chi tiết nhiệm vụ từng Class trên luồng:**
1. **`LoginRequest` (DTO):** Gói dữ liệu chứa `email` và `password` do Client gửi lên.
2. **`AuthController`:** Tiếp nhận request tại API `/api/v1/auth/login`. Kiểm tra tính hợp lệ ban đầu (validate) và gọi hàm của `AuthService`.
3. **`AuthService` (Interface) ➔ `AuthServiceImpl` (Class):** Xử lý logic cốt lõi. So sánh mật khẩu băm, kiểm tra trạng thái tài khoản (bị khóa hay không).
4. **`UserRepository`:** Được `AuthService` gọi để truy vấn Database (ví dụ: `findByEmail`).
5. **`JwtTokenProvider`:** Nếu mật khẩu đúng, `AuthService` sẽ nhờ class này để ký (generate) một chuỗi mã hóa JWT (Access Token & Refresh Token).
6. **`AuthResponse` (DTO):** `AuthService` gói Token vào DTO này, trả về cho `AuthController` để xuất ra cho Client.

---

## 2. Chức năng Xem danh sách & Tìm kiếm Sản phẩm
*Luồng khi người dùng truy cập trang sản phẩm, tìm kiếm từ khóa hoặc lọc theo giá.*

**Luồng đi (Flow):**
`Client` ➔ `ProductController` ➔ `ProductService` ➔ `ProductRepository` ➔ `ProductMapper` ➔ `ProductController` ➔ `Client`

**Chi tiết nhiệm vụ từng Class trên luồng:**
1. **`ProductFilterRequest` (DTO):** Client gửi lên các tham số như (từ khóa, khoảng giá, danh mục, trang số mấy).
2. **`ProductController`:** Nhận request tại API `/api/v1/products`, chuyển bộ lọc xuống Service.
3. **`ProductService` ➔ `ProductServiceImpl`:** Tính toán các điều kiện lọc, chuẩn bị đối tượng phân trang (Pageable) của Spring Data.
4. **`ProductRepository`:** Nhận lệnh từ Service để query DB (thường dùng `Specification` hoặc `@Query` để lọc động). Trả về danh sách Entity `Product`.
5. **`ProductMapper`:** `Service` lấy danh sách Entity `Product` (chứa dữ liệu thô) và nhờ Mapper chuyển hóa thành `ProductListDTO` (chỉ chứa các trường cần hiển thị lên giao diện).
6. **`Page<ProductListDTO>`:** Kết quả cuối cùng trả về cho Frontend.

---

## 3. Chức năng Gợi ý Sản phẩm (Product Recommendation)
*Luồng khi hệ thống gợi ý các sản phẩm "thường được mua cùng" hoặc "bán chạy".*

**Luồng đi (Flow):**
`Client` ➔ `ProductController` ➔ `RecommendationService` ➔ `OrderRepository` / `ProductRepository` ➔ `ProductMapper` ➔ `Client`

**Chi tiết nhiệm vụ từng Class trên luồng:**
1. **`ProductController`:** Gọi API `/api/v1/products/recommendations/...`
2. **`RecommendationService` ➔ `RecommendationServiceImpl`:** Xử lý thuật toán/logic tìm sản phẩm (VD: Đếm số lần 2 sản phẩm xuất hiện trong cùng 1 đơn hàng).
3. **`OrderRepository`:** Truy xuất lịch sử mua hàng để tính toán gợi ý.
4. **`ProductRepository`:** Lấy thông tin chi tiết của các sản phẩm được gợi ý.
5. **`ProductMapper`:** Chuyển đổi dữ liệu Entity thành `ProductListDTO` để trả về cho người dùng.

---

## 4. Chức năng Đặt hàng (Checkout Flow)
*Luồng khi người dùng bấm "Thanh toán" để tạo đơn hàng mới.*

**Luồng đi (Flow):**
`Client` ➔ `OrderController` ➔ `OrderService` ➔ `ProductRepository` (Trừ tồn kho) ➔ `OrderRepository` (Lưu đơn) ➔ `OrderMapper` ➔ `Client`

**Chi tiết nhiệm vụ từng Class trên luồng:**
1. **`OrderCreateRequest` (DTO):** Client gửi thông tin nhận hàng, phương thức thanh toán, danh sách sản phẩm mua.
2. **`OrderController`:** Nhận API `/api/v1/orders`.
3. **`OrderService` ➔ `OrderServiceImpl`:** 
   - Gọi `ProductRepository` để check xem trong kho còn đủ hàng không? Nếu đủ thì trừ số lượng tồn kho.
   - Tính tổng tiền (áp dụng mã giảm giá nếu có).
   - Tạo ra các đối tượng Entity `Order` (Đơn hàng chính) và `OrderDetail` (Chi tiết từng món).
4. **`OrderRepository` & `OrderDetailRepository`:** Lưu `Order` và các `OrderDetail` xuống Database.
5. **`OrderMapper`:** Chuyển Entity `Order` vừa lưu thành `OrderResponseDTO` gửi thông báo thành công về cho Frontend.

---

## 5. Chức năng Giỏ hàng (Cart Flow)
*Luồng khi người dùng bấm "Thêm vào giỏ hàng".*

**Luồng đi (Flow):**
`Client` ➔ `CartController` ➔ `CartService` ➔ `ProductRepository` (Kiểm tra kho) ➔ `CartRepository` ➔ `Client`

**Chi tiết nhiệm vụ từng Class trên luồng:**
1. **`CartItemRequest` (DTO):** Gửi lên `productId` và `quantity`.
2. **`CartController`:** Tiếp nhận API `/api/v1/cart`.
3. **`CartService` ➔ `CartServiceImpl`:** Gọi `ProductRepository` kiểm tra xem sản phẩm có tồn tại và còn hàng không.
4. **`CartRepository` & `CartItemRepository`:** Kiểm tra xem món đó đã có trong giỏ chưa. Nếu có thì cộng dồn số lượng, nếu chưa thì tạo dòng mới. Lưu vào Database.
5. **`CartResponseDTO`:** Trả về giỏ hàng mới nhất để Frontend cập nhật số lượng (Badge đỏ góc trên màn hình).

---

## 6. Chức năng Thống kê Doanh thu (Admin Analytics)
*Luồng cho trang Dashboard của Quản trị viên.*

**Luồng đi (Flow):**
`Admin Client` ➔ `AdminAnalyticsController` ➔ `AnalyticsService` ➔ `OrderRepository` ➔ `AnalyticsDTO` ➔ `Admin Client`

**Chi tiết nhiệm vụ từng Class trên luồng:**
1. **`AdminAnalyticsController`:** Gọi API lấy thống kê theo tháng/năm.
2. **`AnalyticsService`:** Xử lý logic tính toán tổng doanh thu, đếm số đơn hàng trạng thái COMPLETED.
3. **`OrderRepository`:** Thực thi các câu lệnh SQL gom nhóm (GROUP BY) theo ngày/tháng để lấy số liệu tổng kết.
4. Xây dựng đối tượng **DTO Thống kê** trả về dạng mảng/dữ liệu vẽ biểu đồ.

---

### Tóm tắt để bạn dễ nhớ:
- **X (Request/DTO)**: Thùng hàng từ khách chở tới nhà máy.
- **Controller**: Trạm gác cổng nhà máy (nhận hàng, kiểm tra tem nhãn cơ bản). Chuyển vào xưởng.
- **Service**: Xưởng gia công/logic (quyết định làm gì với đơn hàng, lắp ráp, cắt gọt).
- **Repository**: Kho chứa hàng (Giao tiếp trực tiếp với kệ hàng - Database).
- **Mapper**: Bộ phận đóng gói (Bọc hàng thô từ kho thành hàng đẹp DTO trước khi gửi ra ngoài).
- **Y (Response/DTO)**: Sản phẩm hoàn thiện trả lại cho khách.
