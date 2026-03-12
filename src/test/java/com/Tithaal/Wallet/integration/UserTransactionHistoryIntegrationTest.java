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
public class UserTransactionHistoryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User user;
    private Wallet wallet;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        Organization org = organizationRepository.save(Organization.builder()
                .name("HistOrg").orgCode("HIST01")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        user = userRepository.save(User.builder()
                .username("histuser").email("histuser@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(org).createdAt(Instant.now()).build());

        wallet = walletRepository.save(Wallet.builder()
                .user(user).balance(new BigDecimal("500.00"))
                .createdAt(Instant.now()).build());

        // Seed 5 transactions
        for (int i = 0; i < 5; i++) {
            transactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet)
                    .amount(new BigDecimal("10.00"))
                    .type(i % 2 == 0 ? TransactionType.CREDIT : TransactionType.DEBIT)
                    .balanceAfter(new BigDecimal("500.00"))
                    .description("Test tx " + i)
                    .referenceId(UUID.randomUUID().toString())
                    .createdAt(Instant.now().minusSeconds(3600 * i))
                    .build());
        }

        userToken = obtainToken("histuser", "password");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail(username);
        loginDto.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void shouldGetPaginatedUserHistory() throws Exception {
        mockMvc.perform(get("/api/ledger/" + user.getId())
                .header("Authorization", "Bearer " + userToken)
                .param("page", "0").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    void shouldGetWalletSpecificHistory() throws Exception {
        mockMvc.perform(get("/api/ledger/" + user.getId() + "/wallet/" + wallet.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void shouldDownloadUserHistoryPdf() throws Exception {
        mockMvc.perform(get("/api/ledger/" + user.getId() + "/download")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void shouldDenyAccessToOtherUserHistory() throws Exception {
        User other = userRepository.save(User.builder()
                .username("other2").email("other2@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(user.getOrganization()).createdAt(Instant.now()).build());

        mockMvc.perform(get("/api/ledger/" + other.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
