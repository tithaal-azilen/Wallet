package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.client.AuthServiceClient;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class EmailNotificationIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @Test
    void testWalletCreatedEvent_ShouldTriggerEmail() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Long walletId = 1L;

        when(authServiceClient.getUserEmail(userId)).thenReturn(email);

        eventPublisher.publishEvent(new WalletCreatedEvent(this, userId, walletId));

        verify(emailService, timeout(1000)).sendWalletCreationEmail(eq(email), eq(walletId));
    }

    @Test
    void testFeeDeductedEvent_ShouldTriggerEmail() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Long walletId = 1L;
        BigDecimal amount = BigDecimal.TEN;
        LocalDate now = LocalDate.now();

        when(authServiceClient.getUserEmail(userId)).thenReturn(email);

        eventPublisher.publishEvent(new FeeDeductedEvent(this, userId, walletId, amount, now));

        verify(emailService, timeout(1000)).sendFeeDeductionEmail(eq(email), eq(walletId), eq(amount), eq(now));
    }
}
