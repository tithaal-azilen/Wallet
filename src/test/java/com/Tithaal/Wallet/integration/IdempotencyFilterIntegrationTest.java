package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled("Requires real Redis instance")
public class IdempotencyFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    private DebitRequestDto transferDto;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        Wallet senderWallet = walletRepository.save(Wallet.builder()
                .userId(userId)
                .balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now())
                .build());

        Wallet receiverWallet = walletRepository.save(Wallet.builder()
                .userId(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build());

        transferDto = new DebitRequestDto();
        transferDto.setSendingWalletId(senderWallet.getId());
        transferDto.setReceivingWalletId(receiverWallet.getId());
        transferDto.setAmount(new BigDecimal("100.00"));

        String[] credentials = { tenantId.toString(), "ACTIVE" };
        auth = new UsernamePasswordAuthenticationToken(userId, credentials, List.of());
    }

    @Test
    void shouldReturnCachedResponseForDuplicateIdempotencyKey() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        MvcResult firstResponse = mockMvc.perform(post("/api/wallet/transfer")
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult secondResponse = mockMvc.perform(post("/api/wallet/transfer")
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(firstResponse.getResponse().getContentAsString(), secondResponse.getResponse().getContentAsString());
    }
}
