package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeDeductionServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private FeeDeductionService feeDeductionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(feeDeductionService, "monthlyFeeAmount", new BigDecimal("50.0"));
    }

    @Test
    void deductFees_shouldProcessEligibleWallets() {
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(new BigDecimal("100.0"));
        wallet.setNextDeductionDate(LocalDate.now());

        when(walletRepository.findWalletsDueForDeduction(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(wallet))
                .thenReturn(Collections.emptyList());

        // Mock findWithLockingById because processSingleWallet calls it
        when(walletRepository.findWithLockingById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        feeDeductionService.deductFees();

        verify(walletRepository, times(1)).save(wallet);
        verify(walletTransactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void processSingleWallet_shouldDeductFee_whenBalanceIsSufficient() {
        Long walletId = 1L;
        LocalDate today = LocalDate.now();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("100.0"));
        wallet.setNextDeductionDate(today);

        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        feeDeductionService.processSingleWallet(walletId);

        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
        assert wallet.getBalance().compareTo(new BigDecimal("50.0")) == 0;
        assert wallet.getNextDeductionDate().equals(today.plusMonths(1));
        assert wallet.getLastDeductionAttempt().equals(today);
    }

    @Test
    void processSingleWallet_shouldNotDeductFee_whenBalanceIsInsufficient() {
        Long walletId = 1L;
        LocalDate today = LocalDate.now();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("10.0")); // Less than fee 50.0
        wallet.setNextDeductionDate(today);

        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        feeDeductionService.processSingleWallet(walletId);

        verify(walletRepository).save(wallet); // Saved to update lastDeductionAttempt
        verify(walletTransactionRepository, never()).save(any(WalletTransaction.class));
        assert wallet.getBalance().compareTo(new BigDecimal("10.0")) == 0; // Balance unchanged
        assert wallet.getNextDeductionDate().equals(today); // Date NOT advanced
        assert wallet.getLastDeductionAttempt().equals(today); // Attempt recorded
    }

    @Test
    void processSingleWallet_shouldSkip_whenAlreadyprocessedToday() {
        Long walletId = 1L;
        LocalDate today = LocalDate.now();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setLastDeductionAttempt(today);
        wallet.setNextDeductionDate(today);

        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(wallet));

        feeDeductionService.processSingleWallet(walletId);

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(walletTransactionRepository, never()).save(any(WalletTransaction.class));
    }
}
