package com.Tithaal.Wallet.dto;

import com.Tithaal.Wallet.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransactionFilterDto {
    private TransactionType type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant endDate;

    private UUID userId;
    private UUID tenantId;
    private Long walletId;


    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String referenceId;
    private String descriptionKeyword;
}
