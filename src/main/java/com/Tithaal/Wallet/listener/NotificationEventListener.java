package com.Tithaal.Wallet.listener;

import com.Tithaal.Wallet.client.AuthServiceClient;
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
    private final AuthServiceClient authServiceClient;

    @Async
    @EventListener
    public void handleWalletCreatedEvent(WalletCreatedEvent event) {
        log.info("Handling WalletCreatedEvent for userId: {} and walletId: {}", event.getUserId(), event.getWalletId());
        String email = authServiceClient.getUserEmail(event.getUserId());
        if (email != null) {
            emailService.sendWalletCreationEmail(email, event.getWalletId());
        } else {
            log.warn("Skipping email for WalletCreatedEvent: email not found for userId {}", event.getUserId());
        }
    }

    @Async
    @EventListener
    public void handleFeeDeductedEvent(FeeDeductedEvent event) {
        log.info("Handling FeeDeductedEvent for userId: {} and walletId: {}", event.getUserId(), event.getWalletId());
        String email = authServiceClient.getUserEmail(event.getUserId());
        if (email != null) {
            emailService.sendFeeDeductionEmail(email, event.getWalletId(), event.getAmount(), event.getDate());
        } else {
            log.warn("Skipping email for FeeDeductedEvent: email not found for userId {}", event.getUserId());
        }
    }
}
