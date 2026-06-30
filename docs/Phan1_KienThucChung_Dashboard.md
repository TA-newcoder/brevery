# 🎓 Hướng Dẫn Bảo Vệ Đồ Án: Phân Tích Chuyên Sâu Các Chức Năng Cốt Lõi

Tài liệu này được định dạng với màu sắc, biểu tượng và in đậm các **từ khóa quan trọng** để bạn dễ dàng học thuộc. Đi kèm là bộ câu hỏi phòng vệ (Q&A) đã được bổ sung thêm nhiều tình huống thực tế để đối phó với hội đồng.

---

## 💻 1. NGĂN KÉO NỀN TẢNG: CÔNG NGHỆ TÍCH HỢP
*Phần này học thuộc để trả lời câu hỏi tổng quan về cấu trúc hệ thống.*

- 🎨 **Frontend (Giao diện):** 
  - Dựng bằng **Vue.js 3 (Composition API)** để chia nhỏ các component. 
  - Dùng **Vite** để build code siêu tốc. 
  - Dùng **Pinia** làm kho lưu trữ State (trạng thái đăng nhập, thông tin admin). 
  - Dùng **Chart.js** để vẽ biểu đồ.
- ⚙️ **Backend (Xử lý logic):** 
  - Xây dựng **RESTful API** bằng **Spring Boot 3 (Java 21)**.
- 🗄️ **Database (Cơ sở dữ liệu):** 
  - Lưu bằng **SQL Server**, giao tiếp với Backend qua **Spring Data JPA & Hibernate** (ORM - tự động map object Java thành bảng SQL).
- 🛡️ **Bảo mật:** 
  - Dùng **Spring Security & JWT** làm "vé thông hành" kiểm tra quyền truy cập các API.

---

## 📊 2. NGĂN KÉO DASHBOARD: SỐ LIỆU TỨC THỜI (REAL-TIME)
*Trang này dùng để Admin xem lướt tình hình hằng ngày. Tiêu chí cốt lõi: Nhanh và Tức thời.*

### 🛠️ Các chức năng chính & Kỹ thuật
- **Thẻ KPI (Doanh thu, Đơn hàng):** Backend dùng hàm tổng hợp (*Aggregation*) như `COUNT()` và `SUM()` trong SQL. Frontend dùng **Short Polling** (hàm `setInterval` gọi API ngầm mỗi 30s) để số liệu tự nhảy mà không cần ấn F5.
- **Biểu đồ Doanh thu (Line Chart):** Frontend vẽ biểu đồ đường, bo góc mềm mại (`tension: 0.4`), đổ bóng mờ (`fill: true`). Backend dùng `GROUP BY` theo ngày và `SUM` tiền. Hỗ trợ xuất file Excel bằng thư viện **Apache POI**.
- **Top 5 Sản phẩm & Đơn hàng:** Backend dùng `ORDER BY total_sold DESC LIMIT 5`. Frontend xử lý **Client-side Sorting** (khi click vào tiêu đề cột, Vue tự đảo thứ tự bằng Javascript mà không cần gọi lại Backend).

### ❓ Câu Hỏi Phòng Vệ Mảng Dashboard
**Hỏi 1: Dữ liệu Dashboard có cập nhật realtime không hay phải F5?**
> **Trả lời:** Dạ em dùng kỹ thuật Short Polling. Frontend cài đặt `setInterval` tự động gọi API ngầm 30 giây/lần. Giúp số liệu đơn hàng mới tự nhảy mà không bắt Admin thao tác tay.

**Hỏi 2: Tính năng sắp xếp cột Top Sản Phẩm xử lý ở đâu?**
> **Trả lời:** Dạ kết hợp cả hai. Backend xử lý lần đầu (`LIMIT 5`) để tiết kiệm băng thông. Sau khi có cục dữ liệu, thao tác click sắp xếp của Admin được xử lý hoàn toàn bằng Javascript (Vue Computed) trên Frontend để giao diện phản hồi ngay lập tức, giảm tải cho server.

**Hỏi 3: Biểu đồ vẽ như thế nào? Của thư viện nào?**
> **Trả lời:** Dạ em dùng Chart.js. Backend trả về 1 chuỗi JSON gồm mảng "nhãn ngày" và mảng "số liệu". Frontend nhận JSON này bơm vào thuộc tính `type: 'line'` của Chart.js để vẽ.

**Hỏi 4: Lỡ có 100 nhân viên Admin cùng mở trang Dashboard 1 lúc thì hàm setInterval gọi 30s/lần có làm sập server không? (Câu hỏi nâng cao)**
> **Trả lời:** Dạ với quy mô hiện tại thì Spring Boot hoàn toàn chịu tải được. Nhưng để tối ưu hơn trong thực tế, em có thể nâng cấp từ Short Polling lên dùng **WebSockets** (kết nối 2 chiều duy trì liên tục) hoặc áp dụng **Caching (Redis)** ở Backend. Khi đó API sẽ lấy dữ liệu từ Cache ra cực nhanh thay vì phải chạy lại câu SQL tính tổng.

