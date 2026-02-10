package com.Tithaal.Wallet.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class WalletDto {
    private Long id;
    private BigDecimal balance;
    private Instant createdAt;
}
