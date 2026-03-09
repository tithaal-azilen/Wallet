package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long>, JpaSpecificationExecutor<WalletTransaction> {
        void deleteAllByWallet(Wallet wallet);

        java.util.List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);

        java.util.List<WalletTransaction> findAllByWalletUserIdOrderByCreatedAtDesc(Long userId);

        java.util.List<WalletTransaction> findAllByWalletIdOrderByCreatedAtDesc(Long walletId);

        @org.springframework.data.jpa.repository.Query("SELECT t FROM WalletTransaction t JOIN t.wallet w JOIN w.user u WHERE u.organization.id = :orgId")
        org.springframework.data.domain.Page<WalletTransaction> findByOrganizationId(
                        @org.springframework.data.repository.query.Param("orgId") Long orgId,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT t FROM WalletTransaction t JOIN t.wallet w JOIN w.user u WHERE u.organization.id = :orgId "
                        +
                        "AND (:type IS NULL OR t.type = :type) " +
                        "AND (cast(:startDate as timestamp) IS NULL OR t.createdAt >= :startDate) " +
                        "AND (cast(:endDate as timestamp) IS NULL OR t.createdAt <= :endDate) " +
                        "AND (:userId IS NULL OR u.id = :userId)")
        org.springframework.data.domain.Page<WalletTransaction> findByOrganizationIdWithFilters(
                        @org.springframework.data.repository.query.Param("orgId") Long orgId,
                        @org.springframework.data.repository.query.Param("type") com.Tithaal.Wallet.entity.TransactionType type,
                        @org.springframework.data.repository.query.Param("startDate") java.time.Instant startDate,
                        @org.springframework.data.repository.query.Param("endDate") java.time.Instant endDate,
                        @org.springframework.data.repository.query.Param("userId") Long userId,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT t FROM WalletTransaction t JOIN t.wallet w JOIN w.user u "
                        +
                        "LEFT JOIN u.organization o WHERE " +
                        "(:type IS NULL OR t.type = :type) AND " +
                        "(cast(:startDate as timestamp) IS NULL OR t.createdAt >= :startDate) AND " +
                        "(cast(:endDate as timestamp) IS NULL OR t.createdAt <= :endDate) AND " +
                        "(:userId IS NULL OR u.id = :userId) AND " +
                        "(:walletId IS NULL OR w.id = :walletId) AND " +
                        "(:organizationId IS NULL OR o.id = :organizationId)")
        org.springframework.data.domain.Page<WalletTransaction> findAllPlatformWithFilters(
                        @org.springframework.data.repository.query.Param("type") com.Tithaal.Wallet.entity.TransactionType type,
                        @org.springframework.data.repository.query.Param("startDate") java.time.Instant startDate,
                        @org.springframework.data.repository.query.Param("endDate") java.time.Instant endDate,
                        @org.springframework.data.repository.query.Param("userId") Long userId,
                        @org.springframework.data.repository.query.Param("walletId") Long walletId,
                        @org.springframework.data.repository.query.Param("organizationId") Long organizationId,
                        org.springframework.data.domain.Pageable pageable);
}
