package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.dto.AdminTransactionFilterDto;
import com.Tithaal.Wallet.dto.SuperAdminTransactionFilterDto;
import com.Tithaal.Wallet.dto.UserTransactionFilterDto;
import com.Tithaal.Wallet.entity.WalletTransaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WalletTransactionSpecification {

    /**
     * Filter transactions for a specific Auth Service userId (UUID).
     * Joins wallet → userId UUID column (no local users table join needed).
     */
    public static Specification<WalletTransaction> getUserTransactions(UUID userId, UserTransactionFilterDto filterDto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base condition: use denormalized userId column
            predicates.add(cb.equal(root.get("userId"), userId));


            if (filterDto != null) {
                if (filterDto.getType() != null) {
                    predicates.add(cb.equal(root.get("type"), filterDto.getType()));
                }
                if (filterDto.getStartDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getStartDate())));
                }
                if (filterDto.getEndDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getEndDate())));
                }
                if (filterDto.getMinAmount() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filterDto.getMinAmount()));
                }
                if (filterDto.getMaxAmount() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filterDto.getMaxAmount()));
                }
                if (StringUtils.hasText(filterDto.getReferenceId())) {
                    predicates.add(cb.equal(root.get("referenceId"), filterDto.getReferenceId()));
                }
                if (filterDto.getWalletId() != null) {
                    predicates.add(cb.equal(root.join("wallet").get("id"), filterDto.getWalletId()));
                }
                if (StringUtils.hasText(filterDto.getDescriptionKeyword())) {
                    predicates.add(cb.like(cb.lower(root.get("description")),
                            "%" + filterDto.getDescriptionKeyword().toLowerCase() + "%"));
                }
                if (filterDto.getRecipientId() != null) {
                    // recipientId is now a UUID
                    predicates.add(cb.equal(root.join("recipientWallet").get("userId"),
                            filterDto.getRecipientId()));
                }

            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Admin transactions for an org — now scoped by tenant UUID (tenantId column on wallet).
     */
    public static Specification<WalletTransaction> getAdminTransactions(Long orgId, AdminTransactionFilterDto filterDto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Scope to tenant — use denormalized tenantId column
            // orgId is still passed as Long in some contexts; we need to decide if we use orgId or UUID.
            // For now, if we don't have the UUID yet, we still join. 
            // BUT the goal is to use denormalized UUID. 
            // If orgId is Long, we might still need a join to organizations table or just use the UUID if passed.
            // Assuming tenantId in token is UUID, and we pass UUID to this method eventually.
            predicates.add(cb.equal(root.get("tenantId"), filterDto.getTenantId()));


            if (filterDto != null) {
                if (filterDto.getType() != null)
                    predicates.add(cb.equal(root.get("type"), filterDto.getType()));
                if (filterDto.getStartDate() != null)
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getStartDate())));
                if (filterDto.getEndDate() != null)
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getEndDate())));
                if (filterDto.getMinAmount() != null)
                    predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filterDto.getMinAmount()));
                if (filterDto.getMaxAmount() != null)
                    predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filterDto.getMaxAmount()));
                if (StringUtils.hasText(filterDto.getReferenceId()))
                    predicates.add(cb.equal(root.get("referenceId"), filterDto.getReferenceId()));
                if (filterDto.getWalletId() != null)
                    predicates.add(cb.equal(root.join("wallet").get("id"), filterDto.getWalletId()));
                if (filterDto.getUserId() != null)
                    predicates.add(cb.equal(root.get("userId"), filterDto.getUserId()));

                if (StringUtils.hasText(filterDto.getDescriptionKeyword()))
                    predicates.add(cb.like(cb.lower(root.get("description")),
                            "%" + filterDto.getDescriptionKeyword().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<WalletTransaction> getSuperAdminTransactions(SuperAdminTransactionFilterDto filterDto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto != null) {
                if (filterDto.getType() != null)
                    predicates.add(cb.equal(root.get("type"), filterDto.getType()));
                if (filterDto.getStartDate() != null)
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getStartDate())));
                if (filterDto.getEndDate() != null)
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), Timestamp.from(filterDto.getEndDate())));
                if (filterDto.getMinAmount() != null)
                    predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filterDto.getMinAmount()));
                if (filterDto.getMaxAmount() != null)
                    predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filterDto.getMaxAmount()));
                if (StringUtils.hasText(filterDto.getReferenceId()))
                    predicates.add(cb.equal(root.get("referenceId"), filterDto.getReferenceId()));
                if (filterDto.getOrganizationId() != null)
                    // scope by denormalized tenantId column (organizationId in DTO)
                    predicates.add(cb.equal(root.get("tenantId"),
                            filterDto.getOrganizationId()));

                if (filterDto.getWalletId() != null)
                    predicates.add(cb.equal(root.join("wallet").get("id"), filterDto.getWalletId()));
                if (filterDto.getUserId() != null)
                    predicates.add(cb.equal(root.get("userId"), filterDto.getUserId()));

                if (StringUtils.hasText(filterDto.getDescriptionKeyword()))
                    predicates.add(cb.like(cb.lower(root.get("description")),
                            "%" + filterDto.getDescriptionKeyword().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
