package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReportingIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;

    private UUID tenant1;
    private UUID tenant2;
    private UUID admin1Id;
    private UUID superAdminId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();

        tenant1 = UUID.randomUUID();
        tenant2 = UUID.randomUUID();
        admin1Id = UUID.randomUUID();
        superAdminId = UUID.randomUUID();

        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        Wallet wallet1 = walletRepository.save(Wallet.builder()
                .userId(user1Id)
                .tenantId(tenant1)
                .balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now())
                .build());

        Wallet wallet2 = walletRepository.save(Wallet.builder()
                .userId(user2Id)
                .tenantId(tenant2)
                .balance(new BigDecimal("500.00"))
                .createdAt(Instant.now())
                .build());

        for (int i = 0; i < 5; i++) {
            transactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet1)
                    .userId(user1Id)
                    .tenantId(tenant1)
                    .amount(new BigDecimal("10.00"))
                    .type(TransactionType.CREDIT)
                    .balanceAfter(new BigDecimal("1000.00").add(new BigDecimal("10.00").multiply(new BigDecimal(i + 1))))
                    .description("Test transaction " + i)
                    .referenceId(UUID.randomUUID().toString())
                    .createdAt(Instant.now().minusSeconds(3600L * i))
                    .build());
        }

        for (int i = 0; i < 3; i++) {
            transactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet2)
                    .userId(user2Id)
                    .tenantId(tenant2)
                    .amount(new BigDecimal("20.00"))
                    .type(TransactionType.DEBIT)
                    .balanceAfter(new BigDecimal("500.00").subtract(new BigDecimal("20.00").multiply(new BigDecimal(i + 1))))
                    .description("Other org transaction " + i)
                    .referenceId(UUID.randomUUID().toString())
                    .createdAt(Instant.now())
                    .build());
        }
    }

    @Test
    void orgAdminShouldSeeOrgTransactions() throws Exception {
        Authentication auth = mockAuth(admin1Id, tenant1, "ACTIVE", "ORG_ADMIN");

        mockMvc.perform(get("/api/organizations/transactions/" + tenant1)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void superAdminShouldSeeAllTransactions() throws Exception {
        Authentication auth = mockAuth(superAdminId, UUID.randomUUID(), "ACTIVE", "SUPER_ADMIN");

        mockMvc.perform(get("/api/platform/transactions")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(8));
    }

    @Test
    void shouldDownloadOrgPdfReport() throws Exception {
        Authentication auth = mockAuth(admin1Id, tenant1, "ACTIVE", "ORG_ADMIN");

        mockMvc.perform(get("/api/organizations/transactions/" + tenant1 + "/download")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void shouldDownloadPlatformPdfReport() throws Exception {
        Authentication auth = mockAuth(superAdminId, UUID.randomUUID(), "ACTIVE", "SUPER_ADMIN");

        mockMvc.perform(get("/api/platform/transactions/download")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void shouldDenyAccessIfTenantIdMismatch() throws Exception {
        Authentication auth = mockAuth(admin1Id, tenant2, "ACTIVE", "ORG_ADMIN");

        mockMvc.perform(get("/api/organizations/transactions/" + tenant1)
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }

    private Authentication mockAuth(UUID userId, UUID tenantId, String status, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r))
                .collect(Collectors.toList());
        String[] credentials = { tenantId.toString(), status };
        return new UsernamePasswordAuthenticationToken(userId, credentials, authorities);
    }
}
