package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;

public interface WalletService {
    String TopUpWallet(Long walletId, CreditRequestDto creditRequestDto, Long userId);

    String Transfer(DebitRequestDto debitRequestDto, Long userId);

    void validateWalletOwnership(Long walletId, Long userId);
}
