package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.entity.TransactionType;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class WalletHistoryServiceImplTest {

        @Mock
        private WalletTransactionRepository walletTransactionRepository;

        @InjectMocks
        private WalletHistoryServiceImpl walletHistoryService;

        // --- getUserHistory Tests ---

        @Test
        void getUserHistory_Success() {
                java.util.UUID userId = java.util.UUID.randomUUID();


                Wallet wallet = new Wallet();
                wallet.setId(101L);

                WalletTransaction t1 = WalletTransaction.builder()
                                .id(1L)
                                .wallet(wallet)
                                .type(TransactionType.CREDIT)
                                .amount(BigDecimal.TEN)
                                .description("TopUp")
                                .balanceAfter(BigDecimal.TEN)
                                .createdAt(Instant.now())
                                .build();

                WalletTransaction t2 = WalletTransaction.builder()
                                .id(2L)
                                .wallet(wallet)
                                .type(TransactionType.DEBIT)
                                .amount(BigDecimal.ONE)
                                .description("Transfer")
                                .balanceAfter(BigDecimal.valueOf(9))
                                .createdAt(Instant.now().minusSeconds(60)) // Older
                                .build();

                org.springframework.data.domain.Page<WalletTransaction> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(t1, t2));
                when(walletTransactionRepository.findAll(Mockito.<org.springframework.data.jpa.domain.Specification<WalletTransaction>>any(), any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(page);

                com.Tithaal.Wallet.dto.PagedResponse<WalletTransactionEntryDto> result = walletHistoryService.getUserHistory(userId, null, 0, 10, "createdAt", "desc");

                assertEquals(2, result.getContent().size());
                assertEquals(t1.getId(), result.getContent().get(0).getId());
                assertEquals(t2.getId(), result.getContent().get(1).getId());
        }

        @Test
        void getUserHistory_Success_Empty() {
                java.util.UUID userId = java.util.UUID.randomUUID();

                
                org.springframework.data.domain.Page<WalletTransaction> page = new org.springframework.data.domain.PageImpl<>(Collections.emptyList());
                when(walletTransactionRepository.findAll(Mockito.<org.springframework.data.jpa.domain.Specification<WalletTransaction>>any(), any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(page);

                com.Tithaal.Wallet.dto.PagedResponse<WalletTransactionEntryDto> result = walletHistoryService.getUserHistory(userId, null, 0, 10, "createdAt", "desc");

                assertNotNull(result);
                assertTrue(result.getContent().isEmpty());
        }

        // --- getWalletHistory Tests ---

        @Test
        void getWalletHistory_Success() {
                Long walletId = 101L;

                Wallet wallet = new Wallet();
                wallet.setId(walletId);

                WalletTransaction t1 = WalletTransaction.builder()
                                .id(1L)
                                .wallet(wallet)
                                .type(TransactionType.CREDIT)
                                .amount(BigDecimal.valueOf(50))
                                .description("Init")
                                .balanceAfter(BigDecimal.valueOf(50))
                                .createdAt(Instant.now())
                                .build();

                when(walletTransactionRepository.findAllByWalletIdOrderByCreatedAtDesc(walletId))
                                .thenReturn(Collections.singletonList(t1));

                List<WalletTransactionEntryDto> result = walletHistoryService.getWalletHistory(walletId);

                assertEquals(1, result.size());
                assertEquals(t1.getId(), result.get(0).getId());
                assertEquals(walletId, result.get(0).getWalletId());
        }

        @Test
        void getWalletHistory_Success_Empty() {
                Long walletId = 101L;
                when(walletTransactionRepository.findAllByWalletIdOrderByCreatedAtDesc(walletId))
                                .thenReturn(Collections.emptyList());

                List<WalletTransactionEntryDto> result = walletHistoryService.getWalletHistory(walletId);

                assertNotNull(result);
                assertTrue(result.isEmpty());
        }
}
