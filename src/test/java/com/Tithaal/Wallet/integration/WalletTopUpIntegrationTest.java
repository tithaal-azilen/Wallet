package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WalletTopUpIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User user;
    private Wallet wallet;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        walletRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        Organization org = organizationRepository.save(Organization.builder()
                .name("TopUpOrg").orgCode("TOPUP01")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        user = userRepository.save(User.builder()
                .username("topupuser").email("topupuser@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(org).createdAt(Instant.now()).build());

        wallet = walletRepository.save(Wallet.builder()
                .user(user).balance(new BigDecimal("100.00"))
                .createdAt(Instant.now()).build());

        userToken = obtainToken("topupuser", "password");
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
    void shouldTopUpWalletSuccessfully() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(new BigDecimal("250.00"));

        mockMvc.perform(post("/api/user/" + user.getId() + "/wallet/" + wallet.getId() + "/payment")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("350.00").compareTo(updated.getBalance()));
    }

    @Test
    void shouldFailTopUpWithZeroAmount() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/user/" + user.getId() + "/wallet/" + wallet.getId() + "/payment")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailTopUpWithNegativeAmount() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(new BigDecimal("-50.00"));

        mockMvc.perform(post("/api/user/" + user.getId() + "/wallet/" + wallet.getId() + "/payment")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailTopUpWithMissingCardNumber() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/user/" + user.getId() + "/wallet/" + wallet.getId() + "/payment")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailTopUpOnOtherUsersWallet() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .username("other").email("other@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(user.getOrganization()).createdAt(Instant.now()).build());

        Wallet otherWallet = walletRepository.save(Wallet.builder()
                .user(otherUser).balance(new BigDecimal("100.00"))
                .createdAt(Instant.now()).build());

        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(new BigDecimal("50.00"));

        // Using testUser's token but otherUser's userId → should be forbidden by @PreAuthorize
        mockMvc.perform(post("/api/user/" + otherUser.getId() + "/wallet/" + otherWallet.getId() + "/payment")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
