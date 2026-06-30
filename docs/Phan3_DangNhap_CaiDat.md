# Hướng Dẫn Bảo Vệ Đồ Án: Phân Tích Chuyên Sâu (Đăng Nhập, Cài Đặt/Hồ Sơ)

Tài liệu này đi sâu vào kiến trúc bảo mật của module Đăng nhập (Authentication) và các logic cập nhật thông tin trong phần Cài đặt/Hồ sơ cá nhân (Settings/Profile) của dự án. 

---

## 🔐 1. TRANG ĐĂNG NHẬP (LOGIN & BẢO MẬT)
Module này là "người gác cổng" của toàn bộ hệ thống, quyết định ai được phép vào và được làm gì (Role nào).

### Tính năng 1.1: Mã hóa mật khẩu (Password Hashing)
- **Hoạt động:** 
  - Mật khẩu của người dùng (Admin, Khách hàng) không bao giờ được lưu dưới dạng văn bản thô (plaintext) như `123456` trong Database.
- **Cách code hoạt động:** 
  - Backend sử dụng thuật toán **BCrypt** để băm (hash) mật khẩu trước khi lưu vào Cơ sở dữ liệu.
  - Khi người dùng đăng nhập, Backend sẽ dùng hàm `BCrypt.check()` (hoặc cấu hình tự động của Spring Security) để so sánh mật khẩu khách nhập vào với chuỗi băm trong DB.
- **Thuật ngữ:**
  - **Salting:** Thuật toán BCrypt tự động thêm một chuỗi ký tự ngẫu nhiên (gọi là Salt) vào mật khẩu trước khi băm. Việc này giúp chống lại các cuộc tấn công bằng từ điển (Dictionary/Rainbow table attack). Dù 2 người dùng chung mật khẩu `123456`, mã băm lưu trong DB của họ vẫn hoàn toàn khác nhau.

### Tính năng 1.2: Cấp phát "Thẻ bài" JWT (JSON Web Token)
- **Hoạt động:** 
  - Sau khi kiểm tra đúng email và mật khẩu, thay vì dùng Cookie/Session truyền thống (gây nặng Server), hệ thống cấp cho trình duyệt một chuỗi Token.
- **Cách code hoạt động:** 
  - **Tạo Token:** Lớp `JwtTokenProvider` dùng Secret Key (Khóa bí mật siêu dài chỉ Backend biết) để ký tạo ra JWT. Bên trong Token này chứa thông tin cơ bản: User ID, Tên, và Quyền hạn (Role).
  - **Lưu trữ:** Frontend nhận Token từ phản hồi đăng nhập thành công và lưu vào `localStorage` (hoặc `sessionStorage`).
  - **Sử dụng:** Mỗi lần Frontend gọi các API cần quyền (VD: lấy danh sách đơn hàng), nó sẽ đính kèm Token này vào *Header* của HTTP Request (`Authorization: Bearer <token>`).

### Tính năng 1.3: Bảo vệ API bằng Spring Security
- **Hoạt động:** 
  - Ngăn chặn những người chưa đăng nhập hoặc những người không đủ thẩm quyền truy cập các đường dẫn cấm.
- **Cách code hoạt động:** 
  - Backend cấu hình Spring Security với bộ lọc `JwtAuthenticationFilter`. Bộ lọc này chặn các Request gửi tới, bóc tách Header lấy Token, kiểm tra chữ ký (Signature) và xem Token còn hạn (Expiration) không.
  - **Phân quyền (Authorization):** Các API bắt đầu bằng `/api/v1/admin/**` bắt buộc Token gửi lên phải mang quyền (Role) là `ADMIN`. Khách hàng bình thường gọi vào sẽ bị văng mã lỗi 403 (Forbidden).

---

## ⚙️ 2. TRANG CÀI ĐẶT & HỒ SƠ (SETTINGS / PROFILE)
Nơi người dùng cá nhân hóa thông tin, cập nhật bảo mật và thay đổi giao diện.

### Tính năng 2.1: Cập nhật thông tin cá nhân & Ảnh đại diện (Avatar)
- **Hoạt động:** 
  - Người dùng có thể đổi Tên, Số điện thoại, Email và cập nhật Ảnh đại diện.
- **Cách code hoạt động:** 
  - **Frontend:** Giao diện Form dùng `v-model` (Binding 2 chiều của Vue) để lấy dữ liệu. Khi đổi Avatar, Frontend gọi API upload giống hệt phần tải ảnh của Danh mục/Sản phẩm.
  - **Backend:** Nhận thông tin mới, dùng `UserRepository` để lưu đè dữ liệu tương ứng với tài khoản đang đăng nhập.
- **Thuật ngữ:**
  - **State Management (Pinia):** Khi bấm "Lưu", giao diện Frontend (nhờ có Pinia) sẽ cập nhật tên và avatar ở góc phải màn hình ngay lập tức mà không cần tải lại trang (reload).