---

## 📈 3. NGĂN KÉO BÁO CÁO & AI: PHÂN TÍCH CHUYÊN SÂU (INSIGHTS)
*Trang này dùng để xem xét định kỳ cuối tháng/năm. Tiêu chí cốt lõi: Chi tiết và Đầy đủ.*

### 🛠️ Các chức năng chính & Kỹ thuật
- **Biểu đồ Trạng thái đơn (Doughnut Chart):** Biểu đồ dạng bánh rán giúp xem tỷ trọng phần trăm (*Breakdown*). Backend dùng `GROUP BY status` và `COUNT()`.
- **Yuni AI (Tích hợp 2 nơi):**
  - **Ở Dashboard:** AI Chatbot thời gian thực (hỏi gì đáp nấy). Gửi Prompt ẩn kèm số liệu hôm nay lên API của LLM (Gemini) để sinh câu trả lời.
  - **Ở Reports:** AI dạng bài viết (*Batch Analysis*). Kéo toàn bộ lịch sử tháng gửi cho AI phân tích 1 lần, trả về văn bản dạng **Markdown**, Frontend dùng `marked.js` để render ra giao diện.

### ❓ Câu Hỏi Phòng Vệ Mảng Reports & AI
**Hỏi 1: Tại sao lại tách riêng Dashboard và Báo Cáo (Reports) mà không gộp chung?**
> **Trả lời:** Dạ để tối ưu hiệu năng (*Performance*). Dashboard cần load cực nhanh để xem hằng ngày, query nhẹ. Trang Reports phải gọi AI phân tích cục dữ liệu lớn và vẽ biểu đồ phức tạp. Tách ra giúp Dashboard không bị nghẽn chậm.

**Hỏi 2: AI trong Dashboard và AI trong Báo Cáo khác gì nhau?**
> **Trả lời:** Dạ, AI ở Dashboard là Chatbot tương tác (*Interactive*) giải quyết vấn đề tức thời. Còn AI ở Reports là Phân tích hàng loạt (*Batch Analysis*) chạy định kỳ, đọc toàn bộ dữ liệu lịch sử để viết ra một bản báo cáo tĩnh hoàn chỉnh, người dùng chỉ đọc chứ không chat.

**Hỏi 3: Nhỡ AI sinh ra nội dung bậy bạ hoặc sai lệch số liệu thì sao? Làm sao em kiểm soát?**
> **Trả lời:** Dạ trong Prompt (câu lệnh giấu kín) gửi lên AI, em đã thiết lập "System Role" đóng khung nghiêm ngặt là: "Bạn là trợ lý bán hàng chuyên nghiệp. Chỉ được phân tích dựa trên số liệu JSON tôi cung cấp dưới đây, tuyệt đối không bịa đặt dữ liệu ngoài". Do đó AI bị giới hạn phạm vi, đảm bảo tính an toàn và chính xác cho hệ thống.

---

## 🎟️ 4. NGĂN KÉO KHUYẾN MÃI: LOGIC KẾ TOÁN (COUPONS)
*Quản lý mã giảm giá. Tiêu chí cốt lõi: Chính xác và Chặt chẽ.*

### 🛠️ Các chức năng chính & Kỹ thuật
- **Quản lý (CRUD):** Thêm, sửa, xóa mã. Có **Validate** (xác thực) ngăn nhập số âm, ngày quá khứ. Database tự động lưu chữ IN HOA (`toUpperCase`) để không phân biệt hoa/thường (*Case-insensitive*).
- **Bật/Tắt Mã (Soft Update):** Nút hình con mắt. Không xóa hẳn (*Hard Delete*) mà chỉ đổi trạng thái cột `is_active` từ `true` sang `false`.
- **Kiểm soát lượt dùng:** Cột `usageLimit` (Tối đa) và `usedCount` (Đã dùng). Backend cộng dồn `usedCount` mỗi khi có đơn thành công.

### ❓ Câu Hỏi Phòng Vệ Mảng Khuyến Mãi
**Hỏi 1: Làm sao biết mã đã hết lượt?**
> **Trả lời:** Dạ mỗi khi áp mã, Backend so sánh nếu `usedCount >= usageLimit` thì sẽ chặn lại và báo lỗi. Nếu hợp lệ, đơn thành công thì `usedCount` sẽ tự động +1.

**Hỏi 2: Nếu Admin lỡ bấm "Tắt" mã, thì khách đang kẹt ở màn hình thanh toán có bị mất khuyến mãi không?**
> **Trả lời:** Dạ không ạ. Hệ thống của em dùng cơ chế chốt dữ liệu tại **Thời điểm xác thực (Point of Validation)**. Khi khách bấm nút Đặt hàng, nếu mã lúc đó còn hợp lệ thì tiền giảm giá được lưu cứng luôn vào hóa đơn. Việc Admin tắt mã sau đó chỉ áp dụng cho người đến sau.

