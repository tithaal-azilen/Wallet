package com.Tithaal.Wallet.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfiguration
public class MethodSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    public void platformEndpoint_ShouldDenyUser() throws Exception {
        mockMvc.perform(get("/api/platform/organizations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void platformEndpoint_ShouldAllowSuperAdmin() throws Exception {
        mockMvc.perform(get("/api/platform/organizations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ORG_ADMIN")
    public void platformEndpoint_ShouldDenyOrgAdmin() throws Exception {
        mockMvc.perform(get("/api/platform/organizations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void userEndpoint_ShouldAllowUser() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk());
    }
}
