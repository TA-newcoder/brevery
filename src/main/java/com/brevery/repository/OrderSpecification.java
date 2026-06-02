package com.brevery.repository;

import com.brevery.entity.Order;
import com.brevery.enums.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> filterOrders(OrderStatus status, LocalDateTime fromDate, LocalDateTime toDate, Long userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("userId"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
