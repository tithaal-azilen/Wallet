package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    void deleteAllByWallet(com.Tithaal.Wallet.entity.Wallet wallet);
}
