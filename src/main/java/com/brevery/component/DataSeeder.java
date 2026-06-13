package com.brevery.component;

import com.brevery.entity.*;
import com.brevery.enums.*;
import com.brevery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final InventoryReceiptRepository inventoryReceiptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            log.info("Database already seeded. Skipping DataSeeder.");
            return;
        }
        
        log.info("Starting comprehensive data seeding...");

        // 1. Seed Users (reuse existing ones from DataInitializer, add new customers)
        List<User> users = new ArrayList<>();
        
        // Reuse existing admin if already created by DataInitializer
        User adminUser = userRepository.findByEmail("admin@brevery.vn").orElse(null);
        if (adminUser != null) {
            users.add(adminUser);
        } else {
            User newAdmin = User.builder().email("admin@brevery.vn").fullName("Admin Brevery").phone("0909000111")
                    .passwordHash(passwordEncoder.encode("password")).role(Role.ADMIN).isEmailVerified(true).build();
            users.add(userRepository.save(newAdmin));
        }
        
        // Reuse existing guest if present
        User guestUser = userRepository.findByEmail("khach@brevery.vn").orElse(null);
        if (guestUser != null) {
            users.add(guestUser);
        }
        
        // Create additional customers (skip if already exists)
        for (int i = 1; i <= 15; i++) {
            String email = "customer" + i + "@gmail.com";
            User existing = userRepository.findByEmail(email).orElse(null);
            if (existing != null) {
                users.add(existing);
            } else {
                User newUser = User.builder().email(email).fullName("Khách Hàng " + i).phone("0909000" + String.format("%03d", i))
                        .passwordHash(passwordEncoder.encode("password")).role(Role.USER).isEmailVerified(true).build();
                users.add(userRepository.save(newUser));
            }
        }
        log.info("Seeded/Reused {} users.", users.size());

        // 2. Seed Categories
        String[] categoryNames = {"Bánh kem", "Bánh mì", "Bánh ngọt", "Trà sữa", "Cà phê"};
        List<Category> categories = new ArrayList<>();
        for (String catName : categoryNames) {
            categories.add(Category.builder().name(catName).description("Danh mục " + catName).isActive(true).build());
        }
        categoryRepository.saveAll(categories);
        log.info("Seeded 5 categories.");

        // 3. Seed Products (20+) & Variants & Inventory Receipts
        List<Product> products = new ArrayList<>();
        Random random = new Random();
        for (int i = 1; i <= 25; i++) {
            Category category = categories.get(i % 5);
            Product product = Product.builder()
                    .category(category)
                    .name("Sản phẩm " + category.getName() + " Premium " + i)
                    .description("Sản phẩm tuyệt hảo từ nguyên liệu tươi mới mỗi ngày. Mang lại hương vị khó quên.")
                    .isAvailable(true)
                    .totalSold(random.nextInt(200))
                    .lowStockThreshold(10)
                    .variants(new ArrayList<>())
                    .build();

            int price = (random.nextInt(100) + 30) * 1000;
            ProductVariant variantM = ProductVariant.builder()
                    .product(product)
                    .size("M")
                    .price(new BigDecimal(price))
                    .stock(random.nextInt(50) + 5) // Some might be low stock
                    .isAvailable(true)
                    .build();
            
            product.getVariants().add(variantM);

            if (random.nextBoolean()) {
                ProductVariant variantL = ProductVariant.builder()
                        .product(product)
                        .size("L")
                        .price(new BigDecimal(price + 15000))
                        .stock(random.nextInt(30) + 2)
                        .isAvailable(true)
                        .build();
                product.getVariants().add(variantL);
            }
            products.add(product);
        }
        productRepository.saveAll(products);
        log.info("Seeded 25 products with variants.");

        // Inventory Receipts (10+)
        List<InventoryReceipt> receipts = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Product p = products.get(random.nextInt(products.size()));
            receipts.add(InventoryReceipt.builder()
                    .product(p)
                    .quantity(random.nextInt(100) + 20)
                    .importPrice(p.getVariants().get(0).getPrice().multiply(new BigDecimal("0.6"))) // 60% of retail
                    .supplier("Nhà cung cấp Nguyên Liệu Sạch VN")
                    .note("Nhập hàng đợt " + (i + 1))
                    .build());
        }
        inventoryReceiptRepository.saveAll(receipts);
        log.info("Seeded 15 inventory receipts.");

        // 4. Seed Orders (80+ across 12 months for good chart data)
        List<Order> orders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Status distribution: 60% COMPLETED, 15% PENDING/CONFIRMED, 10% PREPARING/SHIPPING, 10% DELIVERING, 5% CANCELLED
        OrderStatus[] weightedStatuses = {
            OrderStatus.COMPLETED, OrderStatus.COMPLETED, OrderStatus.COMPLETED, OrderStatus.COMPLETED, OrderStatus.COMPLETED, OrderStatus.COMPLETED,
            OrderStatus.PENDING, OrderStatus.CONFIRMED,
            OrderStatus.PREPARING, OrderStatus.SHIPPED,
            OrderStatus.DELIVERING,
            OrderStatus.CANCELLED
        };
        
        String[] addresses = {
            "123 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP. HCM",
            "456 Đường Nguyễn Trãi, Phường 3, Quận 5, TP. HCM",
            "789 Đường Cách Mạng Tháng 8, Phường 7, Quận 10, TP. HCM",
            "12 Đường Trần Hưng Đạo, Phường 1, Quận 5, TP. HCM",
            "55 Đường Hai Bà Trưng, Phường Đa Kao, Quận 1, TP. HCM"
        };
        
        for (int i = 1; i <= 80; i++) {
            User customer = users.get(random.nextInt(15) + 1); // skip admin
            OrderStatus status = weightedStatuses[random.nextInt(weightedStatuses.length)];
            
            BigDecimal subTotal = BigDecimal.ZERO;
            List<OrderDetail> details = new ArrayList<>();
            int numItems = random.nextInt(4) + 1;
            for (int j = 0; j < numItems; j++) {
                Product p = products.get(random.nextInt(products.size()));
                ProductVariant v = p.getVariants().get(0);
                int qty = random.nextInt(3) + 1;
                BigDecimal itemPrice = v.getPrice();
                subTotal = subTotal.add(itemPrice.multiply(new BigDecimal(qty)));
                
                details.add(OrderDetail.builder()
                        .productName(p.getName())
                        .variantInfo("Size " + v.getSize())
                        .variant(v).quantity(qty).unitPrice(itemPrice).subTotal(itemPrice.multiply(new BigDecimal(qty)))
                        .build());
            }

            BigDecimal shipping = new BigDecimal("30000");
            BigDecimal total = subTotal.add(shipping);
            String addr = addresses[random.nextInt(addresses.length)];

            Order order = Order.builder()
                    .orderCode("BRV-" + System.currentTimeMillis() + "-" + i)
                    .user(customer)
                    .status(status)
                    .paymentMethod(PaymentMethod.VNPAY)
                    .paymentStatus(PaymentStatus.PAID)
                    .trackingToken(UUID.randomUUID().toString())
                    .subTotal(subTotal).shippingFee(shipping).totalAmount(total)
                    .orderDetails(new ArrayList<>())
                    .build();

            for (OrderDetail d : details) {
                d.setOrder(order);
                order.getOrderDetails().add(d);
            }

            ShippingDetail shippingDetail = ShippingDetail.builder()
                    .order(order).recipientName(customer.getFullName()).phone(customer.getPhone())
                    .addressDetail(addr)
                    .province("HCM")
                    .district("Q" + (random.nextInt(10) + 1))
                    .ward("P" + (random.nextInt(15) + 1))
                    .build();
            order.setShippingDetail(shippingDetail);

            orders.add(order);
        }
        orderRepository.saveAll(orders);
        
        // Update createdAt to spread across 12 months evenly
        for (int idx = 0; idx < orders.size(); idx++) {
            Order o = orders.get(idx);
            LocalDateTime randomDate;
            if (idx < 5) {
                // 5 orders today for dashboard
                randomDate = now.minusHours(random.nextInt(12)).minusMinutes(random.nextInt(60));
            } else if (idx < 15) {
                // 10 orders this week
                randomDate = now.minusDays(random.nextInt(7)).minusHours(random.nextInt(24));
            } else {
                // Rest spread across 12 months
                int monthsAgo = (idx - 15) % 12;
                int dayInMonth = random.nextInt(28) + 1;
                randomDate = now.minusMonths(monthsAgo).withDayOfMonth(Math.min(dayInMonth, 28)).minusHours(random.nextInt(24));
            }
            jdbcTemplate.update("UPDATE orders SET created_at = ? WHERE order_id = ?", randomDate, o.getOrderId());
        }
        log.info("Seeded 80 orders spread across 12 months with revenue data.");

        // 5. Seed Reviews (50+)
        List<Review> reviews = new ArrayList<>();
        String[] comments = {
            "Bánh rất ngon, giao hàng nhanh!", "Tuyệt vời, sẽ ủng hộ shop dài dài.", 
            "Đóng gói cẩn thận nhưng vị hơi ngọt so với mình.", "Chất lượng tốt, giá hợp lý.", 
            "Giao hàng hơi chậm, bù lại bánh ngon.", "Rất đáng tiền, nhân viên dễ thương."
        };
        String[] reviewStatuses = {"PENDING", "APPROVED", "HIDDEN"};
        for (int i = 0; i < 60; i++) {
            Order o = orders.get(random.nextInt(orders.size()));
            Product p = o.getOrderDetails().get(0).getVariant().getProduct();
            User u = o.getUser();
            int rating = random.nextInt(3) + 3; // 3 to 5 stars
            String c = comments[random.nextInt(comments.length)];
            String st = reviewStatuses[random.nextInt(reviewStatuses.length)];
            
            Review rev = Review.builder()
                    .order(o).product(p).user(u)
                    .rating(rating).comment(c).status(st)
                    .adminReply(st.equals("APPROVED") && random.nextBoolean() ? "Cảm ơn bạn đã ủng hộ Brevery!" : null)
                    .build();
            reviews.add(rev);
        }
        reviewRepository.saveAll(reviews);
        log.info("Seeded 60 reviews.");

        log.info("ALL SEED DATA SUCCESSFULLY CREATED!");
    }
}
