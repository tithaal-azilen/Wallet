package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    void deleteAllByWallet(Wallet wallet);

    java.util.List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    java.util.List<WalletTransaction> findAllByWalletUserIdOrderByCreatedAtDesc(Long userId);

    java.util.List<WalletTransaction> findAllByWalletIdOrderByCreatedAtDesc(Long walletId);
}
