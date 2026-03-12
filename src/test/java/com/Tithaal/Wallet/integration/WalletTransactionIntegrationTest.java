package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.DebitRequestDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WalletTransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private Wallet walletA;
    private Wallet walletB;
    private String tokenA;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        Organization org = organizationRepository.save(Organization.builder()
                .name("TestOrg")
                .orgCode("TORG")
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        userA = userRepository.save(User.builder()
                .username("usera")
                .email("usera@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .organization(org)
                .createdAt(Instant.now())
                .build());

        userB = userRepository.save(User.builder()
                .username("userb")
                .email("userb@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .organization(org)
                .createdAt(Instant.now())
                .build());

        walletA = walletRepository.save(Wallet.builder()
                .user(userA)
                .balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now())
                .build());

        walletB = walletRepository.save(Wallet.builder()
                .user(userB)
                .balance(new BigDecimal("0.00"))
                .createdAt(Instant.now())
                .build());

        // Login as User A
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("usera");
        loginDto.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        tokenA = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void shouldPerformSuccessfulTransfer() throws Exception {
        DebitRequestDto transferRequest = new DebitRequestDto();
        transferRequest.setSendingWalletId(walletA.getId());
        transferRequest.setReceivingWalletId(walletB.getId());
        transferRequest.setAmount(new BigDecimal("200.00"));

        mockMvc.perform(post("/api/user/" + userA.getId() + "/wallet/transfer")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk());

        // Assert balances directly from repository
        Wallet updatedWalletA = walletRepository.findById(walletA.getId()).orElseThrow();
        Wallet updatedWalletB = walletRepository.findById(walletB.getId()).orElseThrow();

        assert updatedWalletA.getBalance().compareTo(new BigDecimal("800.00")) == 0;
        assert updatedWalletB.getBalance().compareTo(new BigDecimal("200.00")) == 0;
    }

    @Test
    void shouldFailOnInsufficientFunds() throws Exception {
        DebitRequestDto transferRequest = new DebitRequestDto();
        transferRequest.setSendingWalletId(walletA.getId());
        transferRequest.setReceivingWalletId(walletB.getId());
        transferRequest.setAmount(new BigDecimal("1200.00"));

        mockMvc.perform(post("/api/user/" + userA.getId() + "/wallet/transfer")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    void shouldFailWhenRecipientIsInactive() throws Exception {
        // Deactivate User B
        userB.setStatus(UserStatus.INACTIVE);
        userRepository.save(userB);

        DebitRequestDto transferRequest = new DebitRequestDto();
        transferRequest.setSendingWalletId(walletA.getId());
        transferRequest.setReceivingWalletId(walletB.getId());
        transferRequest.setAmount(new BigDecimal("100.00"));

        // NOTE: This test will fail if the code doesn't currently check for ACTIVE status.
        // It serves as a proof of missing requirement as requested by the user.
        mockMvc.perform(post("/api/user/" + userA.getId() + "/wallet/transfer")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }
}
