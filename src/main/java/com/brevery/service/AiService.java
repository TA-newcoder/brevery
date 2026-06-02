package com.brevery.service;

import com.brevery.entity.Product;
import com.brevery.entity.ProductVariant;
import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.brevery.repository.ProductRepository;
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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    private static final String RATE_LIMIT_PREFIX = "rate:ai:chat:";

    /**
     * Trò chuyện và tư vấn sản phẩm sử dụng Gemini API, giới hạn 10 tin nhắn/phút
     */
    public String chat(String message, String clientIp) {
        log.info("AI Chatbot received message: {} from IP: {}", message, clientIp);

        if (!StringUtils.hasText(message)) {
            return "Chào bạn! Mình có thể giúp gì cho bạn hôm nay? Bạn muốn tìm bánh ngọt hay đồ uống ngon lành nào?";
        }

        // 1. Rate limiting bằng Redis (10 tin nhắn/phút)
        try {
            String rateLimitKey = RATE_LIMIT_PREFIX + clientIp;
            Long count = redisTemplate.opsForValue().increment(rateLimitKey);
            if (count != null && count == 1) {
                redisTemplate.expire(rateLimitKey, 1, TimeUnit.MINUTES);
            }
            if (count != null && count > 10) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Bạn đã chat quá giới hạn cho phép (10 tin nhắn/phút). Vui lòng thử lại sau.");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Redis rate limiting skipped or error", e);
        }

        // 2. Lấy dữ liệu sản phẩm để làm ngữ cảnh
        List<Product> products = productRepository.findAll();

        // 3. Nếu không cấu hình API Key, sử dụng Local Fallback Engine
        if (!StringUtils.hasText(aiApiKey)) {
            log.warn("Gemini API key is not configured. Falling back to Local Smart Engine.");
            return generateLocalResponse(message.toLowerCase(), products);
        }

        // 4. Gọi Gemini API
        try {
            // Lọc sản phẩm phù hợp dựa vào từ khóa để giảm dung lượng prompt
            List<Product> contextualProducts = filterProductsByKeywords(message.toLowerCase(), products);
            String productContext = buildProductContextText(contextualProducts);

            String systemPrompt = "Bạn là Trợ lý ảo AI cực kỳ dễ thương, thân thiện của cửa hàng Brevery (Bakery & Beverage).\n" +
                    "Nhiệm vụ chính của bạn là tư vấn cho khách hàng những món bánh và nước ngon tuyệt của tiệm.\n" +
                    "Dưới đây là thực đơn các món đang bán của Brevery:\n" +
                    productContext + "\n\n" +
                    "Quy tắc trả lời:\n" +
                    "1. Chỉ tư vấn về các sản phẩm có trong danh sách trên của tiệm.\n" +
                    "2. Trả lời bằng tiếng Việt, cực kỳ ngọt ngào, dùng các icon dễ thương.\n" +
                    "3. Câu trả lời phải ngắn gọn, súc tích (dưới 150 từ), định dạng Markdown rõ ràng.\n" +
                    "4. Không bao giờ chế tạo thông tin sản phẩm không có thực tại tiệm.\n" +
                    "5. Từ chối lịch sự nếu khách hàng hỏi các câu hỏi không liên quan đến tiệm bánh, nước hoặc đồ ăn.";

            // Thiết lập HttpClient và Timeout (10 giây)
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(10000);
            requestFactory.setReadTimeout(10000);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + aiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Xây dựng JSON payload
            ObjectNode payloadNode = objectMapper.createObjectNode();
            ArrayNode contentsArray = payloadNode.putArray("contents");
            ObjectNode contentNode = contentsArray.addObject();
            
            // System prompt + user message kết hợp
            ArrayNode partsArray = contentNode.putArray("parts");
            partsArray.addObject().put("text", systemPrompt + "\n\nKhách hàng hỏi: " + message);

            // Cấu hình output tokens
            ObjectNode configNode = payloadNode.putObject("generationConfig");
            configNode.put("maxOutputTokens", 300);
            configNode.put("temperature", 0.7);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payloadNode), headers);

            log.info("Calling Gemini API...");
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
                JsonNode candidateTextNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                if (!candidateTextNode.isMissingNode()) {
                    return candidateTextNode.asText().trim();
                }
            }

            log.warn("Gemini response was empty or malformed. Using fallback.");
            return generateLocalResponse(message.toLowerCase(), products);

        } catch (Exception e) {
            log.error("Failed to connect or retrieve from Gemini API", e);
            return generateLocalResponse(message.toLowerCase(), products);
        }
    }

    private List<Product> filterProductsByKeywords(String message, List<Product> products) {
        // Nếu câu hỏi chung chung, trả về top 10 sản phẩm bán chạy nhất
        if (message.contains("bán chạy") || message.contains("hot") || message.contains("ngon nhất") || message.contains("menu") || message.contains("thực đơn")) {
            return products.stream()
                    .filter(Product::getIsAvailable)
                    .sorted((p1, p2) -> Integer.compare(p2.getTotalSold() != null ? p2.getTotalSold() : 0, p1.getTotalSold() != null ? p1.getTotalSold() : 0))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        // Lọc theo keywords cụ thể
        return products.stream()
                .filter(Product::getIsAvailable)
                .filter(p -> p.getName().toLowerCase().contains(message) ||
                        p.getDescription().toLowerCase().contains(message) ||
                        p.getCategory().getName().toLowerCase().contains(message) ||
                        containsAnyKeyword(message, p.getName().toLowerCase(), p.getDescription().toLowerCase()))
                .limit(6)
                .collect(Collectors.toList());
    }

    private boolean containsAnyKeyword(String message, String name, String desc) {
        String[] keywords = {"bánh", "nước", "trà", "cà phê", "sữa", "mì", "ngọt", "mặn", "dâu", "caramel", "sô cô la", "chocolate", "matcha", "kem"};
        for (String kw : keywords) {
            if (message.contains(kw) && (name.contains(kw) || desc.contains(kw))) {
                return true;
            }
        }
        return false;
    }

    private String buildProductContextText(List<Product> products) {
        if (products.isEmpty()) {
            return "Hiện tại cửa hàng tạm hết các sản phẩm.";
        }

        StringBuilder sb = new StringBuilder();
        for (Product p : products) {
            sb.append("- ").append(p.getName()).append(" (Danh mục: ").append(p.getCategory().getName()).append("): ");
            sb.append(p.getDescription()).append(". ");
            if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                String variantInfo = p.getVariants().stream()
                        .map(v -> "Size " + v.getSize() + " giá " + String.format("%,.0fđ", v.getPrice()))
                        .collect(Collectors.joining(", "));
                sb.append("Giá bán: ").append(variantInfo);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String generateLocalResponse(String question, List<Product> products) {
        StringBuilder reply = new StringBuilder();
        List<Product> matchingProducts = products.stream()
                .filter(Product::getIsAvailable)
                .collect(Collectors.toList());

        if (question.contains("bánh") || question.contains("cake") || question.contains("ngọt")) {
            reply.append("🧁 **Gợi ý các món BÁNH NGỌT & BÁNH MÌ nướng giòn tại Brevery:**\n\n");
            List<Product> cakes = matchingProducts.stream()
                    .filter(p -> p.getCategory().getName().toLowerCase().contains("bánh") || p.getDescription().toLowerCase().contains("bánh"))
                    .limit(4)
                    .collect(Collectors.toList());

            if (cakes.isEmpty()) {
                reply.append("Hiện tại các món bánh ngọt đang được nướng mẻ mới trong lò nướng. Bạn có muốn chọn các loại nước ép giải nhiệt tốt không ạ?\n");
            } else {
                appendLocalProductList(reply, cakes);
            }
        } else if (question.contains("nước") || question.contains("uống") || question.contains("trà") || question.contains("cà phê") || question.contains("coffee") || question.contains("sinh tố")) {
            reply.append("🍹 **Thực đơn ĐỒ UỐNG mát lạnh của tiệm Brevery:**\n\n");
            List<Product> drinks = matchingProducts.stream()
                    .filter(p -> p.getCategory().getName().toLowerCase().contains("nước") || p.getCategory().getName().toLowerCase().contains("uống") || p.getCategory().getName().toLowerCase().contains("trà") || p.getCategory().getName().toLowerCase().contains("cà phê") || p.getDescription().toLowerCase().contains("trà") || p.getDescription().toLowerCase().contains("cà phê"))
                    .limit(4)
                    .collect(Collectors.toList());

            if (drinks.isEmpty()) {
                appendLocalProductList(reply, matchingProducts.stream().limit(3).collect(Collectors.toList()));
            } else {
                appendLocalProductList(reply, drinks);
            }
        } else if (question.contains("bán chạy") || question.contains("hot") || question.contains("ngon nhất") || question.contains("gợi ý")) {
            reply.append("🔥 **TOP các món bánh & nước bán chạy nhất (BEST SELLER) tại tiệm:**\n\n");
            List<Product> bestSellers = matchingProducts.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getTotalSold() == null ? 0 : p2.getTotalSold(), p1.getTotalSold() == null ? 0 : p1.getTotalSold()))
                    .limit(4)
                    .collect(Collectors.toList());

            appendLocalProductList(reply, bestSellers);
        } else {
            reply.append("Chào bạn thân mến! 🌸 Chào mừng bạn đến với **Brevery - Bakery & Beverage**.\n\n");
            reply.append("Mình là trợ lý ảo AI luôn sẵn sàng tư vấn cho bạn những món bánh ngon lành và nước uống tuyệt vời của tiệm.\n\n");
            reply.append("💡 **Bạn có thể hỏi mình các câu hỏi như:**\n");
            reply.append("- *\"Cửa hàng có món bánh ngọt nào ngon bán chạy nhất?\"*\n");
            reply.append("- *\"Cho mình thực đơn đồ uống giải khát mát lạnh đi\"*\n");
            reply.append("- *\"Bánh mì sừng bò croissant giá bao nhiêu vậy?\"*\n\n");
            reply.append("Chúc bạn chọn được món ưng ý và có một ngày thật ngọt ngào! Cupcake! 🧁");
        }

        return reply.toString();
    }

    private void appendLocalProductList(StringBuilder reply, List<Product> products) {
        for (Product p : products) {
            reply.append("🔸 **").append(p.getName()).append("** (").append(p.getCategory().getName()).append(")\n");
            reply.append("   *Mô tả:* ").append(p.getDescription()).append("\n");
            if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                reply.append("   *Giá bán:* ");
                String variantStr = p.getVariants().stream()
                        .map(v -> "Size " + v.getSize() + ": " + String.format("%,.0fđ", v.getPrice()))
                        .collect(Collectors.joining(" | "));
                reply.append(variantStr).append("\n");
            }
            reply.append("\n");
        }
        reply.append("👉 *Hãy bảo mình thêm món bạn chọn vào giỏ hàng ngay nhé!*");
    }
}
