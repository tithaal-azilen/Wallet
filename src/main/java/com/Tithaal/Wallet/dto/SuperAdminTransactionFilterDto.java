package com.Tithaal.Wallet.dto;

import com.Tithaal.Wallet.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminTransactionFilterDto {
    private TransactionType type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant endDate;

    private Long userId;
    private Long walletId;
    private Long organizationId;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String referenceId;
    private String descriptionKeyword;
}
