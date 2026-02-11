package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;

public interface WalletService {
    String TopUpWallet(Long walletId, CreditRequestDto creditRequestDto);

    String Transfer(DebitRequestDto debitRequestDto);

    void validateWalletOwnership(Long walletId, Long userId);
}
