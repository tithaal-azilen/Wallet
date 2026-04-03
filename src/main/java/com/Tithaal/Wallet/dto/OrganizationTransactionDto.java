package com.Tithaal.Wallet.dto;

import com.Tithaal.Wallet.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationTransactionDto {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private BigDecimal balanceAfter;
    private Instant createdAt;
    private Long walletId;
    /** Auth Service UUID of the wallet owner */
    private UUID userId;
}
