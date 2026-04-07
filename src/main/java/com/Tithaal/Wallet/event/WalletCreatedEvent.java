package com.Tithaal.Wallet.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;
import java.util.UUID;

@Getter
public class WalletCreatedEvent extends ApplicationEvent {
    private final UUID userId;
    private final Long walletId;

    public WalletCreatedEvent(Object source, UUID userId, Long walletId) {
        super(source, Clock.systemDefaultZone());
        this.userId = userId;
        this.walletId = walletId;
    }
}
