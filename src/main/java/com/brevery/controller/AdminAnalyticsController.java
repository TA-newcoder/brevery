package com.brevery.controller;

import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.ProductListDTO;
import com.brevery.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Analytics - Admin", description = "Các API phân tích kinh doanh, KPI dashboard realtime và báo cáo AI Insights")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Lấy thống kê KPI dashboard kinh doanh ngày/tháng hiện tại")
    public ResponseEntity<ApiResponse<AnalyticsService.KpiSummary>> getSummary() {
        AnalyticsService.KpiSummary summary = analyticsService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/revenue-chart")
    @Operation(summary = "Lấy dữ liệu vẽ biểu đồ doanh thu theo chu kỳ (7days | 30days | 12months)")
    public ResponseEntity<ApiResponse<List<AnalyticsService.RevenueChartPoint>>> getRevenueChart(
            @RequestParam(defaultValue = "7days") String period) {
        List<AnalyticsService.RevenueChartPoint> points = analyticsService.getRevenueChart(period);
        return ResponseEntity.ok(ApiResponse.success(points));
    }

    @GetMapping("/top-products")
    @Operation(summary = "Lấy danh sách các sản phẩm bán chạy nhất")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductListDTO> topProducts = analyticsService.getTopProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(topProducts));
    }

    @GetMapping("/order-status-breakdown")
    @Operation(summary = "Tỷ lệ phân phối số lượng của từng trạng thái đơn hàng")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getOrderStatusBreakdown() {
        Map<String, Long> breakdown = analyticsService.getOrderStatusBreakdown();
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    @GetMapping("/insight")
    @Operation(summary = "Xem báo cáo nhận xét đánh giá AI Insights tự động của tiệm")
    public ResponseEntity<ApiResponse<String>> getAiInsight() {
        String insight = analyticsService.getAiInsight();
        return ResponseEntity.ok(ApiResponse.success(insight));
    }

    @PostMapping("/insight/trigger")
    @Operation(summary = "Yêu cầu AI lập tức phân tích và tạo báo cáo kinh doanh mới tức thời")
    public ResponseEntity<ApiResponse<String>> triggerAiInsight() {
        String insight = analyticsService.triggerAiInsight();
        return ResponseEntity.ok(ApiResponse.success(insight));
    }
}
