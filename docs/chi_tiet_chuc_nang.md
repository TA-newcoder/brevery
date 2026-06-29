# Hướng Dẫn Bảo Vệ Đồ Án: Phân Tích Chuyên Sâu Các Chức Năng Cốt Lõi

Tài liệu này được viết theo dạng **"Chi tiết từng ngóc ngách"**. Đi kèm với mỗi chức năng là giải thích rõ ràng các thuật ngữ chuyên ngành (IT) để bạn có thể tự tin trả lời hội đồng bảo vệ.

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

## 📊 2. TRANG DASHBOARD (BẢNG ĐIỀU KHIỂN CHÍNH)
Trang Dashboard (`Dashboard.vue`) là nơi Admin vừa mở mắt ra là phải xem để biết tình hình kinh doanh trong ngày.

### Tính năng 2.1: Thẻ thống kê KPI (Các ô thông số trên cùng)
- **Hoạt động:** 
  - Hiển thị 4 con số quan trọng nhất: Tổng doanh thu, Tổng đơn chờ xử lý, Tổng khách, Tổng sản phẩm. Có mũi tên báo hiệu (màu xanh là tốt, màu đỏ/vàng là cảnh báo).
- **Cách code hoạt động:** 
  - Frontend gọi API `/summary`. 
  - Ở Backend, câu lệnh SQL sẽ chạy hàm *Aggregation* (Hàm tổng hợp). Cụ thể là hàm `COUNT()` để đếm số lượng người dùng/đơn hàng và hàm `SUM()` để cộng dồn tiền từ các hóa đơn hoàn thành. 
- **Thuật ngữ:** 
  - **KPI (Key Performance Indicator):** Chỉ số đánh giá hiệu quả kinh doanh.
  - **Short Polling:** Kỹ thuật mà Frontend cứ cách đúng 30 giây lại tự động gọi ngầm API một lần để lấy số liệu mới (bằng hàm `setInterval` trong Javascript) giúp Admin luôn thấy số đơn hàng chờ mới nhất mà không cần ấn F5.

### Tính năng 2.2: Biểu đồ doanh thu dạng đường (Line Chart)
- **Hoạt động:** 
  - Biểu diễn doanh thu thay đổi qua các ngày (hoặc tháng) bằng một đường gấp khúc đi lên đi xuống. Có các nút để lọc: 7 ngày, 30 ngày, 12 tháng.
- **Cách code hoạt động:** 
  - **Frontend:** Dùng thư viện Chart.js với thuộc tính `type: 'line'` (Biểu đồ đường). Dùng thêm `tension: 0.4` để nét gãy khúc trở nên bo cong mềm mại (*Smooth curve*), và `fill: true` để tạo bóng mờ bên dưới đường kẻ (*Gradient background*).
  - **Backend:** Chạy câu lệnh SQL có chứa mệnh đề `GROUP BY` (Gom nhóm dữ liệu). Tức là gom tất cả hóa đơn theo từng ngày, rồi tính `SUM` tiền của ngày đó.
- **Tính năng mở rộng - Xuất báo cáo (Export):** 
  - Khi bấm nút "Tạo báo cáo", Backend sẽ dùng thư viện Apache POI (Java) để xuất file Excel/PDF từ database và ép trình duyệt tự động tải xuống.

### Tính năng 2.3: Yuni Admin (Trợ lý AI tích hợp)
- **Hoạt động:** 
  - Một khung Chat giống hệt ChatGPT nằm ngay trên giao diện. Vừa mở trang, con AI này đã tự động chat 1 câu tóm tắt tình hình cửa hàng hôm nay và đưa ra lời khuyên. Admin có thể chat trực tiếp với nó.
- **Cách code hoạt động:** 
  - Khi load trang, Frontend gửi một *Prompt ẩn* (Câu lệnh giấu kín không cho người dùng thấy) yêu cầu AI phân tích. 
  - Backend nhận prompt, tự động chèn thêm số liệu từ Database (Doanh thu hôm nay, Số đơn ế...), sau đó gửi toàn bộ gói tin này qua API của hệ thống AI (như Gemini) để AI đọc, suy nghĩ và sinh ra câu trả lời trả về cho Frontend hiển thị.
