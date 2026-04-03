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

        java.util.List<WalletTransaction> findAllByUserIdOrderByCreatedAtDesc(java.util.UUID userId);

        java.util.List<WalletTransaction> findAllByWalletIdOrderByCreatedAtDesc(Long walletId);

        @org.springframework.data.jpa.repository.Query("SELECT t FROM WalletTransaction t WHERE t.tenantId = :tenantId")
        org.springframework.data.domain.Page<WalletTransaction> findByTenantId(
                        @org.springframework.data.repository.query.Param("tenantId") java.util.UUID tenantId,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT t FROM WalletTransaction t WHERE t.tenantId = :tenantId "
                        +
                        "AND (:type IS NULL OR t.type = :type) " +
                        "AND (cast(:startDate as timestamp) IS NULL OR t.createdAt >= :startDate) " +
                        "AND (cast(:endDate as timestamp) IS NULL OR t.createdAt <= :endDate) " +
                        "AND (:userId IS NULL OR t.userId = :userId)")
        org.springframework.data.domain.Page<WalletTransaction> findByTenantIdWithFilters(
                        @org.springframework.data.repository.query.Param("tenantId") java.util.UUID tenantId,
                        @org.springframework.data.repository.query.Param("type") com.Tithaal.Wallet.entity.TransactionType type,
                        @org.springframework.data.repository.query.Param("startDate") java.time.Instant startDate,
                        @org.springframework.data.repository.query.Param("endDate") java.time.Instant endDate,
                        @org.springframework.data.repository.query.Param("userId") java.util.UUID userId,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT t FROM WalletTransaction t WHERE "
                        +
                        "(:type IS NULL OR t.type = :type) AND " +
                        "(cast(:startDate as timestamp) IS NULL OR t.createdAt >= :startDate) AND " +
                        "(cast(:endDate as timestamp) IS NULL OR t.createdAt <= :endDate) AND " +
                        "(:userId IS NULL OR t.userId = :userId) AND " +
                        "(:walletId IS NULL OR t.wallet.id = :walletId) AND " +
                        "(:tenantId IS NULL OR t.tenantId = :tenantId)")
        org.springframework.data.domain.Page<WalletTransaction> findAllPlatformWithFilters(
                        @org.springframework.data.repository.query.Param("type") com.Tithaal.Wallet.entity.TransactionType type,
                        @org.springframework.data.repository.query.Param("startDate") java.time.Instant startDate,
                        @org.springframework.data.repository.query.Param("endDate") java.time.Instant endDate,
                        @org.springframework.data.repository.query.Param("userId") java.util.UUID userId,
                        @org.springframework.data.repository.query.Param("walletId") Long walletId,
                        @org.springframework.data.repository.query.Param("tenantId") java.util.UUID tenantId,
                        org.springframework.data.domain.Pageable pageable);

}
