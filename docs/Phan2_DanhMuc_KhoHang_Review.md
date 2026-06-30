# Hướng Dẫn Bảo Vệ Đồ Án: Phân Tích Chuyên Sâu (Danh Mục, Kho Hàng, Đánh Giá)

Tài liệu này tập trung vào 3 module: Danh mục sản phẩm, Kho hàng (Inventory) và Đánh giá của khách hàng. Đi kèm là phần giải thích công nghệ cốt lõi và các thuật ngữ chuyên ngành để bạn tự tin bảo vệ.

---

## 💻 1. CÔNG NGHỆ (FRAMEWORKS) SỬ DỤNG TRONG DỰ ÁN
Khi được hỏi về cấu trúc hệ thống, bạn cần liệt kê các công nghệ sau:

**Khối Frontend (Giao diện người dùng):**

- **Vue.js 3:** 
  - Đây là một *Framework* (Bộ khung lập trình được dựng sẵn) dùng để xây dựng giao diện. 
  - Nó chia trang web thành các *Component* (Các thành phần độc lập, tái sử dụng được như Nút bấm, Bảng, Biểu đồ). 
  - Dự án dùng cú pháp mới nhất là *Composition API* (Cách viết code ngắn gọn và logic hơn).

- **Vite:** 
  - *Build tool* (Công cụ biên dịch và đóng gói code). 
  - Nó giúp dịch code từ Vue sang HTML/CSS/JS thuần để trình duyệt đọc được với tốc độ cực kỳ nhanh.

- **Pinia:** 
  - *State Management* (Trình quản lý trạng thái). 
  - Đây là một cái kho lưu trữ dữ liệu dùng chung cho toàn bộ trang web (ví dụ: Lưu thông tin xem Admin tên là gì, đã đăng nhập chưa, để không phải gọi API liên tục).

- **Chart.js:** 
  - Thư viện Javascript chuyên dụng để vẽ biểu đồ trực quan (tròn, cột, đường...).

**Khối Backend (Hệ thống xử lý & Cơ sở dữ liệu):**

- **Spring Boot 3 (Java 21):** 
  - *Framework Backend* mạnh mẽ nhất của ngôn ngữ Java.
  - Giúp tạo ra các *RESTful API* (Cổng giao tiếp để Frontend gọi xuống lấy dữ liệu).

- **Spring Data JPA & Hibernate:** 
  - *ORM (Object-Relational Mapping)* - Kỹ thuật ánh xạ dữ liệu. 
  - Thay vì phải viết những câu lệnh SQL dài dòng thủ công trong code Java, JPA giúp chuyển đổi các đối tượng (Object) trong Java thành các dòng trong bảng Database tự động.

- **Microsoft SQL Server:** 
  - Hệ quản trị Cơ sở dữ liệu quan hệ (Lưu dữ liệu dưới dạng bảng có liên kết với nhau).

- **Spring Security & JWT (JSON Web Token):** 
  - Hệ thống bảo vệ web. 
  - *JWT* là một chuỗi mã hóa đóng vai trò như "Vé vào cổng" (Token). Khi đăng nhập đúng, backend cấp cho 1 vé JWT, Frontend cầm vé này để đi qua các API bảo mật.

---

## 📂 2. TRANG QUẢN LÝ DANH MỤC SẢN PHẨM (CATEGORIES)
Nơi Admin tạo và phân loại các nhóm sản phẩm (Ví dụ: Bánh mặn, Bánh ngọt, Nước uống).

### Tính năng 1.1: Quản lý danh mục (CRUD & Upload ảnh)
- **Hoạt động:** 
  - Admin có thể Thêm, Sửa tên, Mô tả và Tải ảnh đại diện cho từng danh mục.
- **Cách code hoạt động:** 
  - Khi Admin chọn ảnh từ máy tính, Frontend gọi API upload file (`/upload`) để đưa ảnh lên Cloud hoặc máy chủ. Backend trả về một đường link ảnh (URL). Frontend lấy URL này ghép chung với thông tin danh mục để lưu vào Database.
- **Thuật ngữ:**
  - **Base64 vs URL:** Tránh lưu trực tiếp ảnh dưới dạng chuỗi Base64 vào Database vì sẽ làm phình to CSDL và làm chậm tốc độ truy vấn. Phương pháp chuẩn là lưu ảnh thành file cứng rồi lưu đường dẫn URL vào Database.

