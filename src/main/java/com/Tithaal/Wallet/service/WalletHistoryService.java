package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserTransactionFilterDto;
import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import java.util.List;

public interface WalletHistoryService {
    PagedResponse<WalletTransactionEntryDto> getUserHistory(Long userId, UserTransactionFilterDto filterDto, int page, int size, String sortBy, String sortDir);

    List<WalletTransactionEntryDto> getWalletHistory(Long walletId);
}
