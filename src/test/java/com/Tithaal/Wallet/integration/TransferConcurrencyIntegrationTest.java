package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.WalletService;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.exception.DomainException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests sequential transfer scenarios that validate balance consistency.
 * Note: True multi-threaded concurrency requires a real DB (PostgreSQL)
 * since H2 does not support PESSIMISTIC_WRITE locks in concurrent threads.
 */
@SpringBootTest
@ActiveProfiles("test")
public class TransferConcurrencyIntegrationTest {

    @Autowired private WalletService walletService;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;

    private Long walletAId;
    private Long walletBId;
    private UUID userAId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();

        userAId = UUID.randomUUID();
        UUID userBId = UUID.randomUUID();

        Wallet walletA = walletRepository.save(Wallet.builder()
                .userId(userAId)
                .balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now())
                .build());
        walletAId = walletA.getId();

        Wallet walletB = walletRepository.save(Wallet.builder()
                .userId(userBId)
                .balance(new BigDecimal("0.00"))
                .createdAt(Instant.now())
                .build());
        walletBId = walletB.getId();
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    void shouldConserveMoneyAcrossMultipleSequentialTransfers() {
        for (int i = 0; i < 5; i++) {
            DebitRequestDto dto = new DebitRequestDto();
            dto.setSendingWalletId(walletAId);
            dto.setReceivingWalletId(walletBId);
            dto.setAmount(new BigDecimal("150.00"));
            walletService.Transfer(dto, userAId);
        }

        Wallet finalA = walletRepository.findById(walletAId).orElseThrow();
        Wallet finalB = walletRepository.findById(walletBId).orElseThrow();

        assertEquals(0, new BigDecimal("250.00").compareTo(finalA.getBalance()),
                "Sender balance should be 250.00, got: " + finalA.getBalance());
        assertEquals(0, new BigDecimal("750.00").compareTo(finalB.getBalance()),
                "Receiver balance should be 750.00, got: " + finalB.getBalance());
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalA.getBalance().add(finalB.getBalance())),
                "Total money should be conserved");
    }

    @Test
    void shouldRejectTransferWhenBalanceIsInsufficient() {
        DebitRequestDto dto1 = new DebitRequestDto();
        dto1.setSendingWalletId(walletAId);
        dto1.setReceivingWalletId(walletBId);
        dto1.setAmount(new BigDecimal("800.00"));
        walletService.Transfer(dto1, userAId);

        DebitRequestDto dto2 = new DebitRequestDto();
        dto2.setSendingWalletId(walletAId);
        dto2.setReceivingWalletId(walletBId);
        dto2.setAmount(new BigDecimal("300.00"));

        assertThrows(DomainException.class, () -> walletService.Transfer(dto2, userAId));

        Wallet finalA = walletRepository.findById(walletAId).orElseThrow();
        Wallet finalB = walletRepository.findById(walletBId).orElseThrow();

        assertEquals(0, new BigDecimal("200.00").compareTo(finalA.getBalance()));
        assertEquals(0, new BigDecimal("800.00").compareTo(finalB.getBalance()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalA.getBalance().add(finalB.getBalance())));
    }
}
