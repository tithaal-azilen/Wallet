package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Status enforcement tests — migrated to JWT / mock auth model.
 *
 * User-level status (SUSPENDED, DELETED, INACTIVE) is now enforced by the Auth Service
 * at token issuance time (i.e., blocked users don't get a valid JWT).
 * These tests focus on org-level status enforcement that remains in the Wallet service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StatusEnforcementIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;

    private Organization activeOrg;
    private Organization suspendedOrg;
    private Long senderWalletId;
    private Long recipientWalletId;
    private Long suspendedOrgId;
    private Long activeOrgId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        organizationRepository.deleteAll();

        activeOrg = organizationRepository.save(Organization.builder()
                .name("ActiveOrg").orgCode("ACT01")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());
        activeOrgId = activeOrg.getId();

        suspendedOrg = organizationRepository.save(Organization.builder()
                .name("SuspendedOrg").orgCode("SUS01")
                .status(OrganizationStatus.SUSPENDED).createdAt(Instant.now()).build());
        suspendedOrgId = suspendedOrg.getId();

        UUID userAId = UUID.randomUUID();
        UUID userBId = UUID.randomUUID();

        Wallet sender = walletRepository.save(Wallet.builder()
                .userId(userAId).balance(new BigDecimal("500.00"))
                .createdAt(Instant.now()).build());
        senderWalletId = sender.getId();

        Wallet recipient = walletRepository.save(Wallet.builder()
                .userId(userBId).balance(new BigDecimal("100.00"))
                .createdAt(Instant.now()).build());
        recipientWalletId = recipient.getId();
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void shouldAllowTransferForActiveUser() throws Exception {
        DebitRequestDto dto = new DebitRequestDto();
        dto.setSendingWalletId(senderWalletId);
        dto.setReceivingWalletId(recipientWalletId);
        dto.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/wallet/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void shouldAllowTopUpForValidUser() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/wallet/" + senderWalletId + "/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ORG_ADMIN"})
    void suspendedOrgCannotAccessReporting() throws Exception {
        mockMvc.perform(get("/api/organizations/transactions/" + suspendedOrgId)
                .param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ORG_ADMIN"})
    void activeOrgCanAccessReporting() throws Exception {
        mockMvc.perform(get("/api/organizations/transactions/" + activeOrgId)
                .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());
    }
}
