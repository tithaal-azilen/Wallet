package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrganizationOnboardingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void shouldRegisterOrgAndVerifyOwnership() throws Exception {
        // 1. Register Org 1
        OrganizationRegistrationDto reg1 = new OrganizationRegistrationDto();
        reg1.setOrgName("Organization One");
        reg1.setUsername("admin1");
        reg1.setEmail("admin1@org1.com");
        reg1.setPassword("password123");
        reg1.setCity("Pune");
        reg1.setPhoneNumber("1234567890");

        mockMvc.perform(post("/api/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg1)))
                .andExpect(status().isCreated());

        // 2. Register Org 2
        OrganizationRegistrationDto reg2 = new OrganizationRegistrationDto();
        reg2.setOrgName("Organization Two");
        reg2.setUsername("admin2");
        reg2.setEmail("admin2@org2.com");
        reg2.setPassword("password123");
        reg2.setCity("Mumbai");
        reg2.setPhoneNumber("0987654321");

        mockMvc.perform(post("/api/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isCreated());

        // 3. Duplicate Org Name check
        mockMvc.perform(post("/api/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg1)))
                .andExpect(status().isBadRequest()); // Based on DomainException handling

        // 4. Login as Admin 1
        LoginDto login1 = new LoginDto();
        login1.setUsernameOrEmail("admin1");
        login1.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login1)))
                .andExpect(status().isOk())
                .andReturn();

        String token1 = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
        
        Organization org1 = organizationRepository.findByName("Organization One").orElseThrow();
        Organization org2 = organizationRepository.findByName("Organization Two").orElseThrow();

        // 5. Admin 1 should access Org 1
        mockMvc.perform(get("/api/organizations/manage/" + org1.getId())
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Organization One"));

        // 6. Admin 1 should NOT access Org 2 (Ownership Check)
        mockMvc.perform(get("/api/organizations/manage/" + org2.getId())
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isForbidden());
    }
}
