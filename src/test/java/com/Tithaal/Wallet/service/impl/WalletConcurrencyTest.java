package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.*;
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

    @Autowired
    private UserRepository userRepository;

    private Long senderWalletId;
    private Long recipientWalletId;
    private Long senderUserId;

    @BeforeEach
    void setUp() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        User sender = User.builder()
                .username("sender_concurrent_" + uniqueSuffix)
                .email("sender_c_" + uniqueSuffix + "@example.com")
                .passwordHash("hash")
                .role(Role.ROLE_USER)
                .createdAt(Instant.now())
                .build();
        sender = userRepository.save(sender);
        senderUserId = sender.getId();

        User recipient = User.builder()
                .username("recipient_concurrent_" + uniqueSuffix)
                .email("recipient_c_" + uniqueSuffix + "@example.com")
                .passwordHash("hash")
                .role(Role.ROLE_USER)
                .createdAt(Instant.now())
                .build();
        recipient = userRepository.save(recipient);

        Wallet senderWallet = Wallet.builder()
                .user(sender)
                .balance(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .build();
        senderWallet = walletRepository.save(senderWallet);
        senderWalletId = senderWallet.getId();

        Wallet recipientWallet = Wallet.builder()
                .user(recipient)
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
        debitDto.setAmount(BigDecimal.valueOf(20)); // 10 threads * 20 = 200, only 5 should succeed (balance is 100)

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

        latch.countDown(); // Starts all threads at once
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        Wallet finalSenderWallet = walletRepository.findById(senderWalletId).get();
        Wallet finalRecipientWallet = walletRepository.findById(recipientWalletId).get();

        // One final check: total balance must remain 100
        BigDecimal totalBalance = finalSenderWallet.getBalance().add(finalRecipientWallet.getBalance());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(totalBalance), "Total balance in system must remain 100");
        
        // Final balance of sender should never be negative
        assertTrue(finalSenderWallet.getBalance().compareTo(BigDecimal.ZERO) >= 0);
    }

    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError();
    }
}
