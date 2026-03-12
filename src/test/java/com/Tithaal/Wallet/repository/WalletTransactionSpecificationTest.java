package com.Tithaal.Wallet.repository;

import com.Tithaal.Wallet.dto.UserTransactionFilterDto;
import com.Tithaal.Wallet.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class WalletTransactionSpecificationTest {

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("spec_user")
                .email("spec@example.com")
                .passwordHash("hash")
                .role(Role.ROLE_USER)
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);

        wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.valueOf(100))
                .createdAt(Instant.now())
                .build();
        walletRepository.save(wallet);

        WalletTransaction t1 = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.CREDIT)
                .amount(BigDecimal.TEN)
                .description("Coffee purchase")
                .createdAt(Instant.now())
                .balanceAfter(BigDecimal.valueOf(110))
                .build();

        WalletTransaction t2 = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.DEBIT)
                .amount(BigDecimal.ONE)
                .description("Fee")
                .createdAt(Instant.now())
                .balanceAfter(BigDecimal.valueOf(109))
                .build();

        walletTransactionRepository.save(t1);
        walletTransactionRepository.save(t2);
    }

    @Test
    void getUserTransactions_ShouldFilterByDescriptionKeyword() {
        UserTransactionFilterDto filter = new UserTransactionFilterDto();
        filter.setDescriptionKeyword("coffee");

        Specification<WalletTransaction> spec = WalletTransactionSpecification.getUserTransactions(user.getId(), filter);
        List<WalletTransaction> results = walletTransactionRepository.findAll(spec);

        assertEquals(1, results.size());
        assertTrue(results.get(0).getDescription().toLowerCase().contains("coffee"));
    }

    @Test
    void getUserTransactions_ShouldFilterByType() {
        UserTransactionFilterDto filter = new UserTransactionFilterDto();
        filter.setType(TransactionType.DEBIT);

        Specification<WalletTransaction> spec = WalletTransactionSpecification.getUserTransactions(user.getId(), filter);
        List<WalletTransaction> results = walletTransactionRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals(TransactionType.DEBIT, results.get(0).getType());
    }

    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError();
    }
}
