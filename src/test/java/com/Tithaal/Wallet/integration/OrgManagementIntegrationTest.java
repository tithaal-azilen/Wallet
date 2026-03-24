package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.OrganizationUpdateDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.redis.RefreshTokenRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrgManagementIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private Organization org1;
    private Organization org2;
    private User admin1;
    private User admin2;
    private String admin1Token;
    private String admin2Token;

    @BeforeEach
    void setUp() throws Exception {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        org1 = organizationRepository.save(Organization.builder()
                .name("Alpha Corp").orgCode("ALPHA01")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        org2 = organizationRepository.save(Organization.builder()
                .name("Beta Corp").orgCode("BETA01")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        admin1 = userRepository.save(User.builder()
                .username("admin1").email("admin1@alpha.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_ORG_ADMIN).status(UserStatus.ACTIVE)
                .organization(org1).createdAt(Instant.now()).build());

        admin2 = userRepository.save(User.builder()
                .username("admin2").email("admin2@beta.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ROLE_ORG_ADMIN).status(UserStatus.ACTIVE)
                .organization(org2).createdAt(Instant.now()).build());

        // Add some users to org1
        for (int i = 0; i < 3; i++) {
            userRepository.save(User.builder()
                    .username("orguser" + i).email("orguser" + i + "@alpha.com")
                    .passwordHash(passwordEncoder.encode("password"))
                    .role(Role.ROLE_USER).status(UserStatus.ACTIVE)
                    .organization(org1).createdAt(Instant.now()).build());
        }

        admin1Token = obtainToken("admin1", "password");
        admin2Token = obtainToken("admin2", "password");
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
    void shouldGetOwnOrganizationDetails() throws Exception {
        mockMvc.perform(get("/api/organizations/manage/" + org1.getId())
                .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alpha Corp"));
    }

    @Test
    void shouldDenyAccessToOtherOrganizationDetails() throws Exception {
        mockMvc.perform(get("/api/organizations/manage/" + org2.getId())
                .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateOwnOrganization() throws Exception {
        OrganizationUpdateDto dto = OrganizationUpdateDto.builder().name("Alpha Corp Updated").build();

        mockMvc.perform(put("/api/organizations/manage/" + org1.getId())
                .header("Authorization", "Bearer " + admin1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyUpdateToOtherOrganization() throws Exception {
        OrganizationUpdateDto dto = OrganizationUpdateDto.builder().name("Hacked Name").build();

        mockMvc.perform(put("/api/organizations/manage/" + org2.getId())
                .header("Authorization", "Bearer " + admin1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetPaginatedOrganizationUsers() throws Exception {
        mockMvc.perform(get("/api/organizations/manage/" + org1.getId() + "/users")
                .header("Authorization", "Bearer " + admin1Token)
                .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4)); // 3 users + 1 admin
    }

    @Test
    void shouldDenyListingUsersOfOtherOrganization() throws Exception {
        mockMvc.perform(get("/api/organizations/manage/" + org1.getId() + "/users")
                .header("Authorization", "Bearer " + admin2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSoftDeleteOwnOrganizationAfterDeactivatingUsers() throws Exception {
        // Deactivate all users in org1 (including admin)
        userRepository.findAll().stream()
                .filter(u -> u.getOrganization() != null && u.getOrganization().getId().equals(org1.getId()))
                .forEach(u -> {
                    u.setStatus(UserStatus.INACTIVE);
                    userRepository.save(u);
                });

        // Use Spring Security Test utilities to inject authentication since JWT filter rejects inactive users
        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.Collections
                .singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ORG_ADMIN"));
        com.Tithaal.Wallet.security.CustomUserDetails userDetails = new com.Tithaal.Wallet.security.CustomUserDetails(
                admin1.getId(), admin1.getEmail(), admin1.getPasswordHash(), authorities);
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        mockMvc.perform(delete("/api/organizations/manage/" + org1.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk());

        Organization deleted = organizationRepository.findById(org1.getId()).orElseThrow();
        assertEquals(OrganizationStatus.DELETED, deleted.getStatus());
    }




    @Test
    void shouldFailSoftDeleteWhenOrgHasActiveUsers() throws Exception {
        // Org1 has active users, so soft-delete should fail
        mockMvc.perform(delete("/api/organizations/manage/" + org1.getId())
                .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isBadRequest());
    }
}
