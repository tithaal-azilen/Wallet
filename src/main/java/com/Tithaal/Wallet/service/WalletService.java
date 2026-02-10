package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.Wallet;

public interface WalletService {
    Wallet creditWallet(Long walletId, CreditRequestDto creditRequestDto);

    Wallet transferFunds(DebitRequestDto debitRequestDto);

    void validateWalletOwnership(Long walletId, Long userId);
}
