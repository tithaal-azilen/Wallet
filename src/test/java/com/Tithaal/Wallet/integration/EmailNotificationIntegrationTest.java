package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.event.FeeDeductedEvent;
import com.Tithaal.Wallet.event.WalletCreatedEvent;
import com.Tithaal.Wallet.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class EmailNotificationIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private EmailService emailService;

    @Test
    void testWalletCreatedEvent_ShouldTriggerEmail() {
        String email = "test@example.com";
        Long walletId = 1L;

        eventPublisher.publishEvent(new WalletCreatedEvent(this, email, walletId));

        verify(emailService, timeout(1000)).sendWalletCreationEmail(eq(email), eq(walletId));
    }

    @Test
    void testFeeDeductedEvent_ShouldTriggerEmail() {
        String email = "test@example.com";
        Long walletId = 1L;
        BigDecimal amount = BigDecimal.TEN;
        LocalDate now = LocalDate.now();

        eventPublisher.publishEvent(new FeeDeductedEvent(this, email, walletId, amount, now));

        verify(emailService, timeout(1000)).sendFeeDeductionEmail(eq(email), eq(walletId), eq(amount), eq(now));
    }
}
