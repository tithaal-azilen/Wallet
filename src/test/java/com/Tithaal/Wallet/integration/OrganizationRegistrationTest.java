package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrganizationRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void shouldRegisterOrganizationSuccessfullyWithTrimming() throws Exception {
        OrganizationRegistrationDto dto = new OrganizationRegistrationDto();
        dto.setOrgName("  Microsoft  ");
        dto.setUsername("msadmin");
        dto.setEmail("admin@ms.com");
        dto.setCity("Redmond");
        dto.setPhoneNumber("1234567890");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        Optional<Organization> org = organizationRepository.findAll().stream().findFirst();
        assertTrue(org.isPresent());
        assertEquals("Microsoft", org.get().getName());
        assertTrue(org.get().getOrgCode().startsWith("MIC"));

        Optional<User> admin = userRepository.findByUsername("msadmin");
        assertTrue(admin.isPresent());
        assertEquals("admin@ms.com", admin.get().getEmail());
    }

    @Test
    void shouldFailWhenOrganizationNameAlreadyExists() throws Exception {
        organizationRepository.save(Organization.builder()
                .name("Google")
                .orgCode("GOOGL001")
                .status(com.Tithaal.Wallet.entity.OrganizationStatus.ACTIVE)
                .createdAt(java.time.Instant.now())
                .build());

        OrganizationRegistrationDto dto = new OrganizationRegistrationDto();
        dto.setOrgName("Google");
        dto.setUsername("admin2");
        dto.setEmail("admin2@google.com");
        dto.setCity("Mountain View");
        dto.setPhoneNumber("0987654321");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailOnInvalidPhoneNumber() throws Exception {
        OrganizationRegistrationDto dto = new OrganizationRegistrationDto();
        dto.setOrgName("Apple");
        dto.setUsername("appleadmin");
        dto.setEmail("admin@apple.com");
        dto.setCity("Cupertino");
        dto.setPhoneNumber("123"); // Invalid
        dto.setPassword("password123");

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
