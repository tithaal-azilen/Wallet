package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import java.util.List;

public interface WalletHistoryService {
    List<WalletTransactionEntryDto> getUserHistory(Long userId);

    List<WalletTransactionEntryDto> getWalletHistory(Long walletId);
}
