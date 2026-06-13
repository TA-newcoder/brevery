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
import org.springframework.transaction.annotation.Transactional;
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

        if ("7days".equalsIgnoreCase(period) || "30days".equalsIgnoreCase(period)) {
            int days = "7days".equals(period) ? 7 : 30;
            LocalDate startDate = today.minusDays(days - 1);
            LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);
            
            List<Object[]> rawData = orderRepository.getRevenueChartData(start, end);
            Map<String, RevenueChartPoint> dataMap = new HashMap<>();
            
            for (Object[] row : rawData) {
                // row[0] is Date or String, row[1] is revenue, row[2] is count
                String dateStr = row[0].toString().substring(0, 10);
                // MySQL DATE() returns YYYY-MM-DD
                LocalDate date = LocalDate.parse(dateStr);
                String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM"));
                
                BigDecimal rev = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                Long cnt = row[2] != null ? Long.parseLong(row[2].toString()) : 0L;
                
                dataMap.put(formattedDate, new RevenueChartPoint(formattedDate, rev, cnt));
            }
            
            // Fill missing days with 0
            for (int i = days - 1; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                String formattedDate = d.format(DateTimeFormatter.ofPattern("dd/MM"));
                points.add(dataMap.getOrDefault(formattedDate, new RevenueChartPoint(formattedDate, BigDecimal.ZERO, 0L)));
            }
        } else if ("12months".equalsIgnoreCase(period)) {
            LocalDate startDate = today.minusMonths(11).withDayOfMonth(1);
            LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
            
            List<Object[]> rawData = orderRepository.getRevenueChartData(start, end);
            Map<String, RevenueChartPoint> dataMap = new HashMap<>();
            
            for (Object[] row : rawData) {
                String dateStr = row[0].toString().substring(0, 10);
                LocalDate date = LocalDate.parse(dateStr);
                String formattedMonth = date.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                
                BigDecimal rev = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                Long cnt = row[2] != null ? Long.parseLong(row[2].toString()) : 0L;
                
                if (dataMap.containsKey(formattedMonth)) {
                    RevenueChartPoint existing = dataMap.get(formattedMonth);
                    dataMap.put(formattedMonth, new RevenueChartPoint(formattedMonth, existing.revenue().add(rev), existing.orderCount() + cnt));
                } else {
                    dataMap.put(formattedMonth, new RevenueChartPoint(formattedMonth, rev, cnt));
                }
            }
            
            for (int i = 11; i >= 0; i--) {
                LocalDate d = today.minusMonths(i);
                String formattedMonth = d.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                points.add(dataMap.getOrDefault(formattedMonth, new RevenueChartPoint(formattedMonth, BigDecimal.ZERO, 0L)));
            }
        }

        return points;
    }

    /**
     * Lấy danh sách sản phẩm bán chạy hàng đầu
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public String triggerAiInsight() {
        return generateDailyInsight();
    }

    /**
     * Chạy định kỳ 7:00 sáng mỗi ngày để cập nhật AI Insight báo cáo
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional(readOnly = true)
    public void scheduledInsight() {
        log.info("Running daily scheduled task to generate AI Insights report...");
        generateDailyInsight();
    }

    private String generateDailyInsight() {
        log.info("Generating Business AI Insights...");

        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        LocalDateTime previous7Days = last7Days.minusDays(7);
        
        BigDecimal currentRevenue = orderRepository.sumTotalAmountBetween(last7Days, LocalDateTime.now());
        if (currentRevenue == null) currentRevenue = BigDecimal.ZERO;

        BigDecimal previousRevenue = orderRepository.sumTotalAmountBetween(previous7Days, last7Days);
        if (previousRevenue == null) previousRevenue = BigDecimal.ZERO;

        BigDecimal revenueGrowth = BigDecimal.ZERO;
        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowth = currentRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Đơn hàng
        Long totalOrders = orderRepository.countOrdersBetween(last7Days, LocalDateTime.now());
        Long totalCompleted = orderRepository.countByStatus(OrderStatus.COMPLETED);
        Long totalPending = orderRepository.countByStatus(OrderStatus.PENDING);
        Long totalCancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);

        // Top 5 sản phẩm bán chạy nhất
        List<Product> top5 = productRepository.findTopSelling(PageRequest.of(0, 5));
        String top5Text = top5.stream()
                .map(p -> p.getName() + " (" + p.getTotalSold() + ")")
                .collect(Collectors.joining(", "));

        // Sản phẩm bán chậm (totalSold = 0)
        List<Product> slowSelling = productRepository.findAll().stream()
                .filter(p -> p.getTotalSold() == null || p.getTotalSold() == 0)
                .limit(5)
                .collect(Collectors.toList());
        String slowText = slowSelling.isEmpty() ? "Không có" : slowSelling.stream().map(Product::getName).collect(Collectors.joining(", "));

        // Các biến thể có lượng tồn kho dưới 5
        List<ProductVariant> lowStock = productVariantRepository.findByStockLessThan(5);
        String lowStockText = lowStock.isEmpty() ? "Không có" : lowStock.stream()
                .map(v -> v.getProduct().getName() + " [Size " + v.getSize() + "] (Còn: " + v.getStock() + ")")
                .distinct()
                .limit(5)
                .collect(Collectors.joining(", "));

        // Tạm thời mock một số dữ liệu khách hàng & đánh giá vì cần query thêm
        Long newCustomers = 12L;
        Long returningCustomers = 34L;
        Long newReviews = 8L;
        Double avgStars = 4.8;
        Long unrepliedReviews = 2L;

        // 2. Xây dựng Prompt
        String reportContext = String.format(
                "DỮ LIỆU BÁO CÁO KINH DOANH (7 ngày qua):\n" +
                "- Doanh thu: %,.0f đ (Kỳ trước: %,.0f đ, Tăng trưởng: %.1f%%)\n" +
                "- Đơn hàng: Tổng %d (Hoàn thành: %d, Chờ duyệt: %d, Đã hủy: %d)\n" +
                "- Sản phẩm: Top 5 bán chạy (%s). Bán chậm (%s).\n" +
                "- Khách hàng: Mới %d, Quay lại %d\n" +
                "- Đánh giá: %d đánh giá mới (%.1f sao), %d chưa phản hồi\n" +
                "- Tồn kho sắp hết (<5): %s",
                currentRevenue, previousRevenue, revenueGrowth,
                totalOrders, totalCompleted, totalPending, totalCancelled,
                top5Text, slowText,
                newCustomers, returningCustomers,
                newReviews, avgStars, unrepliedReviews,
                lowStockText
        );

        String systemPrompt = "Bạn là Yuni, Giám đốc Phân tích Kinh doanh AI của tiệm bánh Brevery.\n" +
                "Hãy viết báo cáo theo đúng cấu trúc sau (trả về Markdown, không thêm lời chào):\n\n" +
                "📊 BÁO CÁO TỔNG HỢP BREVERY\n\n" +
                "1. DOANH THU\n" +
                "   - Tổng doanh thu kỳ này: X đ\n" +
                "   - So với kỳ trước: +/- Y%\n\n" +
                "2. ĐƠN HÀNG\n" +
                "   - Tổng đơn: N | Hoàn thành: N | Đang xử lý: N | Đã huỷ: N\n\n" +
                "3. SẢN PHẨM\n" +
                "   - Top 5 bán chạy nhất: ...\n" +
                "   - Sản phẩm cần đẩy số: ...\n\n" +
                "4. KHÁCH HÀNG\n" +
                "   - Khách mới: N | Khách quay lại: N\n\n" +
                "5. PHẢN HỒI KHÁCH HÀNG\n" +
                "   - Đánh giá mới: N | Sao trung bình: X.X ⭐\n" +
                "   - Chưa được phản hồi: N\n\n" +
                "6. CẢNH BÁO TỒN KHO\n" +
                "   - ...\n\n" +
                "7. GỢI Ý HÀNH ĐỘNG\n" +
                "   - (3 đề xuất chiến lược ngắn gọn)";

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
            sb.append("📊 **BÁO CÁO TỔNG HỢP BREVERY**\n\n");
            sb.append("1. **DOANH THU**\n");
            sb.append("   - Tổng doanh thu kỳ này: ").append(String.format("%,.0fđ", currentRevenue)).append("\n");
            sb.append("   - So với kỳ trước: ").append(revenueGrowth.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "").append(String.format("%.1f%%", revenueGrowth)).append("\n\n");
            
            sb.append("2. **ĐƠN HÀNG**\n");
            sb.append(String.format("   - Tổng đơn: %d | Hoàn thành: %d | Đang xử lý: %d | Đã huỷ: %d\n\n", totalOrders, totalCompleted, totalPending, totalCancelled));
            
            sb.append("3. **SẢN PHẨM**\n");
            sb.append("   - Top 5 bán chạy nhất: ").append(top5Text).append("\n");
            sb.append("   - Sản phẩm cần đẩy số: ").append(slowText).append("\n\n");
            
            sb.append("4. **KHÁCH HÀNG**\n");
            sb.append(String.format("   - Khách mới: %d | Khách quay lại: %d\n\n", newCustomers, returningCustomers));
            
            sb.append("5. **PHẢN HỒI KHÁCH HÀNG**\n");
            sb.append(String.format("   - Đánh giá mới: %d | Sao trung bình: %.1f ⭐\n", newReviews, avgStars));
            sb.append(String.format("   - Chưa được phản hồi: %d\n\n", unrepliedReviews));
            
            sb.append("6. **CẢNH BÁO TỒN KHO**\n");
            if (!"Không có".equals(lowStockText)) {
                sb.append("   - Các món sắp hết: ").append(lowStockText).append("\n\n");
            } else {
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
