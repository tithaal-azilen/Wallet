package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests concurrent-like transfer scenarios by performing sequential transfers
 * that validate balance consistency and locking behavior.
 * Note: True multi-threaded concurrency tests require a real database (PostgreSQL/MySQL)
 * since H2 does not properly support PESSIMISTIC_WRITE locks in concurrent threads.
 */
@SpringBootTest
public class TransferConcurrencyIntegrationTest {

    @Autowired private WalletService walletService;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired
    private com.Tithaal.Wallet.repository.RefreshTokenRepository refreshTokenRepository;

    private Long walletAId;
    private Long walletBId;
    private Long userAId;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        Organization org = organizationRepository.save(Organization.builder()
                .name("ConcOrg").orgCode("CONC01")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        User userA = userRepository.save(User.builder()
                .username("conca").email("conca@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(org).createdAt(Instant.now()).build());
        userAId = userA.getId();

        User userB = userRepository.save(User.builder()
                .username("concb").email("concb@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(org).createdAt(Instant.now()).build());

        Wallet walletA = walletRepository.save(Wallet.builder()
                .user(userA).balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now()).build());
        walletAId = walletA.getId();

        Wallet walletB = walletRepository.save(Wallet.builder()
                .user(userB).balance(new BigDecimal("0.00"))
                .createdAt(Instant.now()).build());
        walletBId = walletB.getId();
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void shouldConserveMoneyAcrossMultipleSequentialTransfers() {
        // Perform 5 sequential transfers of 150 each (total 750)
        for (int i = 0; i < 5; i++) {
            DebitRequestDto dto = new DebitRequestDto();
            dto.setSendingWalletId(walletAId);
            dto.setReceivingWalletId(walletBId);
            dto.setAmount(new BigDecimal("150.00"));
            walletService.Transfer(dto, userAId);
        }

        Wallet finalA = walletRepository.findById(walletAId).orElseThrow();
        Wallet finalB = walletRepository.findById(walletBId).orElseThrow();

        // A started with 1000, transferred 5*150=750
        assertEquals(0, new BigDecimal("250.00").compareTo(finalA.getBalance()),
                "Sender balance should be 250.00, got: " + finalA.getBalance());
        assertEquals(0, new BigDecimal("750.00").compareTo(finalB.getBalance()),
                "Receiver balance should be 750.00, got: " + finalB.getBalance());

        // Total money is conserved
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalA.getBalance().add(finalB.getBalance())),
                "Total money should be conserved");
    }

    @Test
    void shouldRejectTransferWhenBalanceIsInsufficient() {
        // Transfer 800 first
        DebitRequestDto dto1 = new DebitRequestDto();
        dto1.setSendingWalletId(walletAId);
        dto1.setReceivingWalletId(walletBId);
        dto1.setAmount(new BigDecimal("800.00"));
        walletService.Transfer(dto1, userAId);

        // Now try to transfer 300 (only 200 remaining)
        DebitRequestDto dto2 = new DebitRequestDto();
        dto2.setSendingWalletId(walletAId);
        dto2.setReceivingWalletId(walletBId);
        dto2.setAmount(new BigDecimal("300.00"));

        assertThrows(DomainException.class, () -> walletService.Transfer(dto2, userAId));

        // Verify balances are still correct after the failed transfer
        Wallet finalA = walletRepository.findById(walletAId).orElseThrow();
        Wallet finalB = walletRepository.findById(walletBId).orElseThrow();

        assertEquals(0, new BigDecimal("200.00").compareTo(finalA.getBalance()));
        assertEquals(0, new BigDecimal("800.00").compareTo(finalB.getBalance()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalA.getBalance().add(finalB.getBalance())));
    }
}