### Tính năng 1.2: Ẩn / Hiện danh mục (Soft Delete)
- **Hoạt động:** 
  - Admin có thể bấm nút hình "con mắt" để tạm ẩn danh mục đi mà không cần xóa hẳn.
- **Cách code hoạt động:** 
  - Giống với Khuyến mãi, đây là kỹ thuật *Soft Delete* (Xóa mềm). Backend chỉ lật trạng thái biến `isActive` thành `false`.
- **Thuật ngữ:**
  - **Cascade Constraint (Ràng buộc toàn vẹn):** Nếu cố tình bấm nút "Xóa" một danh mục đang có chứa sản phẩm bên trong, Database sẽ báo lỗi (Khóa ngoại - Foreign Key). Việc báo lỗi này giúp đảm bảo dữ liệu không bị mồ côi (Orphan data). Do đó tính năng Ẩn thường được khuyên dùng hơn.

---

## 📦 3. TRANG QUẢN LÝ KHO HÀNG (INVENTORY)
Module rất quan trọng để quản lý dòng luân chuyển hàng hóa.

### Tính năng 2.1: Cảnh báo hàng tồn kho thấp (Low Stock Alert)
- **Hoạt động:** 
  - Tự động lọc ra những sản phẩm/biến thể (size) có số lượng tồn kho thấp (dưới 10) và hiển thị cảnh báo ở một góc riêng, giúp Admin chủ động lên kế hoạch nhập hàng.
- **Cách code hoạt động:** 
  - Backend sử dụng câu SQL với điều kiện `WHERE stock < 10` (hoặc ngưỡng an toàn do cửa hàng quy định) để trích xuất nhanh những mặt hàng sắp cạn kiệt. Frontend gọi API này để hiển thị nhắc nhở Admin nhập hàng nhanh (Quick Import).

### Tính năng 2.2: Nhập/Xuất kho nhanh (Quick Import/Export)
- **Hoạt động:** 
  - Admin chọn 1 sản phẩm, chọn loại thao tác "Nhập thêm (+)" hoặc "Xuất bớt (-)", gõ số lượng. Giao diện tự tính toán ngay "Tồn kho sau khi cập nhật" để Admin xem trước.
- **Cách code hoạt động:** 
  - **Frontend:** Dùng tính năng `computed` của Vue để tính toán: `Tồn kho hiện tại` + (hoặc -) `Số lượng nhập` và hiển thị kết quả ngay tức thời lúc Admin đang gõ phím.
  - **Backend:** Khi xác nhận, Backend không chỉ cập nhật tồn kho mới, mà còn tự động sinh ra một **Phiếu nhập kho (Receipt)**. Việc lưu phiếu này giúp hệ thống luôn truy vết được "Lịch sử biến động" (Lịch sử nhập kho).

### Tính năng 2.3: Chỉnh sửa nhanh số lượng tại bảng (Inline Editing)
- **Hoạt động:** 
  - Thay vì phải bấm vào từng sản phẩm rồi qua 3-4 form để sửa kho, Admin có thể bấm thẳng vào nút hình cây bút (edit) ở con số tồn kho trên bảng tổng hợp để gõ lại số mới.
- **Cách code hoạt động:** 
  - Frontend sử dụng kỹ thuật *Conditional Rendering* (`v-if / v-else`). Khi bấm nút "Sửa", số lượng bình thường sẽ bị ẩn đi và thay thế bằng 1 ô `<input>` nhỏ để gõ trực tiếp. Gõ xong bấm Enter, giao diện lưu API và trả lại trạng thái hiển thị bình thường.

---

## ⭐ 4. TRANG QUẢN LÝ ĐÁNH GIÁ (REVIEWS)
Nơi tương tác với Feedback của khách hàng. Không có duyệt bài trước, chỉ có Ẩn/Hiện để đảm bảo tính minh bạch.

### Tính năng 3.1: Lọc trạng thái (Đang hiển thị / Đã ẩn)
- **Hoạt động:** 
  - Các nút Tab trên cùng giúp lọc ra bình luận nào đang hiển thị bình thường và bình luận nào đang bị ẩn.
- **Cách code hoạt động:** 
  - Truyền tham số chuỗi `status=APPROVED` hoặc `status=HIDDEN` lên API qua Query Parameter. Backend đọc tham số này, nạp vào truy vấn SQL để trả về đúng danh sách đánh giá cần tìm.

