package com.brevery.service;

import com.brevery.entity.*;
import com.brevery.enums.ErrorCode;
import com.brevery.enums.OrderStatus;
import com.brevery.exception.AppException;
import com.brevery.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    private static final String RATE_LIMIT_PREFIX = "rate:ai:chat:";

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String chat(String message, String clientIp, boolean isAdmin) {
        log.info("AI Chatbot (Yuni) received message: {} from IP: {} (isAdmin: {})", message, clientIp, isAdmin);

        if (!StringUtils.hasText(message)) {
            return isAdmin ? "Xin chào sếp! Yuni sẵn sàng báo cáo tình hình kinh doanh. Sếp muốn biết gì ạ?" : 
                             "Xin chào! Mình là Yuni, trợ lý của Brevery 🌸 Mình có thể giúp gì cho bạn?";
        }

        // Rate limiting
        try {
            String rateLimitKey = RATE_LIMIT_PREFIX + clientIp;
            Long count = redisTemplate.opsForValue().increment(rateLimitKey);
            if (count != null && count == 1) {
                redisTemplate.expire(rateLimitKey, 1, TimeUnit.MINUTES);
            }
            if (count != null && count > 15) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Bạn đã chat quá giới hạn. Vui lòng thử lại sau.");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Redis rate limiting skipped or error", e);
        }

        if (!StringUtils.hasText(aiApiKey)) {
            log.warn("Gemini API key is not configured. Falling back to Local Engine.");
            return generateLocalFallback(message, isAdmin);
        }

        try {
            String systemPrompt = isAdmin ? buildAdminPrompt() : buildCustomerPrompt(message);
            
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(15000);
            requestFactory.setReadTimeout(40000);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + aiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode payloadNode = objectMapper.createObjectNode();
            
            // System instructions
            ObjectNode systemInstruction = payloadNode.putObject("system_instruction");
            ArrayNode sysParts = systemInstruction.putArray("parts");
            sysParts.addObject().put("text", systemPrompt);
            
            // User message
            ArrayNode contentsArray = payloadNode.putArray("contents");
            ObjectNode contentObj = contentsArray.addObject();
            contentObj.put("role", "user");
            ArrayNode partsArray = contentObj.putArray("parts");
            partsArray.addObject().put("text", message);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payloadNode), headers);

            log.info("Calling Google Gemini API for Yuni {}...", isAdmin ? "Admin" : "Customer");
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
                JsonNode candidates = rootNode.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        String replyText = parts.get(0).path("text").asText().trim();
                        return formatMarkdownAndCards(replyText, isAdmin);
                    }
                }
            }

            log.warn("Gemini response was empty. Using fallback.");
            return generateLocalFallback(message, isAdmin);

        } catch (Exception e) {
            log.error("Failed to connect to Gemini API", e);
            return generateLocalFallback(message, isAdmin);
        }
    }

    private String formatMarkdownAndCards(String text, boolean isAdmin) {
        if (!isAdmin) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\[PRODUCT_ID:(\\d+)\\]").matcher(text);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                try {
                    Long productId = Long.parseLong(matcher.group(1));
                    Product p = productRepository.findById(productId).orElse(null);
                    if (p != null) {
                        String imageUrl = "/images/placeholder.png";
                        if (p.getImages() != null && !p.getImages().isEmpty()) {
                            imageUrl = p.getImages().get(0).getImageUrl();
                        }
                        String price = "Liên hệ";
                        if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                            price = String.format("%,.0fđ", p.getVariants().get(0).getPrice());
                        }
                        
                        String html = "<div class=\"d-flex align-items-center gap-3 p-2 mt-2 mb-2 border rounded-3\" style=\"background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); max-width: 280px;\">" +
                                "<img src=\"" + imageUrl + "\" style=\"width: 60px; height: 60px; object-fit: cover; border-radius: 8px; flex-shrink: 0;\" />" +
                                "<div style=\"flex-grow: 1; min-width: 0;\">" +
                                "<div class=\"fw-bold text-truncate\" style=\"color: #E07340; font-size: 0.85rem;\" title=\"" + p.getName() + "\">" + p.getName() + "</div>" +
                                "<div class=\"d-flex align-items-center justify-content-between mt-1\">" +
                                "<span style=\"color: #1a0f0a; font-weight: bold; font-size: 0.9rem;\">" + price + "</span>" +
                                "<a href=\"/products/" + p.getProductId() + "\" target=\"_blank\" class=\"btn btn-sm\" style=\"background: #E07340; color: white; padding: 2px 8px; font-size: 0.75rem; border-radius: 4px;\">Xem</a>" +
                                "</div>" +
                                "</div>" +
                                "</div>";
                        matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(html));
                    } else {
                        matcher.appendReplacement(sb, "");
                    }
                } catch (Exception e) {
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);
            text = sb.toString();
        }
        
        // Convert basic markdown
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
        text = text.replaceAll("\\*(.*?)\\*", "<em>$1</em>");
        text = text.replace("\n", "<br>");
        return text;
    }

    private String buildCustomerPrompt(String message) {
        List<Product> products = productRepository.findAll().stream().filter(Product::getIsAvailable).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Yuni, trợ lý ảo cực kỳ dễ thương, ngọt ngào của cửa hàng Brevery — chuyên phân phối bánh kẹo đóng gói và nước giải khát đóng chai/lon.\n");
        sb.append("Chào mặc định: 'Xin chào! Mình là Yuni, trợ lý của Brevery 🌸 Mình có thể giúp gì cho bạn?'\n");
        sb.append("Ngôn ngữ: Tiếng Việt, xưng 'mình' gọi 'bạn', dùng icon dễ thương.\n\n");
        
        sb.append("NHIỆM VỤ QUAN TRỌNG:\n");
        sb.append("1. Tư vấn sản phẩm CỤ THỂ dựa trên yêu cầu khách (vị, độ ngọt, giá, mục đích sử dụng)\n");
        sb.append("2. Khi gợi ý, PHẢI đề cập TÊN SẢN PHẨM, GIÁ, và ĐẶC BIỆT thêm mã [PRODUCT_ID:id] ngay phía sau để hệ thống tự động hiển thị hình ảnh (ví dụ: Bánh quy Danisa [PRODUCT_ID:1])\n");
        sb.append("3. Đọc kỹ phần MÔ TẢ sản phẩm bên dưới để biết hương vị, độ ngọt, thành phần\n");
        sb.append("4. Nếu khách hỏi 'ít ngọt', 'không ngọt' → lọc sản phẩm có ghi 'ÍT NGỌT' hoặc 'KHÔNG NGỌT' trong mô tả\n");
        sb.append("5. Nếu khách phàn nàn hoặc vấn đề phức tạp, hướng dẫn họ liên hệ hotline 0705 230 644\n");
        sb.append("6. Trả lời dưới dạng danh sách, CÓ CẤU TRÚC, dùng icon và bullet points\n\n");
        
        sb.append("═══════════════════════════════════════\n");
        sb.append("📋 DANH SÁCH SẢN PHẨM HIỆN CÓ:\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        for (Product p : products) {
            sb.append("🔹 [PRODUCT_ID:").append(p.getProductId()).append("] ").append(p.getName());
            sb.append(" [").append(p.getCategory().getName()).append("]");
            // Include price from variants
            if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                sb.append(" — Giá: ");
                for (int i = 0; i < p.getVariants().size(); i++) {
                    ProductVariant v = p.getVariants().get(i);
                    sb.append(v.getSize()).append(": ").append(String.format("%,.0f", v.getPrice())).append("đ");
                    if (i < p.getVariants().size() - 1) sb.append(", ");
                }
            }
            sb.append("\n   Mô tả: ").append(p.getDescription()).append("\n\n");
        }
        
        sb.append("═══════════════════════════════════════\n");
        sb.append("Địa chỉ cửa hàng: 12 Nguyễn Văn Bảo, Phường 4, Q.Gò Vấp, TP.HCM\n");
        sb.append("Hotline: 0705 230 644\n");
        sb.append("Giờ mở cửa: T2-CN, 7:00 - 22:00\n");
        sb.append("Freeship cho đơn từ 500.000đ nội thành HCM\n");
        
        if (message.matches(".*(đơn hàng|BRV-).*") || message.contains("@")) {
            sb.append("\nMột số đơn hàng gần đây để bạn tra cứu:\n");
            List<Order> recentOrders = orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(10).collect(Collectors.toList());
            for (Order o : recentOrders) {
                sb.append("Đơn ").append(o.getOrderCode()).append(" (Email: ")
                  .append(o.getUser().getEmail()).append(") - Trạng thái: ").append(o.getStatus()).append("\n");
            }
        }
        
        return sb.toString();
    }

    private String buildAdminPrompt() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime startOfWeek = startOfDay.minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);
        
        // --- Revenue data ---
        List<Order> todayOrders = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        BigDecimal todayRevenue = todayOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long todayOrderCount = todayOrders.size();
        
        List<Order> allOrders = orderRepository.findAll();
        
        // This week
        BigDecimal weekRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfWeek))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // This month
        BigDecimal monthRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long monthOrderCount = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth)).count();

        // Last month (for comparison)
        BigDecimal lastMonthRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getCreatedAt() != null
                        && o.getCreatedAt().isAfter(startOfLastMonth) && o.getCreatedAt().isBefore(endOfLastMonth))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // --- Order status breakdown ---
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long confirmedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
        long preparingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING).count();
        long shippedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        long deliveringOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERING).count();
        long completedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long totalOrders = allOrders.size();
        
        // Cancel rate
        String cancelRate = totalOrders > 0 ? 
            BigDecimal.valueOf(cancelledOrders * 100).divide(BigDecimal.valueOf(totalOrders), 1, RoundingMode.HALF_UP) + "%" : "0%";

        // --- Products ---
        List<Product> allProducts = productRepository.findAll();
        long totalProducts = allProducts.size();
        
        // Low stock alert
        List<Product> lowStockProducts = allProducts.stream()
                .filter(p -> p.getVariants() != null && p.getVariants().stream().anyMatch(v -> v.getStock() <= p.getLowStockThreshold()))
                .collect(Collectors.toList());
        
        // Out of stock
        List<Product> outOfStockProducts = allProducts.stream()
                .filter(p -> p.getVariants() != null && p.getVariants().stream().allMatch(v -> v.getStock() == 0))
                .collect(Collectors.toList());
        
        // Top selling
        List<Product> topSelling = allProducts.stream()
                .sorted((a, b) -> (b.getTotalSold() == null ? 0 : b.getTotalSold()) - (a.getTotalSold() == null ? 0 : a.getTotalSold()))
                .limit(5).collect(Collectors.toList());
        
        // --- Customers ---
        long totalCustomers = userRepository.count();
        
        // --- Reviews ---
        List<Review> allReviews = reviewRepository.findAll();
        long pendingReviews = allReviews.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        double avgRating = allReviews.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .mapToInt(Review::getRating)
                .average().orElse(0.0);
        long negativeReviews = allReviews.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()) && r.getRating() <= 2).count();

        // --- Build prompt ---
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Yuni Admin, trợ lý quản lý AI cao cấp của cửa hàng Brevery — chuyên phân phối bánh kẹo đóng gói và nước giải khát đóng chai/lon.\n\n");
        sb.append("PHONG CÁCH BẮT BUỘC:\n");
        sb.append("- Xưng: 'em' hoặc 'Yuni', gọi admin là 'sếp'\n");
        sb.append("- Ngôn ngữ: Tiếng Việt, chuyên nghiệp nhưng thân thiện và nhiệt huyết\n");
        sb.append("- Trả lời CÓ CẤU TRÚC RÕ RÀNG: dùng tiêu đề (##, ###), bullet points, icon phù hợp\n");
        sb.append("- Khi trình bày số liệu, dùng bullet points và in đậm số quan trọng\n");
        sb.append("- LUÔN LUÔN đưa ra phân tích chủ động, nhận xét xu hướng, và gợi ý hành động cụ thể\n");
        sb.append("- Nếu phát hiện vấn đề (tỷ lệ huỷ cao, tồn kho thấp, doanh thu giảm), phải CẢNH BÁO rõ ràng\n\n");
        
        sb.append("VAI TRÒ CỦA BẠN:\n");
        sb.append("1. PHÂN TÍCH GIA (Business Analyst): Đọc số liệu, phát hiện xu hướng tăng/giảm, so sánh các giai đoạn\n");
        sb.append("2. CỐ VẤN CHIẾN LƯỢC: Đưa ra lời khuyên kinh doanh dựa trên dữ liệu (VD: nên chạy khuyến mãi, nhập thêm hàng, v.v.)\n");
        sb.append("3. CẢNH BÁO RỦI RO: Chủ động báo ngay nếu phát hiện vấn đề nghiêm trọng (hết hàng, đơn huỷ nhiều, đánh giá kém)\n");
        sb.append("4. GỢI Ý HÀNH ĐỘNG: Mỗi câu trả lời phải kết thúc bằng ít nhất 2-3 hành động cụ thể sếp nên làm NGAY\n\n");
        
        sb.append("═══════════════════════════════════════\n");
        sb.append("📊 DỮ LIỆU KINH DOANH THỰC TẾ (CẬP NHẬT REAL-TIME):\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        sb.append("💰 DOANH THU:\n");
        sb.append("- Hôm nay: ").append(String.format("%,.0f VNĐ", todayRevenue)).append(" (").append(todayOrderCount).append(" đơn)\n");
        sb.append("- Tuần này: ").append(String.format("%,.0f VNĐ", weekRevenue)).append("\n");
        sb.append("- Tháng này: ").append(String.format("%,.0f VNĐ", monthRevenue)).append(" (").append(monthOrderCount).append(" đơn)\n");
        sb.append("- Tháng trước: ").append(String.format("%,.0f VNĐ", lastMonthRevenue)).append("\n");
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = monthRevenue.subtract(lastMonthRevenue).multiply(BigDecimal.valueOf(100))
                    .divide(lastMonthRevenue, 1, RoundingMode.HALF_UP);
            sb.append("- Tăng trưởng so tháng trước: ").append(growth.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "").append(growth).append("%\n");
        }
        
        // Average order value
        if (completedOrders > 0) {
            BigDecimal avgOrderValue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(completedOrders), 0, RoundingMode.HALF_UP);
            sb.append("- Giá trị đơn trung bình (AOV): ").append(String.format("%,.0f VNĐ", avgOrderValue)).append("\n");
        }
        
        sb.append("\n📦 ĐƠN HÀNG (tổng ").append(totalOrders).append(" đơn):\n");
        sb.append("- Chờ xác nhận: ").append(pendingOrders).append("\n");
        sb.append("- Đã xác nhận: ").append(confirmedOrders).append("\n");
        sb.append("- Đang chuẩn bị: ").append(preparingOrders).append("\n");
        sb.append("- Đã giao VC: ").append(shippedOrders).append("\n");
        sb.append("- Đang giao: ").append(deliveringOrders).append("\n");
        sb.append("- Hoàn thành: ").append(completedOrders).append("\n");
        sb.append("- Đã huỷ: ").append(cancelledOrders).append(" (tỷ lệ huỷ: ").append(cancelRate).append(")\n");
        
        sb.append("\n🏆 TOP 5 SẢN PHẨM BÁN CHẠY:\n");
        for (int i = 0; i < topSelling.size(); i++) {
            Product p = topSelling.get(i);
            sb.append(i + 1).append(". ").append(p.getName()).append(" — Đã bán: ").append(p.getTotalSold() == null ? 0 : p.getTotalSold()).append("\n");
        }
        
        // Bottom selling
        List<Product> bottomSelling = allProducts.stream()
                .filter(Product::getIsAvailable)
                .sorted((a, b) -> (a.getTotalSold() == null ? 0 : a.getTotalSold()) - (b.getTotalSold() == null ? 0 : b.getTotalSold()))
                .limit(3).collect(Collectors.toList());
        if (!bottomSelling.isEmpty()) {
            sb.append("\n📉 SẢN PHẨM BÁN CHẬM NHẤT:\n");
            for (int i = 0; i < bottomSelling.size(); i++) {
                Product p = bottomSelling.get(i);
                sb.append(i + 1).append(". ").append(p.getName()).append(" — Chỉ bán: ").append(p.getTotalSold() == null ? 0 : p.getTotalSold()).append("\n");
            }
        }
        
        sb.append("\n🏪 KHO HÀNG (").append(totalProducts).append(" sản phẩm):\n");
        if (!outOfStockProducts.isEmpty()) {
            sb.append("🔴 HẾT HÀNG: ");
            sb.append(outOfStockProducts.stream().map(Product::getName).collect(Collectors.joining(", "))).append("\n");
        }
        if (!lowStockProducts.isEmpty()) {
            sb.append("🟡 SẮP HẾT (dưới ngưỡng):\n");
            for (Product p : lowStockProducts) {
                p.getVariants().stream()
                    .filter(v -> v.getStock() <= p.getLowStockThreshold())
                    .forEach(v -> sb.append("  - ").append(p.getName()).append(" [").append(v.getSize()).append("] còn ").append(v.getStock()).append(" (ngưỡng: ").append(p.getLowStockThreshold()).append(")\n"));
            }
        } else {
            sb.append("✅ Tất cả sản phẩm đều đủ hàng.\n");
        }
        
        sb.append("\n👥 KHÁCH HÀNG: ").append(totalCustomers).append(" tài khoản\n");
        
        sb.append("\n⭐ ĐÁNH GIÁ:\n");
        sb.append("- Rating trung bình: ").append(String.format("%.1f", avgRating)).append("/5 sao\n");
        sb.append("- Bình luận chờ duyệt: ").append(pendingReviews).append("\n");
        if (negativeReviews > 0) {
            sb.append("- ⚠️ Có ").append(negativeReviews).append(" đánh giá tiêu cực (≤2 sao)\n");
        }
        
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("YÊU CẦU TRẢ LỜI:\n");
        sb.append("1. Nếu sếp yêu cầu 'phân tích tổng quan' hoặc đây là lần đầu tiên trong phiên, hãy TỰ ĐỘNG phân tích toàn bộ dữ liệu trên và đưa ra:\n");
        sb.append("   a. Tóm tắt tình hình kinh doanh (doanh thu, đơn hàng, xu hướng)\n");
        sb.append("   b. Điểm mạnh và điểm yếu\n");
        sb.append("   c. Cảnh báo rủi ro (nếu có)\n");
        sb.append("   d. 3 hành động ưu tiên cần làm ngay hôm nay\n");
        sb.append("2. Trả lời câu hỏi của sếp dựa trên dữ liệu thực tế ở trên. Tự tin, chuyên nghiệp.\n");
        sb.append("3. Nếu sếp hỏi ngoài phạm vi dữ liệu, hãy nói rõ 'Em chưa có dữ liệu về phần này' thay vì bịa số liệu.\n");
        sb.append("4. Luôn kết thúc bằng câu hỏi gợi mở để tiếp tục hội thoại.\n");
        
        return sb.toString();
    }

    private String generateLocalFallback(String message, boolean isAdmin) {
        if (isAdmin) {
            // Build a basic summary even without API
            List<Order> allOrders = orderRepository.findAll();
            long pending = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
            long completed = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
            BigDecimal totalRevenue = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            List<Product> lowStock = productRepository.findAll().stream()
                    .filter(p -> p.getVariants() != null && p.getVariants().stream().anyMatch(v -> v.getStock() <= p.getLowStockThreshold()))
                    .collect(Collectors.toList());
            
            StringBuilder sb = new StringBuilder();
            sb.append("📊 **Báo cáo nhanh từ Yuni:**\n\n");
            sb.append("💰 Tổng doanh thu (đơn hoàn thành): **").append(String.format("%,.0f VNĐ", totalRevenue)).append("**\n");
            sb.append("📦 Đơn chờ duyệt: **").append(pending).append("** | Hoàn thành: **").append(completed).append("**\n");
            
            if (!lowStock.isEmpty()) {
                sb.append("\n⚠️ **Cảnh báo tồn kho thấp:**\n");
                for (Product p : lowStock.stream().limit(5).collect(Collectors.toList())) {
                    sb.append("- ").append(p.getName()).append("\n");
                }
            }
            
            sb.append("\n*Mẹo: Hệ thống AI đang tạm thời gián đoạn do quá tải. Xin vui lòng thử lại sau ít phút!*");
            return sb.toString();
        } else {
            String lowerMsg = message.toLowerCase();
            if (lowerMsg.contains("chào") || lowerMsg.contains("hi") || lowerMsg.contains("hello")) {
                return "Chào bạn! Mình là Yuni 🌸. Bạn cần mình tư vấn về bánh kẹo hay đồ uống ạ?";
            } else if (lowerMsg.contains("giá") || lowerMsg.contains("bao nhiêu")) {
                return "Bên mình có rất nhiều mức giá khác nhau tùy thuộc vào loại sản phẩm. Thường nước giải khát giao động từ 15k - 50k, còn bánh kẹo đóng hộp từ 30k - 200k ạ. Bạn muốn xem loại nào cụ thể không?";
            } else if (lowerMsg.contains("giao hàng") || lowerMsg.contains("ship")) {
                return "Brevery có giao hàng tận nơi tại TP.HCM ạ. Phí ship sẽ tùy thuộc vào khoảng cách, nhưng freeship cho đơn từ 500k nhé! 🚚";
            } else if (lowerMsg.contains("bánh") || lowerMsg.contains("kẹo")) {
                return "Brevery có các dòng bánh quy bơ, bánh xốp đóng hộp cao cấp nhập khẩu và nội địa. Rất thích hợp để nhâm nhi cùng trà hoặc làm quà tặng. Bạn ghé trang Sản phẩm xem nhé! 🍪";
            } else if (lowerMsg.contains("nước") || lowerMsg.contains("trà") || lowerMsg.contains("cafe") || lowerMsg.contains("cà phê")) {
                return "Về đồ uống, bên mình phân phối các loại nước khoáng, nước trái cây đóng lon và trà sữa đóng chai vô cùng tiện lợi. Đảm bảo date luôn mới nhất! 🥤";
            } else if (lowerMsg.contains("cửa hàng") || lowerMsg.contains("địa chỉ") || lowerMsg.contains("ở đâu")) {
                return "Brevery hiện có trụ sở chính tại 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM ạ. Rất mong được đón tiếp bạn! 🏠";
            } else {
                return "Cảm ơn bạn đã nhắn tin cho Brevery! 🌸 Yuni đang trong quá trình học hỏi thêm nên chưa hiểu rõ ý bạn. Bạn có thể nói rõ hơn hoặc gọi trực tiếp hotline 0705 230 644 nhé!";
            }
        }
    }
}
