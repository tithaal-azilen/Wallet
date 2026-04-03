package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /** Find all wallets owned by the given Auth Service userId UUID. */
    List<Wallet> findByUserId(UUID userId);

    /** Find wallet by userId UUID (first one — for single-wallet-per-user case). */
    Optional<Wallet> findFirstByUserId(UUID userId);

    /** Find all wallets for a specific tenant (for admin reporting). */
    List<Wallet> findByTenantId(UUID tenantId);

    @Query("SELECT w FROM Wallet w WHERE w.nextDeductionDate <= :date AND (w.lastDeductionAttempt IS NULL OR w.lastDeductionAttempt < :date)")
    List<Wallet> findWalletsDueForDeduction(@Param("date") java.time.LocalDate date, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findWithLockingById(@Param("id") Long id);
}
