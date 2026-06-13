package com.brevery.controller;

import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.OrderResponse;
import com.brevery.enums.OrderStatus;
import com.brevery.enums.PaymentStatus;
import com.brevery.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Order - Admin", description = "Quản lý, tìm kiếm bộ lọc đơn hàng nâng cao, cập nhật trạng thái và xuất Excel")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Xem danh sách đơn hàng nâng cao (Có bộ lọc trạng thái, khoảng ngày, người dùng và phân trang)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderResponse> orders = orderService.getAllOrdersForAdmin(status, fromDate, toDate, userId, orderCode, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Cập nhật trạng thái giao hàng và trạng thái thanh toán của đơn hàng (Có validate chuyển đổi hợp lệ)")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus) {

        orderService.updateOrderStatus(orderId, status, paymentStatus);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái đơn hàng thành công"));
    }

    @GetMapping("/export")
    @Operation(summary = "Xuất Excel danh sách đơn hàng theo bộ lọc hiện tại")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderCode) {

        byte[] excelBytes = orderService.exportOrdersToExcel(status, fromDate, toDate, userId, orderCode);
        String filename = "Orders_Export_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
