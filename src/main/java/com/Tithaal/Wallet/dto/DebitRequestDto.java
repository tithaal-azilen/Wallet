package com.Tithaal.Wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DebitRequestDto {

    @NotNull(message = "Sending Wallet ID is required")
    private Long sendingWalletId;

    @NotNull(message = "Receiving Wallet ID is required")
    private Long receivingWalletId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
