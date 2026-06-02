package com.brevery.service;

import com.brevery.entity.Order;
import com.brevery.entity.ProductVariant;
import com.brevery.entity.User;
import com.brevery.enums.OrderStatus;
import com.brevery.enums.Role;
import com.brevery.exception.AppException;
import com.brevery.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private com.brevery.mapper.OrderMapper orderMapper;

    @Mock
    private com.brevery.service.NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private ProductVariant testVariant;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@test.com");
        testUser.setRole(Role.USER);

        testVariant = new ProductVariant();
        testVariant.setVariantId(1L);
        testVariant.setStock(1);
        testVariant.setPrice(new java.math.BigDecimal("50000.0"));

        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
    }

    @Test
    void testCancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        orderService.cancelOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testCancelOrder_Fail_WrongUser() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        AppException exception = assertThrows(AppException.class, () -> {
            orderService.cancelOrder(1L, 2L); // User ID 2 tries to cancel User 1's order
        });

        assertEquals(com.brevery.enums.ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(orderRepository, never()).save(any());
    }
}
