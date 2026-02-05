package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByUser(com.Tithaal.Wallet.entity.User user);
}