### Tính năng 3.2: Ẩn/Mở bình luận tiêu cực
- **Hoạt động:** 
  - Nếu gặp bình luận spam, Admin có quyền bấm "Ẩn bình luận" trong menu thao tác. Khách hàng ở ngoài web sẽ không thấy nữa.
- **Thuật ngữ / Logic:**
  - **Post-Moderation (Kiểm duyệt sau):** Hệ thống được thiết kế theo hướng cho phép khách hàng đăng bình luận và hiển thị ngay lập tức (Real-time feedback) nhằm tạo cảm giác chân thực. Admin chỉ can thiệp đi ẩn (Hide) những bình luận vi phạm chính sách sau khi nó đã lên sóng, thay vì mô hình Bắt buộc Duyệt (Pre-Moderation) gây chậm trễ cho người dùng.

### Tính năng 3.3: Cửa hàng phản hồi đánh giá (Admin Reply)
- **Hoạt động:** 
  - Admin bấm nút Chat, một khung Modal hiện ra cho phép Admin nhập câu trả lời của cửa hàng.
- **Cách code hoạt động:** 
  - Khi Admin gửi phản hồi, Backend lưu đoạn văn bản đó vào cột `admin_reply` tương ứng trên cùng dòng (Row) của bảng Đánh giá (Reviews). Khách hàng sẽ thấy cửa hàng phản hồi ngay dưới bình luận của mình ngoài trang chi tiết sản phẩm.

---

## 💡 5. CÁCH TRẢ LỜI CÂU HỎI VỀ 3 CHỨC NĂNG NÀY:

**1. Giáo viên: "Tại sao lại lưu ảnh danh mục qua API Upload chứ không lưu thẳng mã Base64 vào Database?"**
> Trả lời: Dạ, nếu lưu chuỗi Base64 (mã hóa hình ảnh thành chuỗi ký tự) trực tiếp vào Database, kích thước dòng dữ liệu sẽ phình to rất nhanh (một ảnh 1MB có thể thành chuỗi dài cả triệu ký tự). Điều này làm Database cực kỳ chậm khi truy vấn (Full Table Scan). Chuẩn công nghiệp là em đưa file ảnh lưu thành file tĩnh (Static file) ở máy chủ hoặc Cloud, sau đó chỉ lưu một chuỗi đường dẫn URL rất ngắn vào Database để tối ưu hiệu suất.

**2. Giáo viên: "Chức năng Tồn kho, nếu 2 nhân viên Admin cùng lúc nhấn nhập thêm 10 cái bánh thì có bị lỗi dữ liệu không?"**
> Trả lời: Dạ, để giải quyết vấn đề đồng thời (Concurrency), ở Backend em có thể sử dụng cơ chế **Transaction (Giao dịch)** của Spring Boot, hoặc dùng truy vấn cập nhật tương đối dạng `UPDATE variant SET stock = stock + 10` để Database tự khóa dòng (Row-level lock) trong khoảnh khắc đó, đảm bảo 2 quá trình không ghi đè lên nhau.

**3. Giáo viên: "Tại sao hệ thống Đánh giá (Review) không làm chức năng 'Chờ duyệt' trước khi hiển thị cho khách?"**
> Trả lời: Dạ đây là quyết định thiết kế (Design pattern) theo hướng Post-Moderation (Kiểm duyệt sau). Việc cho hiển thị ngay lập tức mang lại trải nghiệm người dùng rất tốt, khuyến khích khách review hơn. Thay vào đó, em cung cấp cho Admin công cụ Ẩn (Hide) chỉ với 1 click chuột để xử lý các bình luận spam/xấu sau đó. Phương pháp này giảm tải áp lực phải duyệt hàng trăm bình luận một ngày cho Admin.

**4. Giáo viên: "Phiếu nhập kho (Receipt) trong phần Lịch sử sinh ra từ đâu?"**
> Trả lời: Dạ, phiếu nhập kho không cần Admin phải tự tạo bằng tay một cách phức tạp. Khi Admin thao tác ở phần 'Nhập/Xuất nhanh', ngoài việc cập nhật biến số `stock`, Backend của em còn tự động tạo ngầm (Auto-generate) một dòng lưu vào bảng `Receipts` lưu lại dấu vết: ID sản phẩm, Số lượng thay đổi, và Ghi chú. Điều này đảm bảo tính minh bạch cho hệ thống quản lý.
