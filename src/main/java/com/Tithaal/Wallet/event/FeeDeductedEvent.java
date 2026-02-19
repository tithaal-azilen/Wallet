package com.Tithaal.Wallet.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Getter
public class FeeDeductedEvent extends ApplicationEvent {
    private final String email;
    private final Long walletId;
    private final BigDecimal amount;
    private final LocalDate date;

    public FeeDeductedEvent(Object source, String email, Long walletId, BigDecimal amount, LocalDate date) {
        super(source, Clock.systemDefaultZone());
        this.email = email;
        this.walletId = walletId;
        this.amount = amount;
        this.date = date;
    }
}
