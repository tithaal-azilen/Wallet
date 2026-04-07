package com.Tithaal.Wallet.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class OrderTransactionRequestDto {
    @NotNull(message = "Sender ID is required")
    private UUID senderId;

    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
}
