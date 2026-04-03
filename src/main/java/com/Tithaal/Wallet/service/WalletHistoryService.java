package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserTransactionFilterDto;
import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;

import java.util.List;
import java.util.UUID;

public interface WalletHistoryService {
    PagedResponse<WalletTransactionEntryDto> getUserHistory(UUID userId, UserTransactionFilterDto filterDto,
            int page, int size, String sortBy, String sortDir);

    List<WalletTransactionEntryDto> getWalletHistory(Long walletId);

    List<WalletTransactionEntryDto> getAllUserHistory(UUID userId, UserTransactionFilterDto filterDto,
            String sortBy, String sortDir);
}
