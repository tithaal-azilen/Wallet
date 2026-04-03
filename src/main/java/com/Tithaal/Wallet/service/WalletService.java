package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;

import java.util.UUID;

public interface WalletService {

    /** Top up a specific wallet. userId from JWT is used for ownership validation. */
    String topUpWallet(Long walletId, CreditRequestDto creditRequestDto, UUID userId);

    /**
     * Transfer funds. userId from JWT validates sender ownership.
     * senderStatus from JWT is used for status enforcement (no DB join).
     */
    String transfer(DebitRequestDto debitRequestDto, UUID userId, String senderStatus);

    /** Validate that a wallet belongs to the given userId UUID. */
    void validateWalletOwnership(Long walletId, UUID userId);

    /** Create a new wallet for the given userId and tenantId (both from JWT). */
    String addWallet(UUID userId, UUID tenantId);
}
