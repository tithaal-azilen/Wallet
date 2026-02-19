package com.Tithaal.Wallet.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

@Getter
public class WalletCreatedEvent extends ApplicationEvent {
    private final String email;
    private final Long walletId;

    public WalletCreatedEvent(Object source, String email, Long walletId) {
        super(source, Clock.systemDefaultZone());
        this.email = email;
        this.walletId = walletId;
    }
}
