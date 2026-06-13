package com.brevery.service;

import com.brevery.dto.request.CreateOrderRequest;
import com.brevery.dto.request.CreateReviewRequest;
import com.brevery.dto.response.OrderResponse;
import com.brevery.entity.*;
import com.brevery.enums.ErrorCode;
import com.brevery.enums.OrderStatus;
import com.brevery.enums.PaymentStatus;
import com.brevery.exception.AppException;
import com.brevery.mapper.OrderMapper;
import com.brevery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ShippingDetailRepository shippingDetailRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CouponRepository couponRepository;
    private final UserCouponUsageRepository userCouponUsageRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.info("Creating order. User: {}, Payment Method: {}", userId, request.getPaymentMethod());

        User user = null;
        List<OrderDetail> orderDetailsToSave = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        // 1. Thu thập danh sách sản phẩm mua hàng
        if (userId != null) {
            // User flow: lấy từ database CartItem
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            List<CartItem> cartItems = cartItemRepository.findByUserUserId(userId);
            if (cartItems.isEmpty()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Giỏ hàng trống. Không thể đặt hàng.");
            }

            for (CartItem item : cartItems) {
                ProductVariant variant = item.getVariant();
                validateAndDeductStock(variant, item.getQuantity());

                BigDecimal sub = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                OrderDetail detail = OrderDetail.builder()
                        .variant(variant)
                        .productName(variant.getProduct().getName())
                        .variantInfo("Size " + variant.getSize())
                        .unitPrice(variant.getPrice())
                        .quantity(item.getQuantity())
                        .subTotal(sub)
                        .build();

                orderDetailsToSave.add(detail);
                subTotal = subTotal.add(sub);
            }

            // Xóa sạch giỏ hàng DB
            cartItemRepository.deleteByUserUserId(userId);

        } else {
            // Guest flow: lấy trực tiếp từ items trong request
            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Danh sách sản phẩm mua hàng trống.");
            }

            for (CreateOrderRequest.CartItemRequest item : request.getItems()) {
                ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                        .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                validateAndDeductStock(variant, item.getQuantity());

                BigDecimal sub = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                OrderDetail detail = OrderDetail.builder()
                        .variant(variant)
                        .productName(variant.getProduct().getName())
                        .variantInfo("Size " + variant.getSize())
                        .unitPrice(variant.getPrice())
                        .quantity(item.getQuantity())
                        .subTotal(sub)
                        .build();

                orderDetailsToSave.add(detail);
                subTotal = subTotal.add(sub);
            }
        }

        // 2. Tính toán giảm giá Coupon
        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            discountAmount = couponService.validateAndCalculateDiscount(request.getCouponCode(), subTotal, userId);
            appliedCoupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode()).orElse(null);
        }
        BigDecimal finalAmount = subTotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // 3. Tạo mã đơn hàng độc nhất định dạng: BRV-yyyyMMdd-XXXXX
        String orderCode = generateOrderCode();
        String trackingToken = UUID.randomUUID().toString();

        // 4. Khởi tạo đơn hàng
        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .subTotal(subTotal)
                .discountAmount(discountAmount)
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(finalAmount)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .note(request.getNote())
                .trackingToken(trackingToken)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 5. Lưu Order Details
        for (OrderDetail detail : orderDetailsToSave) {
            detail.setOrder(savedOrder);
            orderDetailRepository.save(detail);
        }
        savedOrder.setOrderDetails(orderDetailsToSave);

        // 6. Lưu Shipping Details
        ShippingDetail shippingDetail = ShippingDetail.builder()
                .order(savedOrder)
                .recipientName(request.getReceiverName())
                .phone(request.getReceiverPhone())
                .province("")
                .district("")
                .ward("")
                .addressDetail(request.getShippingAddress())
                .build();
        shippingDetailRepository.save(shippingDetail);
        savedOrder.setShippingDetail(shippingDetail);

        // 7. Cập nhật Coupon lượt dùng
        if (appliedCoupon != null) {
            appliedCoupon.setUsedCount(appliedCoupon.getUsedCount() + 1);
            couponRepository.save(appliedCoupon);

            if (userId != null) {
                UserCouponUsage usage = UserCouponUsage.builder()
                        .user(user)
                        .coupon(appliedCoupon)
                        .order(savedOrder)
                        .build();
                userCouponUsageRepository.save(usage);
            }
        }

        OrderResponse response = orderMapper.toResponse(savedOrder);
        notificationService.sendNewOrderNotification(response);

        // Gửi email xác nhận
        String recipientEmail = user != null ? user.getEmail() : request.getGuestEmail();
        if (recipientEmail != null && !recipientEmail.isBlank()) {
            emailService.sendOrderConfirmationEmail(recipientEmail, request.getReceiverName(), orderCode, finalAmount);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserUserId(userId, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse trackOrder(String orderCode, String phone) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Không tìm thấy đơn hàng với mã được cấp"));

        ShippingDetail shippingDetail = order.getShippingDetail();
        if (shippingDetail == null || !shippingDetail.getPhone().trim().equals(phone.trim())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Thông tin số điện thoại không khớp với đơn hàng");
        }

        return orderMapper.toResponse(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Đơn hàng không tồn tại"));

        if (status != null) {
            // Validate flow: PENDING -> CONFIRMED -> SHIPPING -> DELIVERED
            OrderStatus current = order.getStatus();
            if (current == OrderStatus.CANCELLED) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Không thể cập nhật đơn hàng đã hủy");
            }
            if (current == OrderStatus.COMPLETED) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Không thể cập nhật đơn hàng đã giao thành công");
            }

            // Check transitions
            boolean isValidTransition = false;
            if (current == OrderStatus.PENDING) {
                isValidTransition = (status == OrderStatus.CONFIRMED || status == OrderStatus.CANCELLED);
            } else if (current == OrderStatus.CONFIRMED) {
                isValidTransition = (status == OrderStatus.PREPARING || status == OrderStatus.CANCELLED);
            } else if (current == OrderStatus.PREPARING) {
                isValidTransition = (status == OrderStatus.SHIPPED || status == OrderStatus.CANCELLED);
            } else if (current == OrderStatus.SHIPPED) {
                isValidTransition = (status == OrderStatus.DELIVERING || status == OrderStatus.CANCELLED);
            } else if (current == OrderStatus.DELIVERING) {
                isValidTransition = (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED);
            }

            if (!isValidTransition) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "Chuyển đổi trạng thái không hợp lệ: từ " + current + " sang " + status);
            }

            // Nếu hủy đơn thì phải hoàn lại kho
            if (status == OrderStatus.CANCELLED) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    ProductVariant variant = detail.getVariant();
                    variant.setStock(variant.getStock() + detail.getQuantity());
                    if (variant.getStock() > 0) {
                        variant.setIsAvailable(true);
                    }
                    productVariantRepository.save(variant);
                }
            }

            order.setStatus(status);
        }

        if (paymentStatus != null) {
            order.setPaymentStatus(paymentStatus);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Admin updated order {} status to {}, payment to {}", orderId, status, paymentStatus);

        // Gửi WebSocket notify
        OrderResponse response = orderMapper.toResponse(savedOrder);
        notificationService.sendAdminNotification(response);
        if (savedOrder.getUser() != null) {
            notificationService.sendUserNotification(savedOrder.getUser().getUserId(), response);
            
            // Gửi email cập nhật trạng thái
            if (status != null && savedOrder.getUser().getEmail() != null && !savedOrder.getUser().getEmail().isBlank()) {
                emailService.sendOrderStatusUpdateEmail(savedOrder.getUser().getEmail(), savedOrder.getUser().getFullName(), savedOrder.getOrderCode(), status);
            }
        }
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Đơn hàng không tồn tại"));

        if (userId != null && (order.getUser() == null || !order.getUser().getUserId().equals(userId))) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể hủy đơn hàng khi trạng thái là PENDING");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.FAILED);

        // Hoàn lại kho
        for (OrderDetail detail : order.getOrderDetails()) {
            ProductVariant variant = detail.getVariant();
            variant.setStock(variant.getStock() + detail.getQuantity());
            if (variant.getStock() > 0) {
                variant.setIsAvailable(true);
            }
            productVariantRepository.save(variant);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} cancelled by user {}", orderId, userId);

        OrderResponse response = orderMapper.toResponse(savedOrder);
        notificationService.sendAdminNotification(response);
        
        // Gửi email cập nhật trạng thái hủy
        if (savedOrder.getUser() != null && savedOrder.getUser().getEmail() != null && !savedOrder.getUser().getEmail().isBlank()) {
            emailService.sendOrderStatusUpdateEmail(savedOrder.getUser().getEmail(), savedOrder.getUser().getFullName(), savedOrder.getOrderCode(), OrderStatus.CANCELLED);
        }
    }

    @Transactional
    public void addReview(Long orderId, Long userId, CreateReviewRequest reviewRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Đơn hàng không tồn tại"));

        if (userId != null && (order.getUser() == null || !order.getUser().getUserId().equals(userId))) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền đánh giá sản phẩm của đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã giao thành công");
        }

        // Check if product was in this order
        boolean productInOrder = order.getOrderDetails().stream()
                .anyMatch(detail -> detail.getVariant().getProduct().getProductId().equals(reviewRequest.getProductId()));
        if (!productInOrder) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Sản phẩm không có trong đơn hàng này");
        }

        // Check if already reviewed
        boolean alreadyReviewed = reviewRepository.existsByProductProductIdAndUserUserIdAndOrderOrderId(
                reviewRequest.getProductId(), userId, orderId);
        if (alreadyReviewed) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi");
        }

        Product product = productRepository.findById(reviewRequest.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Review review = Review.builder()
                .order(order)
                .product(product)
                .user(user)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .status("PENDING")
                .build();

        reviewRepository.save(review);
        log.info("User {} reviewed product {} in order {}", userId, reviewRequest.getProductId(), orderId);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersForAdmin(OrderStatus status, LocalDateTime fromDate, LocalDateTime toDate, Long userId, String orderCode, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Order> spec = OrderSpecification.filterOrders(status, fromDate, toDate, userId, orderCode);
        return orderRepository.findAll(spec, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrdersForAdminList(OrderStatus status, LocalDateTime fromDate, LocalDateTime toDate, Long userId, String orderCode) {
        org.springframework.data.jpa.domain.Specification<Order> spec = OrderSpecification.filterOrders(status, fromDate, toDate, userId, orderCode);
        return orderRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public byte[] exportOrdersToExcel(OrderStatus status, LocalDateTime fromDate, LocalDateTime toDate, Long userId, String orderCode) {
        List<Order> orders = getAllOrdersForAdminList(status, fromDate, toDate, userId, orderCode);

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Orders");

            // Header Row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã Đơn Hàng", "Khách Hàng", "Tổng Tiền Mua", "Giảm Giá", "Thanh Toán", "Trạng Thái Đơn", "Trạng Thái Thanh Toán", "Địa Chỉ Nhận", "Số Điện Thoại", "Ngày Tạo"};

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Order order : orders) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(order.getOrderCode());
                row.createCell(1).setCellValue(order.getUser() != null ? order.getUser().getFullName() : "Khách vãng lai");
                row.createCell(2).setCellValue(order.getSubTotal().doubleValue());
                row.createCell(3).setCellValue(order.getDiscountAmount().doubleValue());
                row.createCell(4).setCellValue(order.getTotalAmount().doubleValue());
                row.createCell(5).setCellValue(order.getStatus().toString());
                row.createCell(6).setCellValue(order.getPaymentStatus().toString());

                String address = order.getShippingDetail() != null ? order.getShippingDetail().getAddressDetail() : "";
                String phone = order.getShippingDetail() != null ? order.getShippingDetail().getPhone() : "";
                row.createCell(7).setCellValue(address);
                row.createCell(8).setCellValue(phone);
                row.createCell(9).setCellValue(order.getCreatedAt().toString());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to export orders to Excel", e);
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Không thể xuất file Excel. Chi tiết: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private void validateAndDeductStock(ProductVariant variant, int quantity) {
        if (!Boolean.TRUE.equals(variant.getIsAvailable()) || variant.getStock() < quantity) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Sản phẩm size " + variant.getSize() + " không đủ số lượng tồn kho khả dụng");
        }
        variant.setStock(variant.getStock() - quantity);
        if (variant.getStock() == 0) {
            variant.setIsAvailable(false);
        }
        productVariantRepository.save(variant);
    }

    private String generateOrderCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rand = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "BRV-" + dateStr + "-" + rand;
    }
}
