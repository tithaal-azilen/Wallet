package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.Wallet;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);

    List<Wallet> findByUser(User user);

    @Query("SELECT w FROM Wallet w WHERE w.nextDeductionDate <= :date AND (w.lastDeductionAttempt IS NULL OR w.lastDeductionAttempt < :date)")
    List<Wallet> findWalletsDueForDeduction(@Param("date") java.time.LocalDate date, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findWithLockingById(@Param("id") Long id);
}