- **Thuật ngữ:** 
  - **Prompt:** Lời nhắc hoặc câu lệnh yêu cầu AI làm gì đó.
  - **LLM (Large Language Model):** Mô hình ngôn ngữ lớn (như ChatGPT, Gemini) làm não bộ xử lý.

### Tính năng 2.4: Bảng Top Sản Phẩm & Đơn hàng gần đây
- **Hoạt động:** 
  - Hiển thị 5 món bán chạy nhất và 5 đơn hàng mới nhất vừa đặt.
- **Cách code hoạt động:** 
  - Backend dùng câu SQL có mệnh đề `ORDER BY total_sold DESC` (Sắp xếp theo số lượng bán giảm dần) và `LIMIT 5` (Chỉ lấy 5 dòng đầu tiên).
- **Thuật ngữ:**
  - **Client-side Sorting (Sắp xếp ở máy khách):** Khi Admin bấm vào tiêu đề cột "Doanh thu" ở Top sản phẩm, bảng sẽ tự đảo lộn trật tự. Việc đảo lộn này làm hoàn toàn bằng Javascript của Vue (Computed Property) ngay trên trình duyệt mà không cần phải gọi lại mạng Internet để xin Backend dữ liệu mới.

---

## 📈 3. TRANG BÁO CÁO (REPORTS)
Khác với Dashboard là xem lướt hằng ngày, trang Báo Cáo (`Reports.vue`) dùng để xem xét kỹ lưỡng định kỳ (Cuối tháng/Năm). Việc chia 2 trang riêng biệt giúp tối ưu *Performance* (Hiệu suất load trang) vì biểu đồ báo cáo rất nặng.

### Tính năng 3.1: Biểu đồ tỷ lệ trạng thái đơn hàng (Doughnut Chart)
- **Hoạt động:** 
  - Hiển thị một cái bánh tròn bị đục lỗ rỗng ở giữa. Chia làm nhiều mảng màu, mỗi màu tượng trưng cho 1 trạng thái: Đã giao, Chờ duyệt, Hủy...
- **Cách code hoạt động:** 
  - **Frontend:** Cấu hình Chart.js với `type: 'doughnut'`. Biểu đồ tròn cực kỳ phù hợp để xem *Breakdown* (Tỷ trọng phần trăm của một tổng thể).
  - **Backend:** Dùng truy vấn `GROUP BY status` (Gom nhóm theo trạng thái) và đếm (`COUNT`) số lượng đơn trong nhóm đó.

### Tính năng 3.2: Báo cáo Thông minh AI (AI Insights)
- **Hoạt động:** 
  - Không phải khung chat, mà là một màn hình hiển thị toàn bộ một bài văn dài báo cáo kinh doanh được in đậm, gạch đầu dòng rõ ràng.
- **Cách code hoạt động:** 
  - Khi bấm "Cập nhật báo cáo mới", Backend sẽ làm một tính năng gọi là *Batch Analysis* (Phân tích hàng loạt). Nó kéo toàn bộ dữ liệu lịch sử bán hàng trong tháng, gửi 1 cú khổng lồ cho AI. AI sẽ mất 10-15 giây để suy nghĩ và trả về một văn bản có định dạng *Markdown*.
- **Thuật ngữ:**
  - **Markdown:** Ngôn ngữ đánh dấu siêu nhẹ. Dùng các ký tự như `**chữ**` để in đậm, `#` để làm tiêu đề. Frontend dùng thư viện `marked.js` để dịch từ Markdown sang mã HTML hiển thị đẹp mắt.

---

## 🎟️ 4. TRANG QUẢN LÝ KHUYẾN MÃI (COUPONS)
Nơi Admin tạo ra các mã giảm giá cho chiến dịch Marketing. 

### Tính năng 4.1: Bảng danh sách & CRUD
- **Hoạt động:** 
  - Liệt kê các mã (VD: SALE50, TET2024...). Có nút Thêm, Sửa, Ẩn/Hiện, Xóa. Nếu mã hết hạn, ngày tháng sẽ bị tô màu đỏ.
- **Cách code hoạt động:** 
  - Quản lý bằng bộ API chuẩn REST. 
  - Khi nhấn "Thêm", một Modal (Hộp thoại nổi lên) xuất hiện. Dữ liệu trong Modal gắn liền với biến Javascript thông qua `v-model` (Ràng buộc dữ liệu 2 chiều - gõ trên form thì biến tự đổi).
