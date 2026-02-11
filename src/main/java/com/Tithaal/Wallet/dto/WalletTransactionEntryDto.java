package com.Tithaal.Wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionEntryDto {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String description;
    private BigDecimal balanceAfter;
    private Instant createdAt;
    private Long walletId;
}
