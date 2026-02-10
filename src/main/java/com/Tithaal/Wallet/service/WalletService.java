package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {
    Wallet creditWallet(Long walletId, BigDecimal amount);

    Wallet transferFunds(Long senderWalletId, Long recipientWalletId, BigDecimal amount);

    void validateWalletOwnership(Long walletId, Long userId);
}
