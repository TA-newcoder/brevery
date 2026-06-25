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
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            log.info("Database already seeded. Skipping DataSeeder.");
            return;
        }
        
        log.info("Starting comprehensive data seeding...");
        
        try {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
            log.info("Cleared Redis cache.");
        } catch (Exception e) {
            log.warn("Failed to clear Redis cache: {}", e.getMessage());
        }

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
        String[] categoryNames = {"Bánh quy đóng hộp", "Bánh xốp đóng thùng", "Nước khoáng chai", "Nước ngọt đóng lon", "Nước trái cây chai thủy tinh"};
        List<Category> categories = new ArrayList<>();
        for (String catName : categoryNames) {
            categories.add(Category.builder().name(catName).description("Danh mục phân phối " + catName).isActive(true).build());
        }
        categoryRepository.saveAll(categories);
        log.info("Seeded 5 categories.");

        // 3. Seed Products with detailed descriptions for AI recommendation
        List<Product> products = new ArrayList<>();
        Random random = new Random();
        
        // Define products per category with rich descriptions
        String[][] productData = {
            // Category 0: Bánh quy đóng hộp
            {"Bánh quy bơ Danisa", "Bánh quy bơ Danisa nhập khẩu Đan Mạch. Vị bơ thơm béo, độ ngọt vừa phải, giòn tan trong miệng. Thành phần: bơ, bột mì, đường, trứng. Thích hợp làm quà biếu, ăn kèm trà chiều. HSD: 12 tháng.", "0"},
            {"Bánh quy ít đường Julie's", "Bánh quy Julie's Less Sugar nhập khẩu Malaysia. Giảm 40% đường so với bánh thường, VỊ ÍT NGỌT phù hợp người ăn kiêng hoặc người lớn tuổi. Giòn xốp, vị bơ nhẹ. HSD: 10 tháng.", "0"},
            {"Bánh quy socola Oreo", "Bánh quy Oreo nhân kem vani. Vị socola đậm đà, kem ngọt dịu. Thích hợp cho trẻ em và người thích ăn vặt. Đóng hộp tiện lợi, dễ bảo quản. HSD: 9 tháng.", "0"},
            {"Bánh quy phô mai Kjeldsens", "Bánh quy phô mai Kjeldsens nhập khẩu Đan Mạch. Vị phô mai mặn nhẹ, KHÔNG NGỌT, giòn rụm. Rất thích hợp ăn kèm rượu vang hoặc trà đen. HSD: 12 tháng.", "0"},
            {"Bánh quy ngũ cốc Bahlsen", "Bánh quy ngũ cốc Leibniz Bahlsen từ Đức. VỊ ÍT NGỌT, giàu chất xơ từ yến mạch và hạt lanh. Phù hợp người ăn healthy, tập gym. Không chứa cholesterol. HSD: 14 tháng.", "0"},
            
            // Category 1: Bánh xốp đóng thùng  
            {"Bánh xốp kem sữa Nabati", "Bánh xốp Nabati nhân kem sữa, nhập khẩu Indonesia. Vị ngọt dịu, xốp nhẹ, tan nhanh. Thích hợp ăn vặt hàng ngày. Đóng thùng 20 gói tiện lợi phân phối. HSD: 12 tháng.", "1"},
            {"Bánh xốp socola Tango", "Bánh xốp Tango vị socola đậm, NGỌT VỪA. Lớp xốp giòn kẹp kem socola béo ngậy. Phổ biến trong các tiệm tạp hóa. Đóng thùng 24 gói. HSD: 10 tháng.", "1"},
            {"Bánh xốp matcha Collon", "Bánh xốp cuộn Collon vị matcha trà xanh Nhật Bản. VỊ THANH, ÍT NGỌT, hương trà xanh tự nhiên. Thích hợp cho người thích vị nhẹ nhàng. Đóng hộp nhỏ gọn. HSD: 8 tháng.", "1"},
            {"Bánh xốp phô mai Bika", "Bánh xốp Bika vị phô mai Malaysia. Giòn tan, vị phô mai MẶN NHẸ KHÔNG NGỌT. Rất được ưa chuộng tại Đông Nam Á. Đóng thùng 20 gói. HSD: 12 tháng.", "1"},
            {"Bánh xốp dâu tây Richeese", "Bánh xốp Richeese nhân kem dâu tây. Vị ngọt thanh, hương dâu tự nhiên. Màu sắc bắt mắt, trẻ em rất thích. Đóng thùng 30 gói nhỏ. HSD: 10 tháng.", "1"},

            // Category 2: Nước khoáng chai
            {"Nước khoáng Evian 500ml", "Nước khoáng thiên nhiên Evian nhập khẩu Pháp. Nguồn nước từ dãy Alps, không ga, pH trung tính 7.2. Tinh khiết, KHÔNG VỊ, KHÔNG ĐƯỜNG. Phù hợp mọi lứa tuổi. Chai nhựa 500ml.", "2"},
            {"Nước khoáng Vĩnh Hảo 1.5L", "Nước khoáng thiên nhiên Vĩnh Hảo, nguồn nước khoáng Bình Thuận. Có ga nhẹ tự nhiên, giàu khoáng chất. KHÔNG ĐƯỜNG, KHÔNG CALO. Chai 1.5L dùng gia đình. HSD: 12 tháng.", "2"},
            {"Nước khoáng Lavie 350ml", "Nước khoáng Lavie tinh khiết, thương hiệu Nestlé. Không ga, nhẹ nhàng, dễ uống. KHÔNG VỊ, KHÔNG ĐƯỜNG. Chai nhỏ 350ml tiện mang theo. Thùng 24 chai.", "2"},
            {"Nước khoáng Aquafina 500ml", "Nước tinh khiết Aquafina, công nghệ lọc HydRO-7 của PepsiCo. Tinh khiết tuyệt đối, KHÔNG ĐƯỜNG KHÔNG GA. Chai 500ml, phổ biến tại các cửa hàng tiện lợi. Thùng 24 chai.", "2"},
            {"Nước khoáng Dasani 1.5L", "Nước tinh khiết Dasani thuộc tập đoàn Coca-Cola. Bổ sung khoáng chất thiết yếu. KHÔNG VỊ, KHÔNG CALO. Chai 1.5L phù hợp văn phòng và gia đình. Thùng 12 chai.", "2"},
            
            // Category 3: Nước ngọt đóng lon
            {"Coca-Cola lon 330ml", "Nước ngọt có ga Coca-Cola Classic. Vị cola đặc trưng, NGỌT ĐẬM, sảng khoái. Chứa cafein và đường. Lon nhôm 330ml, uống lạnh ngon nhất. Thùng 24 lon.", "3"},
            {"Pepsi không đường lon 330ml", "Nước ngọt Pepsi Zero Sugar. Có ga, vị cola đậm đà nhưng KHÔNG ĐƯỜNG, 0 CALO. Phù hợp người ăn kiêng hoặc không thích ngọt. Lon 330ml. Thùng 24 lon.", "3"},
            {"7Up lon 330ml", "Nước ngọt có ga 7Up vị chanh lime. Vị chua ngọt thanh mát, sảng khoái. Ngọt vừa phải. Lon 330ml, lý tưởng uống lạnh ngày nóng. Thùng 24 lon.", "3"},
            {"Trà xanh C2 lon 330ml", "Trà xanh đóng lon C2, hương lài. VỊ TRÀ THANH ÍT NGỌT, giàu chất chống oxy hóa từ trà xanh tự nhiên. Không ga, không cafein đậm. Lon 330ml. Thùng 24 lon.", "3"},
            {"Nước tăng lực Redbull lon 250ml", "Nước tăng lực Redbull vị ngọt thanh. Chứa taurine, cafein, vitamin B. Giúp tỉnh táo, tăng sức bền. NGỌT VỪA, có ga nhẹ. Lon 250ml. Thùng 24 lon.", "3"},

            // Category 4: Nước trái cây chai thủy tinh
            {"Nước ép táo Martinelli's", "Nước ép táo 100% nguyên chất Martinelli's nhập khẩu Mỹ. Vị táo tự nhiên, NGỌT TỰ NHIÊN KHÔNG THÊM ĐƯỜNG. Không chất bảo quản. Chai thủy tinh 296ml sang trọng. HSD: 18 tháng.", "4"},
            {"Nước ép cam Tropicana", "Nước cam ép nguyên chất Tropicana. Giàu vitamin C, vị chua ngọt tự nhiên, ÍT NGỌT HƠN NƯỚC NGỌT THÔNG THƯỜNG. Chai thủy tinh 300ml. HSD: 6 tháng (bảo quản lạnh sau khi mở).", "4"},
            {"Nước ép nho Welch's", "Nước ép nho đỏ Welch's nhập khẩu Mỹ. Vị nho đậm đà, ngọt tự nhiên từ nho Concord. Giàu polyphenol chống oxy hóa. Chai thủy tinh 296ml. KHÔNG THÊM ĐƯỜNG nhân tạo.", "4"},
            {"Sinh tố xoài chai thủy tinh", "Sinh tố xoài đóng chai thủy tinh, sản xuất tại Việt Nam. Vị xoài Cát Chu thơm ngon, NGỌT TỰ NHIÊN. Đặc sánh, giàu vitamin A và C. Chai 300ml. HSD: 6 tháng.", "4"},
            {"Nước ép lựu Grante", "Nước ép lựu Grante nhập khẩu Georgia. 100% lựu nguyên chất, VỊ CHUA NHẸ, ÍT NGỌT. Giàu chất chống oxy hóa, tốt cho tim mạch. Chai thủy tinh 750ml cao cấp. HSD: 24 tháng.", "4"},
        };
        
        for (String[] pData : productData) {
            String name = pData[0];
            String desc = pData[1];
            int catIdx = Integer.parseInt(pData[2]);
            Category category = categories.get(catIdx);
            
            Product product = Product.builder()
                    .category(category)
                    .name(name)
                    .description(desc)
                    .isAvailable(true)
                    .totalSold(random.nextInt(200))
                    .lowStockThreshold(10)
                    .variants(new ArrayList<>())
                    .build();

            int price = (random.nextInt(100) + 30) * 1000;
            ProductVariant variantM = ProductVariant.builder()
                    .product(product)
                    .size("Hộp/Lốc")
                    .price(new BigDecimal(price))
                    .stock(random.nextInt(50) + 5)
                    .isAvailable(true)
                    .build();
            
            product.getVariants().add(variantM);

            if (random.nextBoolean()) {
                ProductVariant variantL = ProductVariant.builder()
                        .product(product)
                        .size("Thùng")
                        .price(new BigDecimal(price * 10))
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