### Tính năng 2.2: Đổi Mật Khẩu (Change Password)
- **Hoạt động:** 
  - Form yêu cầu 3 trường: "Mật khẩu hiện tại", "Mật khẩu mới", và "Xác nhận mật khẩu mới".
- **Cách code hoạt động:** 
  - Frontend tự kiểm tra (Validate) bằng Javascript xem ô "Mới" và "Xác nhận" có khớp nhau không trước khi cho phép gọi API, giảm tải cho Server.
  - Backend tiếp nhận API, dùng `BCrypt` để đối chiếu "Mật khẩu hiện tại" khách nhập xem có đúng với mật khẩu trong DB không. Nếu đúng, Backend băm (Hash) "Mật khẩu mới" và tiến hành lưu đè cập nhật vào DB.

### Tính năng 2.3: Chế độ Sáng/Tối (Dark/Light Mode)
- **Hoạt động:** 
  - Nút gạt (Toggle) cho phép người dùng đổi màu giao diện hệ thống theo sở thích hoặc theo điều kiện ánh sáng môi trường.
- **Cách code hoạt động:** 
  - Frontend Vue.js sử dụng CSS Variables (Biến CSS) để định nghĩa sẵn 2 bảng màu độc lập.
  - Khi gạt công tắc, Javascript đổi thuộc tính `data-theme="dark"` trên thẻ ngoài cùng `<html>` hoặc `<body>`. 
  - Trạng thái này được lưu vào `localStorage` của trình duyệt. Lần sau khách mở web, Vue sẽ đọc biến trong `localStorage` để tự động bật lại đúng màu sắc đã lưu.

---

## 💡 3. CÁCH TRẢ LỜI CÂU HỎI VỀ ĐĂNG NHẬP & CÀI ĐẶT:

**1. Giáo viên: "Tại sao dự án em dùng JWT (Token) mà không dùng Session (Phiên) như truyền thống?"**
> Trả lời: Dạ, dự án em thiết kế theo mô hình kiến trúc RESTful API (Frontend Vue.js và Backend Spring Boot hoàn toàn tách biệt). Việc dùng JWT giúp Backend trở nên **Stateless (Không lưu trạng thái)**. Nghĩa là Backend không phải tốn RAM để lưu trữ phiên làm việc của từng người. Điều này giúp Server giảm tải, dễ dàng nâng cấp/mở rộng (Scale) sau này, đồng thời Frontend ở một tên miền khác cũng dễ dàng gọi API mà không sợ lỗi chặn Cookie.

**2. Giáo viên: "Mật khẩu đã mã hóa BCrypt rồi, lỡ quên mật khẩu thì giải mã (Decrypt) kiểu gì để lấy lại gửi cho khách?"**
> Trả lời: Dạ thưa thầy/cô, BCrypt là thuật toán băm (Hash) **một chiều**, nó được thiết kế để không thể giải mã ngược lại được. Ngay cả Admin hay kỹ sư quản trị Database cũng không biết mật khẩu gốc là gì. Vì vậy trong các hệ thống thực tế, chức năng "Quên mật khẩu" không phải là "Lấy lại mật khẩu cũ", mà hệ thống sẽ gửi một mã OTP hoặc Link xác nhận qua Email để người dùng chứng minh danh tính, sau đó cho phép họ **Thiết lập lại một mật khẩu hoàn toàn mới**.

**3. Giáo viên: "Nếu một người lấy cắp được chuỗi JWT Token của tài khoản Admin, họ có thể phá hệ thống mãi mãi không?"**
> Trả lời: Dạ không ạ. JWT Token trong hệ thống em được cấp phát đi kèm một **Thời hạn (Expiration time)** (ví dụ: chỉ sống được vài giờ). Sau thời gian đó Token tự động vô hiệu hóa dù kẻ trộm còn giữ. Trong môi trường doanh nghiệp thực tế, người ta còn kết hợp thêm cơ chế Refresh Token ngắn hạn và dài hạn, hoặc gắn Token vào HttpOnly Cookie để tránh bị script lấy cắp (Tấn công XSS).

**4. Giáo viên: "Trong phần Cài đặt Hồ sơ, tại sao khi vừa bấm lưu Tên/Ảnh mới, cái thanh menu ở tít góc phải màn hình đổi theo liền mà trang web không hề nháy (tải lại - F5)?"**
> Trả lời: Dạ đó là nhờ sức mạnh của **Vue.js kết hợp với Pinia**. Pinia là một kho quản lý trạng thái tập trung (Global State). Khi API đổi tên chạy thành công, Frontend lập tức ghi đè dữ liệu mới vào biến trong kho Pinia. Vì các thành phần như Menu, Header đang "đăng ký theo dõi" (Reactivity) biến này, nên Vue sẽ tự động kích hoạt tính năng DOM ảo (Virtual DOM) để vẽ lại những khu vực đó ngay lập tức mà không cần F5 trình duyệt.
