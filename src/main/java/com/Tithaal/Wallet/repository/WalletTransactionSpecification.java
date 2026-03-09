package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.dto.UserTransactionFilterDto;
import com.Tithaal.Wallet.entity.WalletTransaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WalletTransactionSpecification {

    public static Specification<WalletTransaction> getUserTransactions(Long userId, UserTransactionFilterDto filterDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base Condition: Transaction belongs to a wallet owned by the user
            predicates.add(criteriaBuilder.equal(root.join("wallet").join("user").get("id"), userId));

            if (filterDto != null) {
                // Type filter
                if (filterDto.getType() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), filterDto.getType()));
                }

                // Date range filters
                if (filterDto.getStartDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getStartDate())));
                }
                if (filterDto.getEndDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getEndDate())));
                }

                // Amount range filters
                if (filterDto.getMinAmount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filterDto.getMinAmount()));
                }
                if (filterDto.getMaxAmount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filterDto.getMaxAmount()));
                }

                // Reference ID filter (exact match as it's an ID)
                if (StringUtils.hasText(filterDto.getReferenceId())) {
                    predicates.add(criteriaBuilder.equal(root.get("referenceId"), filterDto.getReferenceId()));
                }

                // Wallet ID filter
                if (filterDto.getWalletId() != null) {
                    predicates.add(criteriaBuilder.equal(root.join("wallet").get("id"), filterDto.getWalletId()));
                }

                // Description keyword filter (partial match, case-insensitive)
                if (StringUtils.hasText(filterDto.getDescriptionKeyword())) {
                    String keywordPattern = "%" + filterDto.getDescriptionKeyword().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keywordPattern));
                }
                // Recipient ID filter
                if (filterDto.getRecipientId() != null) {
                    predicates.add(criteriaBuilder.equal(root.join("recipientWallet").join("user").get("id"), filterDto.getRecipientId()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<WalletTransaction> getAdminTransactions(Long orgId, com.Tithaal.Wallet.dto.AdminTransactionFilterDto filterDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.join("wallet").join("user").join("organization").get("id"), orgId));

            if (filterDto != null) {
                if (filterDto.getType() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), filterDto.getType()));
                }
                if (filterDto.getStartDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getStartDate())));
                }
                if (filterDto.getEndDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getEndDate())));
                }
                if (filterDto.getMinAmount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filterDto.getMinAmount()));
                }
                if (filterDto.getMaxAmount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filterDto.getMaxAmount()));
                }
                if (StringUtils.hasText(filterDto.getReferenceId())) {
                    predicates.add(criteriaBuilder.equal(root.get("referenceId"), filterDto.getReferenceId()));
                }
                if (filterDto.getWalletId() != null) {
                    predicates.add(criteriaBuilder.equal(root.join("wallet").get("id"), filterDto.getWalletId()));
                }
                if (filterDto.getUserId() != null) {
                    predicates.add(criteriaBuilder.equal(root.join("wallet").join("user").get("id"), filterDto.getUserId()));
                }
                if (StringUtils.hasText(filterDto.getDescriptionKeyword())) {
                    String keywordPattern = "%" + filterDto.getDescriptionKeyword().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keywordPattern));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<WalletTransaction> getSuperAdminTransactions(com.Tithaal.Wallet.dto.SuperAdminTransactionFilterDto filterDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto != null) {
                if (filterDto.getType() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), filterDto.getType()));
                }
                if (filterDto.getStartDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getStartDate())));
                }
                if (filterDto.getEndDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getEndDate())));
                }
                if (filterDto.getMinAmount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filterDto.getMinAmount()));
                }
                if (filterDto.getMaxAmount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filterDto.getMaxAmount()));
                }
                if (StringUtils.hasText(filterDto.getReferenceId())) {
                    predicates.add(criteriaBuilder.equal(root.get("referenceId"), filterDto.getReferenceId()));
                }
                if (filterDto.getOrganizationId() != null) {
                    predicates.add(criteriaBuilder.equal(root.join("wallet").join("user").join("organization").get("id"), filterDto.getOrganizationId()));
                }
                if (filterDto.getWalletId() != null) {
                    predicates.add(criteriaBuilder.equal(root.join("wallet").get("id"), filterDto.getWalletId()));
                }
                if (filterDto.getUserId() != null) {
                    predicates.add(criteriaBuilder.equal(root.join("wallet").join("user").get("id"), filterDto.getUserId()));
                }
                if (StringUtils.hasText(filterDto.getDescriptionKeyword())) {
                    String keywordPattern = "%" + filterDto.getDescriptionKeyword().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keywordPattern));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
