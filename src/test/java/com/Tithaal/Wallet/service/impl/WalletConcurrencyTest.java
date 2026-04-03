package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class WalletConcurrencyTest {

    @Autowired
    private WalletServiceImpl walletService;

    @Autowired
    private WalletRepository walletRepository;

    private Long senderWalletId;
    private Long recipientWalletId;
    private UUID senderUserId;

    @BeforeEach
    void setUp() {
        senderUserId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();

        Wallet senderWallet = Wallet.builder()
                .userId(senderUserId)
                .balance(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .build();
        senderWallet = walletRepository.save(senderWallet);
        senderWalletId = senderWallet.getId();

        Wallet recipientWallet = Wallet.builder()
                .userId(recipientUserId)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();
        recipientWallet = walletRepository.save(recipientWallet);
        recipientWalletId = recipientWallet.getId();
    }

    @Test
    void transfer_ShouldPreventOverspendingUnderHighConcurrency() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setSendingWalletId(senderWalletId);
        debitDto.setReceivingWalletId(recipientWalletId);
        debitDto.setAmount(BigDecimal.valueOf(20)); // 10 threads * 20 = 200, only 5 should succeed

        Callable<String> task = () -> {
            latch.await();
            try {
                return walletService.Transfer(debitDto, senderUserId);
            } catch (Exception e) {
                return e.getMessage();
            }
        };

        List<Future<String>> futuresList = new java.util.ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futuresList.add(executorService.submit(task));
        }

        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        Wallet finalSenderWallet = walletRepository.findById(senderWalletId).get();
        Wallet finalRecipientWallet = walletRepository.findById(recipientWalletId).get();

        BigDecimal totalBalance = finalSenderWallet.getBalance().add(finalRecipientWallet.getBalance());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(totalBalance), "Total balance in system must remain 100");
        assertTrue(finalSenderWallet.getBalance().compareTo(BigDecimal.ZERO) >= 0);
    }

    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError();
    }
}