- **Thuật ngữ:**
  - **CRUD:** 4 thao tác cơ bản với Database: Create (Tạo), Read (Đọc), Update (Sửa), Delete (Xóa).
  - **Validation (Xác thực dữ liệu):** Code ngăn không cho Admin nhập số âm vào ô tiền giảm, hoặc nhập ngày hết hạn là ngày của quá khứ.
  - **Case-insensitive (Không phân biệt hoa thường):** Khi lưu vào Database, Backend sẽ dùng hàm `toUpperCase()` để chuyển mã giảm giá thành IN HOA hết, và kiểm tra trùng lặp để khách nhập "tet" hay "TET" thì đều tính là 1 mã.

### Tính năng 4.2: Bật / Tắt mã khuyến mãi (Toggle Active)
- **Hoạt động:** 
  - Nút hình con mắt. Khi tắt đi, mã vẫn nằm trên bảng nhưng khách hàng không dùng được nữa.
- **Cách code hoạt động:** 
  - Thay vì Xóa hẳn dữ liệu ra khỏi Database (*Hard Delete*), Backend sẽ làm thao tác gọi là *Soft Update* (Cập nhật trạng thái). Nó chỉ lật ngược giá trị cột `is_active` trong Database từ `true` (Hoạt động) thành `false` (Bị khóa).
- **Thuật ngữ:**
  - **Point of Validation (Thời điểm xác thực):** Chức năng này cực kỳ thông minh. Nếu mã bị ẩn, thì những đơn hàng đang nằm trong Database mà đã áp mã này trước đó thì **KHÔNG BỊ ẢNH HƯỞNG**, tiền vẫn được giảm. Logic kiểm tra (hạn dùng, trạng thái ẩn, giới hạn số lượt) CHỈ chạy đúng vào khoảnh khắc khách hàng bấm nút **"Thanh Toán (Checkout)"**. Nếu qua được vòng kiểm tra đó, tiền giảm giá sẽ được lưu chết (Fix cứng) vào hóa đơn.

---

## 💡 5. CÁCH TRẢ LỜI MỘT SỐ CÂU HỎI THƯỜNG GẶP TỪ GIÁO VIÊN:

**1. Giáo viên: "Cái AI trong Dashboard và AI trong phần Báo Cáo khác nhau thế nào?"**
> Trả lời: Dạ, AI ở Dashboard hoạt động theo mô hình Chatbot thời gian thực (Interactive), cho phép em hỏi đáp các vấn đề cụ thể (như "Phân tích vì sao hôm nay ế?"). Còn AI trong phần Báo cáo (Reports) là dạng tạo Báo cáo tĩnh toàn diện định kỳ (Batch Analysis). Nó lấy toàn bộ dữ liệu tổng hợp của cửa hàng để đưa ra bài phân tích sâu, người dùng chỉ đọc và nhận lời khuyên chứ không chat qua lại.

**2. Giáo viên: "Làm sao để biết mã giảm giá đã hết lượt sử dụng?"**
> Trả lời: Dạ trong Database của chức năng Khuyến mãi em có lưu 2 trường là `usageLimit` (giới hạn tối đa) và `usedCount` (số lượt đã dùng). Cứ mỗi lần có một đơn hàng đặt thành công bằng mã đó, Backend sẽ `usedCount = usedCount + 1`. Khi khách áp mã, Backend sẽ so sánh nếu `usedCount >= usageLimit` thì sẽ văng ra lỗi báo "Mã đã hết lượt".

**3. Giáo viên: "Biểu đồ trong Dashboard lấy số liệu vẽ như thế nào?"**
> Trả lời: Dạ, ở Frontend em dùng thư viện Chart.js. Còn ở Backend em viết API trả về dữ liệu chuẩn JSON gồm 2 mảng là labels (nhãn ngày tháng) và data (doanh thu tương ứng). Backend của em dùng câu query `GROUP BY` theo ngày/tháng trên cột `created_at` của bảng Đơn hàng (những đơn thành công) và `SUM` cột tổng tiền để tính ra doanh thu. Frontend nhận mảng JSON đó rồi bơm vào Chart.js để vẽ.

