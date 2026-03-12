package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.entity.Role;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GlobalUserManagementTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private OrganizationRepository organizationRepository;

        private User adminUser;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
                organizationRepository.deleteAll();

                Organization org = organizationRepository.save(Organization.builder()
                                .name("Test Org")
                                .orgCode("TEST001")
                                .status(OrganizationStatus.ACTIVE)
                                .createdAt(Instant.now())
                                .build());

                adminUser = userRepository.save(User.builder()
                                .username("superadmin")
                                .email("admin@example.com")
                                .passwordHash("hash")
                                .role(Role.ROLE_SUPER_ADMIN)
                                .status(UserStatus.ACTIVE)
                                .createdAt(Instant.now())
                                .build());

                userRepository.save(User.builder()
                                .username("john_doe")
                                .email("john@example.com")
                                .passwordHash("hash")
                                .role(Role.ROLE_USER)
                                .status(UserStatus.ACTIVE)
                                .organization(org)
                                .createdAt(Instant.now())
                                .build());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldReturnPaginatedUsersAcrossAllOrganizations() throws Exception {
                mockMvc.perform(get("/api/platform/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldFilterByEmailWithTrimmingAndCaseInsensitivity() throws Exception {
                mockMvc.perform(get("/api/platform/users")
                                .param("email", " JOHN@example.com "))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalElements").value(1))
                                .andExpect(jsonPath("$.content[0].username").value("john_doe"));
        }

        @Test
        void shouldPreventSuperAdminFromDeactivatingSelf() throws Exception {
                // Mock current security context with adminUser
                java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.Collections
                                .singletonList(
                                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                                "ROLE_SUPER_ADMIN"));
                CustomUserDetails userDetails = new CustomUserDetails(adminUser.getId(), adminUser.getEmail(),
                                adminUser.getPasswordHash(), authorities);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                mockMvc.perform(put("/api/platform/users/" + adminUser.getId() + "/status")
                                .param("status", "INACTIVE"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message")
                                                .value("Super Admin cannot deactivate their own account"));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldUpdateUserStatus() throws Exception {
                User user = userRepository.findByUsername("john_doe").get();

                mockMvc.perform(put("/api/platform/users/" + user.getId() + "/status")
                                .param("status", "SUSPENDED"))
                                .andExpect(status().isOk());

                User updated = userRepository.findById(user.getId()).get();
                assertEquals(UserStatus.SUSPENDED, updated.getStatus());
        }
}
