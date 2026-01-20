package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.constants.TransactionStatus;
import com.example.bankcards.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * конфиг с методами запросов через criteria api
 */
public final class TransactionSpecification {
    private TransactionSpecification() {}
    public static Specification<Transaction> withStatus(TransactionStatus status) {
        return (root,query,cb) -> status == null ? null : cb
                .equal(root.get("transactionStatus"),status);
    }

    public static Specification<Transaction> withMinAmount(BigDecimal minAmount) {
        return (root, query, criteriaBuilder) -> minAmount == null
        ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Transaction> withMaxAmount(BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> maxAmount == null
                ? null : criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }

    public static Specification<Transaction> withUserId(Long userId) {
        if (userId == null) return null;
        return(root,query,cb) -> {
            Join<Transaction, Card> cardJoin = root.join("fromCard");
            Join<Card, User> userJoin = cardJoin.join("user");
            return cb.equal(userJoin.get("id"),userId);
        };
    }

    public static Specification<Transaction> withTimeStampBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root,query,cb) -> {
            if (startDate == null || endDate == null)
                return null;
            return cb.between(root.get("timeStamp"),startDate,endDate);
        };
    }
}