**Hỏi 3: Khách hàng nhập mã "TET24", người khác nhập "tet24" thì tính là 1 mã hay 2 mã?**
> **Trả lời:** Dạ tính là 1 mã ạ. Ở Frontend khi người dùng gõ, hoặc khi xuống Backend, hàm `toUpperCase()` sẽ chuyển tất cả thành IN HOA để tra cứu trong Database, đảm bảo tính *Case-insensitive* (Không phân biệt chữ hoa chữ thường) giúp tối ưu trải nghiệm cho khách.

**Hỏi 4: Nếu lỡ khách hàng biết API, dùng tool bắn 100 request một lúc với cùng 1 mã giảm giá sắp hết lượt thì sao? (Vấn đề Race Condition)**
> **Trả lời:** Dạ thưa thầy/cô, để chặn trường hợp này, trong hàm xử lý Đặt hàng ở Backend, em sẽ đánh dấu `@Transactional` cho SQL. Khi trừ số lượng mã, hệ thống Database sẽ tự động khóa dòng dữ liệu đó lại (Row-level Lock), đảm bảo các luồng (Thread) xử lý tuần tự, sẽ không có chuyện 1 mã bị dùng lố số lượng cho phép.

---

## 🆘 5. BÍ KÍP "CỨU CÁNH" CẦN NHỚ TRONG PHÒNG BẢO VỆ
*Nếu giáo viên hỏi một câu bạn không chuẩn bị trước kiểu: "Sao em lại chọn cách này?" hoặc "Sao không làm kiểu kia cho dễ?", hãy bình tĩnh lôi 3 **"kim bài miễn tử"** này ra ghép vào đầu câu trả lời:*

> 💡 **"Dạ, em làm vậy để tối ưu Trải nghiệm người dùng (UX)..."** 
> *(Dành cho các tính năng liên quan đến Frontend, bấm nút, giao diện, sắp xếp).*

> ⚡ **"Dạ, quyết định này giúp hệ thống tối ưu Hiệu năng (Performance)..."** 
> *(Dành cho việc tách trang, lưu Cache, không gọi API thừa).*

> 🌐 **"Dạ, làm vậy để tiết kiệm Băng thông mạng (Network Bandwidth) và giảm tải cho Database..."** 
> *(Dành cho việc xử lý số liệu ở Backend rồi mới gửi lên, thay vì gửi cả cục bự lên Frontend cho Javascript tự tính).*

---

## 📌 PHỤ LỤC: VỊ TRÍ MÃ NGUỒN (SOURCE CODE) CÁC CHỨC NĂNG CỐT LÕI

*Phần này dùng để bạn tra cứu nhanh xem code của từng chức năng nằm ở file nào, dòng số mấy để kịp thời mở ra cho hội đồng xem nếu bị yêu cầu giải thích code.*

### 1. Báo cáo doanh thu & Biểu đồ trạng thái (Doughnut Chart/Line Chart)
- **Backend - Đếm đơn hàng theo trạng thái:** Nằm ở `src/main/java/com/brevery/service/AnalyticsService.java` (dòng 178 - 186) trong hàm `getOrderStatusBreakdown()`.
- **Backend - Gom nhóm doanh thu (GROUP BY):** Nằm ở `src/main/java/com/brevery/repository/OrderRepository.java` (dòng 44 - 50) trong hàm `getRevenueChartData()`.
- **Frontend - Render Biểu đồ Chart.js:** Nằm ở `bakery-frontend/src/pages/admin/Dashboard.vue` (dòng 230) và `bakery-frontend/src/pages/admin/Reports.vue` (dòng 64).

### 2. Yuni AI (Chatbot Real-time & Reports Batch Analysis)
- **Backend - Chatbot Yuni (Dashboard):** Nằm ở `src/main/java/com/brevery/service/AiService.java` (dòng 320) có prompt chỉ định System Role cho Admin.
- **Backend - Phân tích định kỳ (Reports - Batch Analysis):** Nằm ở `src/main/java/com/brevery/service/AnalyticsService.java` (dòng 224 - 393) ở hàm `generateDailyInsight()` (phần prompt và kết nối API Gemini).
- **Frontend - Render báo cáo Markdown:** Nằm ở `bakery-frontend/src/pages/admin/Reports.vue` (dòng 63, 194) sử dụng thư viện `marked` (`import { marked } from 'marked'`) để parse dữ liệu AI thành HTML.

### 3. Khuyến mãi (Coupons - Logic Kế Toán)
- **Backend - Không phân biệt hoa/thường (Case-insensitive):** Nằm ở `src/main/java/com/brevery/repository/CouponRepository.java` (dòng 11) qua cú pháp JPA `findByCodeIgnoreCase()`.
- **Backend - Cập nhật lượt dùng & Chống Race Condition (@Transactional):** Nằm ở `src/main/java/com/brevery/service/OrderService.java` (dòng 175 - 186) trong hàm xử lý Đặt hàng, kiểm tra và cập nhật `usedCount`.