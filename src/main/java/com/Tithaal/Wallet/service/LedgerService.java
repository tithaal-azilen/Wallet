package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.LedgerEntryDto;
import java.util.List;

public interface LedgerService {
    List<LedgerEntryDto> getUserLedger(Long userId);
}
