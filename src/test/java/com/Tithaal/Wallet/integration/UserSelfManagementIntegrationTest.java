package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.UpdateUserDto;
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

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserSelfManagementIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired
    private com.Tithaal.Wallet.repository.RefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private User otherUser;
    private String testUserToken;

    @BeforeEach
    void setUp() throws Exception {
        refreshTokenRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        Organization org = organizationRepository.save(Organization.builder()
                .name("TestOrg").orgCode("TORG01")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        testUser = userRepository.save(User.builder()
                .username("alice").email("alice@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(org).createdAt(Instant.now()).build());

        otherUser = userRepository.save(User.builder()
                .username("bob").email("bob@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                .organization(org).createdAt(Instant.now()).build());

        testUserToken = obtainToken("alice", "password");
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
    void shouldGetOwnUserDetails() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@test.com"));
    }

    @Test
    void shouldNotGetOtherUserDetails() throws Exception {
        mockMvc.perform(get("/api/users/" + otherUser.getId())
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateOwnProfile() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setCity("Mumbai");
        updateDto.setPhoneNumber("9999999999");

        mockMvc.perform(put("/api/users/" + testUser.getId())
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotUpdateOtherUserProfile() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setCity("Delhi");

        mockMvc.perform(put("/api/users/" + otherUser.getId())
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAddWalletToOwnAccount() throws Exception {
        mockMvc.perform(post("/api/users/" + testUser.getId() + "/addwallet")
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldNotAddWalletToOtherUser() throws Exception {
        mockMvc.perform(post("/api/users/" + otherUser.getId() + "/addwallet")
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isForbidden());
    }
}
