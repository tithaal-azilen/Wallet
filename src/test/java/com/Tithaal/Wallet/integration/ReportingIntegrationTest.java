package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReportingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization org1;
    private Organization org2;
    private User admin1;
    private User superAdmin;
    private String admin1Token;
    private String superAdminToken;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // Setup Org 1
        org1 = organizationRepository.save(Organization.builder()
                .name("Org One")
                .orgCode("ORG1")
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        admin1 = userRepository.save(User.builder()
                .username("admin1")
                .email("admin1@org1.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_ORG_ADMIN)
                .organization(org1)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        User user1 = userRepository.save(User.builder()
                .username("user1")
                .email("user1@org1.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER)
                .organization(org1)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        Wallet wallet1 = walletRepository.save(Wallet.builder()
                .user(user1)
                .balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now())
                .build());

        // Setup Org 2
        org2 = organizationRepository.save(Organization.builder()
                .name("Org Two")
                .orgCode("ORG2")
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        User user2 = userRepository.save(User.builder()
                .username("user2")
                .email("user2@org2.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER)
                .organization(org2)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        Wallet wallet2 = walletRepository.save(Wallet.builder()
                .user(user2)
                .balance(new BigDecimal("500.00"))
                .createdAt(Instant.now())
                .build());

        // Setup Super Admin
        superAdmin = userRepository.save(User.builder()
                .username("superadmin")
                .email("superadmin@platform.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        // Generate Transactions
        for (int i = 0; i < 5; i++) {
            transactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet1)
                    .amount(new BigDecimal("10.00"))
                    .type(TransactionType.CREDIT)
                    .balanceAfter(new BigDecimal("1000.00").add(new BigDecimal("10.00").multiply(new BigDecimal(i + 1))))
                    .description("Test transaction " + i)
                    .referenceId(UUID.randomUUID().toString())
                    .createdAt(Instant.now().minusSeconds(3600 * i))
                    .build());
        }

        for (int i = 0; i < 3; i++) {
            transactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet2)
                    .amount(new BigDecimal("20.00"))
                    .type(TransactionType.DEBIT)
                    .balanceAfter(new BigDecimal("500.00").subtract(new BigDecimal("20.00").multiply(new BigDecimal(i + 1))))
                    .description("Other org transaction " + i)
                    .referenceId(UUID.randomUUID().toString())
                    .createdAt(Instant.now())
                    .build());
        }

        // Login Admin 1
        admin1Token = obtainToken("admin1", "password");
        // Login Super Admin
        superAdminToken = obtainToken("superadmin", "password");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail(username);
        loginDto.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void orgAdminShouldSeeOnlyTheirOrgTransactions() throws Exception {
        mockMvc.perform(get("/api/organizations/transactions/" + org1.getId())
                .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content[0].username").value("user1"));
    }

    @Test
    void orgAdminShouldFailToSeeOtherOrgTransactions() throws Exception {
        mockMvc.perform(get("/api/organizations/transactions/" + org2.getId())
                .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminShouldSeeAllTransactions() throws Exception {
        mockMvc.perform(get("/api/platform/transactions")
                .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(8));
    }

    @Test
    void shouldDownloadOrgPdfReport() throws Exception {
        mockMvc.perform(get("/api/organizations/transactions/" + org1.getId() + "/download")
                .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void shouldDownloadPlatformPdfReport() throws Exception {
        mockMvc.perform(get("/api/platform/transactions/download")
                .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }
}
