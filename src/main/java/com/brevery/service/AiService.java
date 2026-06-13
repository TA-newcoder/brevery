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
            log.warn("Anthropic API key is not configured. Falling back to Local Engine.");
            return generateLocalFallback(message, isAdmin);
        }

        try {
            String systemPrompt = isAdmin ? buildAdminPrompt() : buildCustomerPrompt(message);
            
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(15000);
            requestFactory.setReadTimeout(25000);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            String url = "https://api.anthropic.com/v1/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", aiApiKey);
            headers.set("anthropic-version", "2023-06-01");

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("model", "claude-sonnet-4-20250514");
            payloadNode.put("max_tokens", 1500);
            payloadNode.put("system", systemPrompt);

            ArrayNode messagesArray = payloadNode.putArray("messages");
            ObjectNode userMessage = messagesArray.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payloadNode), headers);

            log.info("Calling Anthropic Claude API for Yuni {}...", isAdmin ? "Admin" : "Customer");
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
                JsonNode contentNode = rootNode.path("content");
                if (contentNode.isArray() && contentNode.size() > 0) {
                    return contentNode.get(0).path("text").asText().trim();
                }
            }

            log.warn("Anthropic response was empty. Using fallback.");
            return generateLocalFallback(message, isAdmin);

        } catch (Exception e) {
            log.error("Failed to connect to Anthropic API", e);
            return generateLocalFallback(message, isAdmin);
        }
    }

    private String buildCustomerPrompt(String message) {
        List<Product> products = productRepository.findAll().stream().filter(Product::getIsAvailable).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Yuni, trợ lý ảo cực kỳ dễ thương, ngọt ngào của cửa hàng Brevery (Bakery & Beverage).\n");
        sb.append("Chào mặc định: 'Xin chào! Mình là Yuni, trợ lý của Brevery 🌸 Mình có thể giúp gì cho bạn?'\n");
        sb.append("Ngôn ngữ: Tiếng Việt, xưng 'mình' gọi 'bạn', dùng icon dễ thương.\n");
        sb.append("Nhiệm vụ: Tư vấn sản phẩm, gợi ý món, tra cứu đơn hàng, hướng dẫn.\n");
        sb.append("Nếu khách phàn nàn hoặc vấn đề phức tạp, hướng dẫn họ liên hệ Zalo shop qua số 0909000111.\n\n");
        
        sb.append("Danh sách thực đơn hiện có:\n");
        for (Product p : products.stream().limit(15).collect(Collectors.toList())) {
            sb.append("- ").append(p.getName()).append(": ").append(p.getDescription()).append("\n");
        }
        
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
        sb.append("Bạn là Yuni Admin, trợ lý quản lý AI cao cấp của cửa hàng Brevery (Bakery & Beverage).\n\n");
        sb.append("PHONG CÁCH:\n");
        sb.append("- Xưng: 'em' hoặc 'Yuni', gọi admin là 'sếp'\n");
        sb.append("- Ngôn ngữ: Tiếng Việt, chuyên nghiệp nhưng thân thiện và nhiệt huyết\n");
        sb.append("- Trả lời ngắn gọn, có cấu trúc rõ ràng, dùng icon phù hợp\n");
        sb.append("- Khi trình bày số liệu, dùng bảng hoặc bullet points\n");
        sb.append("- Luôn kết thúc bằng gợi ý hành động cụ thể nếu có thể\n\n");
        
        sb.append("═══════════════════════════════════════\n");
        sb.append("📊 DỮ LIỆU KINH DOANH THỰC TẾ:\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        sb.append("💰 DOANH THU:\n");
        sb.append("- Hôm nay: ").append(String.format("%,.0f VNĐ", todayRevenue)).append(" (").append(todayOrderCount).append(" đơn)\n");
        sb.append("- Tuần này: ").append(String.format("%,.0f VNĐ", weekRevenue)).append("\n");
        sb.append("- Tháng này: ").append(String.format("%,.0f VNĐ", monthRevenue)).append(" (").append(monthOrderCount).append(" đơn)\n");
        sb.append("- Tháng trước: ").append(String.format("%,.0f VNĐ", lastMonthRevenue)).append("\n");
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = monthRevenue.subtract(lastMonthRevenue).multiply(BigDecimal.valueOf(100))
                    .divide(lastMonthRevenue, 1, RoundingMode.HALF_UP);
            sb.append("- So với tháng trước: ").append(growth.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "").append(growth).append("%\n");
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
        sb.append("Hãy trả lời câu hỏi của sếp dựa trên dữ liệu trên. Tự tin, chuyên nghiệp, và luôn đưa ra gợi ý hành động cụ thể.\n");
        sb.append("Nếu sếp hỏi ngoài phạm vi dữ liệu, hãy nói rõ 'Em chưa có dữ liệu về phần này' thay vì bịa số liệu.\n");
        
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
            
            sb.append("\n💡 *Cấu hình API key Anthropic để Yuni phân tích chi tiết và thông minh hơn!*");
            return sb.toString();
        } else {
            return "Xin chào! Mình là Yuni, trợ lý của Brevery 🌸 Hiện tại hệ thống phản hồi tự động đang nâng cấp. Bạn vui lòng liên hệ Zalo 0909000111 để được hỗ trợ nhanh nhất nhé!";
        }
    }
}
