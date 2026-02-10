package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.Wallet;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);

    java.util.List<Wallet> findByUser(User user);
}
