package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StatusEnforcementIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private com.Tithaal.Wallet.repository.RefreshTokenRepository refreshTokenRepository;

    private Organization activeOrg;
    private Organization deletedOrg;
    private Organization suspendedOrg;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        activeOrg = organizationRepository.save(Organization.builder()
                .name("ActiveOrg").orgCode("ACT01")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        deletedOrg = organizationRepository.save(Organization.builder()
                .name("DeletedOrg").orgCode("DEL01")
                .status(OrganizationStatus.DELETED).createdAt(Instant.now()).build());

        suspendedOrg = organizationRepository.save(Organization.builder()
                .name("SuspendedOrg").orgCode("SUS01")
                .status(OrganizationStatus.SUSPENDED).createdAt(Instant.now()).build());
    }

    private LoginDto loginDto(String username, String password) {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail(username);
        dto.setPassword(password);
        return dto;
    }

    private String obtainToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto(username, "password"))))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private UsernamePasswordAuthenticationToken authFor(User user, String role) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        CustomUserDetails details = new CustomUserDetails(
                user.getId(), user.getEmail(), user.getPasswordHash(), authorities);
        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    // ==================== USER LOGIN TESTS ====================

    @Test
    void deletedUserCannotSignIn() throws Exception {
        userRepository.save(User.builder()
                .username("deleteduser").email("deleted@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.DELETED)
                .organization(activeOrg).createdAt(Instant.now()).build());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto("deleteduser", "password"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void inactiveUserCannotSignIn() throws Exception {
        userRepository.save(User.builder()
                .username("inactiveuser").email("inactive@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.INACTIVE)
                .organization(activeOrg).createdAt(Instant.now()).build());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto("inactiveuser", "password"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void suspendedUserCanSignIn() throws Exception {
        userRepository.save(User.builder()
                .username("suspuser").email("susp@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.SUSPENDED)
                .organization(activeOrg).createdAt(Instant.now()).build());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto("suspuser", "password"))))
                .andExpect(status().isOk());
    }

    @Test
    void activeUserCanSignIn() throws Exception {
        userRepository.save(User.builder()
                .username("activeuser").email("active@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(activeOrg).createdAt(Instant.now()).build());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto("activeuser", "password"))))
                .andExpect(status().isOk());
    }

    // ==================== DELETED ORG USER TESTS ====================

    @Test
    void userOfDeletedOrgCannotSignIn() throws Exception {
        userRepository.save(User.builder()
                .username("delorgusr").email("delorg@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(deletedOrg).createdAt(Instant.now()).build());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto("delorgusr", "password"))))
                .andExpect(status().isForbidden());
    }

    // ==================== SUSPENDED USER TRANSFER TESTS ====================

    @Test
    void suspendedUserCannotTransfer() throws Exception {
        User suspUser = userRepository.save(User.builder()
                .username("suspxfer").email("suspxfer@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.SUSPENDED)
                .organization(activeOrg).createdAt(Instant.now()).build());

        User recipient = userRepository.save(User.builder()
                .username("recipient").email("recip@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(activeOrg).createdAt(Instant.now()).build());

        Wallet senderWallet = walletRepository.save(Wallet.builder()
                .user(suspUser).balance(new BigDecimal("500.00"))
                .createdAt(Instant.now()).build());

        Wallet recipWallet = walletRepository.save(Wallet.builder()
                .user(recipient).balance(new BigDecimal("100.00"))
                .createdAt(Instant.now()).build());

        DebitRequestDto dto = new DebitRequestDto();
        dto.setSendingWalletId(senderWallet.getId());
        dto.setReceivingWalletId(recipWallet.getId());
        dto.setAmount(new BigDecimal("50.00"));

        // Suspended user can sign in — use their auth context
        mockMvc.perform(post("/api/user/" + suspUser.getId() + "/wallet/transfer")
                .with(authentication(authFor(suspUser, "ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void suspendedUserCanTopUp() throws Exception {
        User suspUser = userRepository.save(User.builder()
                .username("susptp").email("susptp@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.SUSPENDED)
                .organization(activeOrg).createdAt(Instant.now()).build());

        Wallet wallet = walletRepository.save(Wallet.builder()
                .user(suspUser).balance(new BigDecimal("0.00"))
                .createdAt(Instant.now()).build());

        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/user/" + suspUser.getId() + "/wallet/" + wallet.getId() + "/payment")
                .with(authentication(authFor(suspUser, "ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== ORG STATUS REPORTING TESTS ====================

    @Test
    void suspendedOrgCannotAccessReporting() throws Exception {
        User admin = userRepository.save(User.builder()
                .username("suspadmin").email("suspadmin@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_ORG_ADMIN).status(UserStatus.ACTIVE)
                .organization(suspendedOrg).createdAt(Instant.now()).build());

        mockMvc.perform(get("/api/organizations/transactions/" + suspendedOrg.getId())
                .with(authentication(authFor(admin, "ROLE_ORG_ADMIN")))
                .param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activeOrgCanAccessReporting() throws Exception {
        User admin = userRepository.save(User.builder()
                .username("actadmin").email("actadmin@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_ORG_ADMIN).status(UserStatus.ACTIVE)
                .organization(activeOrg).createdAt(Instant.now()).build());

        mockMvc.perform(get("/api/organizations/transactions/" + activeOrg.getId())
                .with(authentication(authFor(admin, "ROLE_ORG_ADMIN")))
                .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());
    }
}