**4. Giáo viên: "Dữ liệu Dashboard có được cập nhật realtime (thời gian thực) không hay phải F5 (tải) lại trang?"**
> Trả lời: Dạ, Dashboard của em sử dụng kỹ thuật Short Polling. Ở Frontend (`Dashboard.vue`), em có cài đặt một bộ đếm giờ (Timer) sử dụng hàm `setInterval` để tự động gọi lại các API mỗi 30 giây một lần. Điều này giúp các số liệu (đặc biệt là số đơn hàng chờ xử lý) được tự động làm mới liên tục mà Admin không cần thao tác F5.

**5. Giáo viên: "Nếu 1 mã khuyến mãi đang có người dùng, nhưng Admin bấm nút 'Ẩn' thì đơn hàng đang thanh toán của người đó có bị ảnh hưởng không?"**
> Trả lời: Dạ không ạ. Logic mã khuyến mãi chỉ được kiểm tra (Validate) ở đúng **thời điểm người dùng bấm nút Đặt Hàng (Checkout)**. Khi đơn hàng đã được tạo thành công vào Database, số tiền giảm giá đã được tính cứng vào hệ thống. Nếu sau đó Admin mới bấm ẩn hoặc sửa mã thì chỉ ngăn chặn những người đặt hàng sau, còn các đơn đã tạo trước đó thì không bị thay đổi.

**6. Giáo viên: "Tại sao lại tách riêng trang Dashboard và Bảng Báo Cáo (Reports) mà không gộp chung vào 1 trang?"**
> Trả lời: Dạ em thiết kế tách ra để tối ưu trải nghiệm và hiệu năng (Performance). Dashboard là trang Admin xem hằng ngày để xử lý công việc tức thời (có đơn mới không, top sản phẩm hôm nay), nên các câu SQL query rất nhẹ. Còn trang Báo Cáo là nơi xem định kỳ (cuối tuần/tháng), cần render các biểu đồ phân bổ phức tạp và gọi AI phân tích toàn bộ dữ liệu lớn. Tách ra giúp trang Dashboard load cực kỳ nhanh mà không bị nghẽn bởi chức năng Báo cáo.

**7. Giáo viên: "Tính năng sắp xếp (Sort) cột của bảng Top Sản Phẩm bán chạy được xử lý trên Frontend hay Backend?"**
> Trả lời: Dạ là sự kết hợp của cả hai. Lúc đầu, Backend dùng câu truy vấn SQL `ORDER BY total_sold DESC LIMIT 5` để lấy chính xác 5 sản phẩm bán chạy nhất từ Database trả về nhằm tiết kiệm băng thông. Tuy nhiên, sau khi có dữ liệu, nếu Admin bấm vào các tiêu đề cột (như Doanh thu) để sắp xếp lại, thì tính năng sort đó được xử lý hoàn toàn bằng JavaScript (Vue Computed Properties `sortedTopProducts`) trên Frontend, giúp giao diện thay đổi ngay lập tức mà không cần tốn tài nguyên gọi lại API.

**8. Giáo viên: "Cái biểu đồ dạng đường cứ đi lên đi xuống hiển thị Doanh Thu thuộc loại (type) gì?"**
> Trả lời: Dạ biểu đồ hiển thị doanh thu thay đổi qua các ngày thuộc loại **`type: 'line'` (Biểu đồ đường / Line Chart)** của thư viện Chart.js. Em cấu hình thêm thuộc tính `tension: 0.4` để nét đứt gãy giữa các điểm dữ liệu trở nên bo cong mềm mại (smooth) và dùng `fill: true` kết hợp với màu Gradient để tạo lớp đổ bóng mờ ảo phía dưới đường kẻ, giúp biểu đồ trông hiện đại và có chiều sâu hơn.

---

*Lưu ý: Nếu bị hỏi những câu khó như "Tại sao làm thế này mà không làm thế kia", hãy luôn dùng các từ khóa như: "Để tối ưu Performance (Hiệu suất)", "Để tăng cường UX (Trải nghiệm người dùng)", "Để tiết kiệm băng thông mạng (Network bandwidth)". Giáo viên sẽ rất thích những câu trả lời có tính hệ thống như vậy.*
