package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrganizationOversightTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private OrganizationRepository organizationRepository;

        @Autowired

        @BeforeEach
        void setUp() {
                organizationRepository.deleteAll();

                organizationRepository.save(Organization.builder()
                                .name("Paytm")
                                .orgCode("PAYTM001")
                                .status(OrganizationStatus.ACTIVE)
                                .createdAt(Instant.now().minus(10, ChronoUnit.DAYS))
                                .build());

                organizationRepository.save(Organization.builder()
                                .name("Google")
                                .orgCode("GOOGL002")
                                .status(OrganizationStatus.SUSPENDED)
                                .createdAt(Instant.now())
                                .build());

                organizationRepository.save(Organization.builder()
                                .name("Amazon")
                                .orgCode("AMZ003")
                                .status(OrganizationStatus.DELETED)
                                .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                                .build());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldReturnPaginatedOrganizations() throws Exception {
                mockMvc.perform(get("/api/platform/organizations")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.totalElements").value(2)); // DELETED should be excluded
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldFilterByOrganizationNameWithTrimming() throws Exception {
                mockMvc.perform(get("/api/platform/organizations")
                                .param("name", "  Paytm  "))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalElements").value(1))
                                .andExpect(jsonPath("$.content[0].name").value("Paytm"));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldUpdateOrganizationStatus() throws Exception {
                Organization org = organizationRepository.findAll().get(0);

                mockMvc.perform(put("/api/platform/organizations/" + org.getId() + "/status")
                                .param("status", "SUSPENDED"))
                                .andExpect(status().isOk());

                Organization updated = organizationRepository.findById(org.getId()).get();
                assertEquals(OrganizationStatus.SUSPENDED, updated.getStatus());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        void shouldPreventActivatingDeletedOrganization() throws Exception {
                Organization deletedOrg = organizationRepository.save(Organization.builder()
                                .name("Deleted Org")
                                .orgCode("DEL001")
                                .status(OrganizationStatus.DELETED)
                                .createdAt(Instant.now())
                                .build());

                mockMvc.perform(put("/api/platform/organizations/" + deletedOrg.getId() + "/status")
                                .param("status", "ACTIVE"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "USER")
        void shouldDenyUserAccessOrganizationList() throws Exception {
                mockMvc.perform(get("/api/platform/organizations"))
                                .andExpect(status().isForbidden());
        }
}
