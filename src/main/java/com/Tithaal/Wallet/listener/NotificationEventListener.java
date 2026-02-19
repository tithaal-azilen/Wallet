package com.Tithaal.Wallet.listener;

import com.Tithaal.Wallet.event.FeeDeductedEvent;
import com.Tithaal.Wallet.event.WalletCreatedEvent;
import com.Tithaal.Wallet.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleWalletCreatedEvent(WalletCreatedEvent event) {
        log.info("Handling WalletCreatedEvent for walletId: {}", event.getWalletId());
        emailService.sendWalletCreationEmail(event.getEmail(), event.getWalletId());
    }

    @Async
    @EventListener
    public void handleFeeDeductedEvent(FeeDeductedEvent event) {
        log.info("Handling FeeDeductedEvent for walletId: {}", event.getWalletId());
        emailService.sendFeeDeductionEmail(event.getEmail(), event.getWalletId(), event.getAmount(), event.getDate());
    }
}
