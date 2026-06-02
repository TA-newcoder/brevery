package com.brevery.service;

import com.brevery.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo đơn hàng mới realtime cho Admin qua WebSocket topic /topic/admin/orders
     */
    public void sendNewOrderNotification(OrderResponse order) {
        log.info("Sending realtime WebSocket notification for order: {}", order.getOrderCode());
        sendAdminNotification(order);
    }

    /**
     * Gửi thông báo chung cho Admin
     */
    public void sendAdminNotification(Object payload) {
        try {
            messagingTemplate.convertAndSend("/topic/admin/orders", payload);
        } catch (Exception e) {
            log.error("Failed to send admin WebSocket notification", e);
        }
    }

    /**
     * Gửi thông báo riêng cho User
     */
    public void sendUserNotification(Long userId, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", payload);
        } catch (Exception e) {
            log.error("Failed to send user WebSocket notification to user: {}", userId, e);
        }
    }
}
