package com.Tithaal.Wallet.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class FeeDeductedEvent extends ApplicationEvent {
    private final UUID userId;
    private final Long walletId;
    private final BigDecimal amount;
    private final LocalDate date;

    public FeeDeductedEvent(Object source, UUID userId, Long walletId, BigDecimal amount, LocalDate date) {
        super(source, Clock.systemDefaultZone());
        this.userId = userId;
        this.walletId = walletId;
        this.amount = amount;
        this.date = date;
    }
}
