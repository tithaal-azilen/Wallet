package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PlatformTransactionTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private WalletTransactionRepository transactionRepository;

        @Autowired
        private WalletRepository walletRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private OrganizationRepository organizationRepository;

        @BeforeEach
        void setUp() {
                transactionRepository.deleteAll();
                walletRepository.deleteAll();
                userRepository.deleteAll();
                organizationRepository.deleteAll();

                Organization org = organizationRepository.save(Organization.builder()
                                .name("FinTech")
                                .orgCode("FIN001")
                                .status(OrganizationStatus.ACTIVE)
                                .createdAt(Instant.now())
                                .build());

                User user = userRepository.save(User.builder()
                                .username("user1")
                                .email("user1@example.com")
                                .passwordHash("hash")
                                .role(Role.ROLE_USER)
                                .status(UserStatus.ACTIVE)
                                .organization(org)
                                .createdAt(Instant.now())
                                .build());

                Wallet wallet = walletRepository.save(Wallet.builder()
                                .user(user)
                                .balance(new BigDecimal("1000.00"))
                                .createdAt(Instant.now())
                                .build());

                transactionRepository.save(WalletTransaction.builder()
                                .wallet(wallet)
                                .amount(new BigDecimal("100.00"))
                                .balanceAfter(new BigDecimal("1100.00"))
                                .type(TransactionType.CREDIT)
                                .description("Bonus")
                                .createdAt(Instant.now())
                                .build());

                transactionRepository.save(WalletTransaction.builder()
                                .wallet(wallet)
                                .amount(new BigDecimal("50.00"))
                                .balanceAfter(new BigDecimal("1050.00"))
                                .type(TransactionType.DEBIT)
                                .description("Purchase")
                                .createdAt(Instant.now().minusSeconds(1000))
                                .build());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldReturnPaginatedTransactionsAcrossAllOrganizations() throws Exception {
                mockMvc.perform(get("/api/platform/transactions"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldFilterTransactionsByAmountRange() throws Exception {
                mockMvc.perform(get("/api/platform/transactions")
                                .param("minAmount", "75.00")
                                .param("maxAmount", "125.00"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalElements").value(1))
                                .andExpect(jsonPath("$.content[0].amount").value(100.0));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldGeneratePlatformWidePdf() throws Exception {
                mockMvc.perform(get("/api/platform/transactions/download"))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Content-Type", "application/pdf"))
                                .andExpect(header().exists("Content-Disposition"));
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldDenyUserAccessTransactions() throws Exception {
                mockMvc.perform(get("/api/platform/transactions"))
                                .andExpect(status().isForbidden());
        }
}
