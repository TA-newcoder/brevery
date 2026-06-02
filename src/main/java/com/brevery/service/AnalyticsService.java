package com.brevery.service;

import com.brevery.dto.response.ProductListDTO;
import com.brevery.entity.Product;
import com.brevery.entity.ProductVariant;
import com.brevery.enums.ErrorCode;
import com.brevery.enums.OrderStatus;
import com.brevery.exception.AppException;
import com.brevery.mapper.ProductMapper;
import com.brevery.repository.OrderRepository;
import com.brevery.repository.ProductRepository;
import com.brevery.repository.ProductVariantRepository;
import com.brevery.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    private static final String INSIGHT_CACHE_KEY = "analytics:insight";

    /**
     * DTO đại diện cho biểu đồ doanh thu
     */
    public record RevenueChartPoint(String date, BigDecimal revenue, Long orderCount) {}

    /**
     * DTO đại diện cho KPI Summary
     */
    public record KpiSummary(
            BigDecimal totalRevenue,
            Long pendingOrders,
            Long totalCustomers,
            Long totalProducts
    ) {}

    /**
     * Lấy KPI Summary của cửa hàng, cache 2 phút
     */
    @Cacheable(value = "analytics_summary", key = "'kpis'", unless = "#result == null")
    public KpiSummary getSummary() {
        log.info("Calculating business KPIs summary...");

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        BigDecimal revenueToday = orderRepository.sumTotalAmountBetween(startOfToday, endOfToday);
        if (revenueToday == null) revenueToday = BigDecimal.ZERO;

        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        BigDecimal revenueThisMonth = orderRepository.sumTotalAmountBetween(startOfMonth, endOfToday);
        if (revenueThisMonth == null) revenueThisMonth = BigDecimal.ZERO;

        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long totalCustomers = userRepository.count();
        Long totalProducts = productRepository.count();

        return new KpiSummary(revenueToday, pendingOrders, totalCustomers, totalProducts);
    }

    /**
     * Lấy dữ liệu biểu đồ doanh thu theo period (7days | 30days | 12months)
     */
    public List<RevenueChartPoint> getRevenueChart(String period) {
        log.info("Fetching revenue chart data for period: {}", period);
        List<RevenueChartPoint> points = new ArrayList<>();
        LocalDate today = LocalDate.now();

        if ("7days".equalsIgnoreCase(period)) {
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
                BigDecimal rev = orderRepository.sumTotalAmountBetween(start, end);
                Long cnt = orderRepository.countOrdersBetween(start, end);
                points.add(new RevenueChartPoint(date.format(DateTimeFormatter.ofPattern("dd/MM")), rev == null ? BigDecimal.ZERO : rev, cnt));
            }
        } else if ("30days".equalsIgnoreCase(period)) {
            for (int i = 29; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
                BigDecimal rev = orderRepository.sumTotalAmountBetween(start, end);
                Long cnt = orderRepository.countOrdersBetween(start, end);
                points.add(new RevenueChartPoint(date.format(DateTimeFormatter.ofPattern("dd/MM")), rev == null ? BigDecimal.ZERO : rev, cnt));
            }
        } else if ("12months".equalsIgnoreCase(period)) {
            for (int i = 11; i >= 0; i--) {
                LocalDate date = today.minusMonths(i);
                LocalDate firstDay = date.withDayOfMonth(1);
                LocalDate lastDay = date.withDayOfMonth(date.lengthOfMonth());
                LocalDateTime start = LocalDateTime.of(firstDay, LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(lastDay, LocalTime.MAX);
                BigDecimal rev = orderRepository.sumTotalAmountBetween(start, end);
                Long cnt = orderRepository.countOrdersBetween(start, end);
                points.add(new RevenueChartPoint(date.format(DateTimeFormatter.ofPattern("MM/yyyy")), rev == null ? BigDecimal.ZERO : rev, cnt));
            }
        }

        return points;
    }

    /**
     * Lấy danh sách sản phẩm bán chạy hàng đầu
     */
    public List<ProductListDTO> getTopProducts(int limit) {
        return productRepository.findTopSelling(PageRequest.of(0, limit)).stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Đếm tỷ lệ các trạng thái đơn hàng
     */
    public Map<String, Long> getOrderStatusBreakdown() {
        Map<String, Long> breakdown = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            breakdown.put(status.name(), orderRepository.countByStatus(status));
        }
        return breakdown;
    }

    /**
     * Lấy báo cáo AI Insight từ Redis, nếu trống thì tự động sinh mới
     */
    public String getAiInsight() {
        String cachedInsight = null;
        try {
            cachedInsight = redisTemplate.opsForValue().get(INSIGHT_CACHE_KEY);
        } catch (Exception e) {
            log.warn("Redis is offline or unavailable, skipping cache for AI Insight: {}", e.getMessage());
        }
        
        if (StringUtils.hasText(cachedInsight)) {
            return cachedInsight;
        }
        return generateDailyInsight();
    }

    /**
     * Trigger sinh thủ công báo cáo AI Insight mới
     */
    public String triggerAiInsight() {
        return generateDailyInsight();
    }

    /**
     * Chạy định kỳ 7:00 sáng mỗi ngày để cập nhật AI Insight báo cáo
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void scheduledInsight() {
        log.info("Running daily scheduled task to generate AI Insights report...");
        generateDailyInsight();
    }

    private String generateDailyInsight() {
        log.info("Generating Business AI Insights...");

        // 1. Thu thập dữ liệu
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        BigDecimal totalRevenue7Days = orderRepository.sumTotalAmountBetween(last7Days, LocalDateTime.now());
        if (totalRevenue7Days == null) totalRevenue7Days = BigDecimal.ZERO;

        Long totalOrders7Days = orderRepository.countOrdersBetween(last7Days, LocalDateTime.now());

        // Top 5 sản phẩm bán chạy nhất
        List<Product> top5 = productRepository.findTopSelling(PageRequest.of(0, 5));
        String top5Text = top5.stream()
                .map(p -> p.getName() + " (Đã bán: " + p.getTotalSold() + ")")
                .collect(Collectors.joining(", "));

        // Sản phẩm bán chậm (totalSold = 0)
        List<Product> slowSelling = productRepository.findAll().stream()
                .filter(p -> p.getTotalSold() == null || p.getTotalSold() == 0)
                .limit(5)
                .collect(Collectors.toList());
        String slowText = slowSelling.isEmpty() ? "Không có" : slowSelling.stream().map(Product::getName).collect(Collectors.joining(", "));

        // Các biến thể có lượng tồn kho dưới 10
        List<ProductVariant> lowStock = productVariantRepository.findByStockLessThan(10);
        String lowStockText = lowStock.isEmpty() ? "Không có" : lowStock.stream()
                .map(v -> v.getProduct().getName() + " [Size " + v.getSize() + "] (Còn lại: " + v.getStock() + ")")
                .distinct()
                .limit(5)
                .collect(Collectors.joining(", "));

        // 2. Xây dựng Prompt
        String reportContext = String.format(
                "BÁO CÁO KINH DOANH TIỆM BÁNH BREVERY (7 ngày qua):\n" +
                "- Tổng doanh thu: %sđ\n" +
                "- Tổng đơn hàng thành công: %d đơn\n" +
                "- Top 5 món bán chạy nhất: %s\n" +
                "- Sản phẩm chưa bán được: %s\n" +
                "- Sản phẩm sắp hết kho (dưới 10 cái): %s",
                String.format("%,.0f", totalRevenue7Days), totalOrders7Days, top5Text, slowText, lowStockText
        );

        String systemPrompt = "Bạn là Giám đốc Phân tích Kinh doanh AI của tiệm bánh & nước Brevery.\n" +
                "Nhiệm vụ của bạn là đọc báo cáo số liệu kinh doanh trên và đưa ra phân tích đánh giá cực kỳ chuyên nghiệp bằng tiếng Việt.\n" +
                "Yêu cầu báo cáo:\n" +
                "1. Định dạng Markdown chỉn chu, đẹp mắt, chia các phần rõ ràng (Nhận xét chung, Đề xuất cải thiện thực đơn, Cảnh báo kho hàng).\n" +
                "2. Ngắn gọn, tập trung thẳng vào insight hành động hành vi (dưới 250 từ).\n" +
                "3. Giọng văn năng động, truyền cảm hứng và mang lại giá trị cao cho người quản trị cửa hàng.";

        String insightResult = "";

        if (StringUtils.hasText(aiApiKey)) {
            try {
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(10000);
                requestFactory.setReadTimeout(10000);
                RestTemplate restTemplate = new RestTemplate(requestFactory);

                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + aiApiKey;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                ObjectNode payloadNode = objectMapper.createObjectNode();
                ArrayNode contentsArray = payloadNode.putArray("contents");
                ObjectNode contentNode = contentsArray.addObject();
                ArrayNode partsArray = contentNode.putArray("parts");
                partsArray.addObject().put("text", systemPrompt + "\n\nDữ liệu số liệu thực tế:\n" + reportContext);

                ObjectNode configNode = payloadNode.putObject("generationConfig");
                configNode.put("maxOutputTokens", 500);

                HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payloadNode), headers);
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                    JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
                    JsonNode candidateTextNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                    if (!candidateTextNode.isMissingNode()) {
                        insightResult = candidateTextNode.asText().trim();
                    }
                }
            } catch (Exception e) {
                log.error("Failed to fetch daily business insight from Gemini API", e);
            }
        }

        // Fallback local report if API is not configured or fails
        if (!StringUtils.hasText(insightResult)) {
            log.info("Using Local Fallback Analytics engine.");
            StringBuilder sb = new StringBuilder();
            sb.append("📊 **BÁO CÁO PHÂN TÍCH DOANH THU BREVERY**\n\n");
            sb.append("✨ **Nhận xét chung:**\n");
            sb.append("Cửa hàng ghi nhận doanh thu 7 ngày qua đạt **").append(String.format("%,.0fđ", totalRevenue7Days)).append("** với **").append(totalOrders7Days).append(" đơn hàng** thành công. Hoạt động kinh doanh đang diễn ra ổn định.\n\n");
            sb.append("💡 **Đề xuất thực đơn:**\n");
            sb.append("- Đẩy mạnh quảng bá các sản phẩm bán chạy nhất: *").append(top5Text).append("* qua các chương trình combo giảm giá.\n");
            if (!"Không có".equals(slowText)) {
                sb.append("- Cần xem xét lại công thức chế biến hoặc chạy flash-sale cho các món chưa bán chạy: *").append(slowText).append("* để kích cầu người mua.\n");
            }
            sb.append("\n⚠️ **Cảnh báo tồn kho:**\n");
            if (!"Không có".equals(lowStockText)) {
                sb.append("Cần khẩn trương nhập thêm nguyên liệu cho các sản phẩm sắp hết kho sau: *").append(lowStockText).append("* để tránh gián đoạn dịch vụ giao hàng.");
            } else {
                sb.append("Tất cả các sản phẩm đều đang duy trì mức tồn kho cực kỳ an toàn.");
            }
            insightResult = sb.toString();
        }

        // Lưu vào Redis cache với TTL 25 giờ
        try {
            redisTemplate.opsForValue().set(INSIGHT_CACHE_KEY, insightResult, 25, TimeUnit.HOURS);
            log.info("Successfully updated business AI insights in Redis cache.");
        } catch (Exception e) {
            log.error("Failed to save business insight in Redis", e);
        }

        return insightResult;
    }
}
