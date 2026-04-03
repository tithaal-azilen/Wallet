package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    // --- TopUpWallet Tests ---

    @Test
    void TopUpWallet_Success() {
        Long walletId = 1L;
        UUID userId = UUID.randomUUID();
        CreditRequestDto creditDto = new CreditRequestDto();
        creditDto.setAmount(BigDecimal.TEN);
        creditDto.setCreditCardNumber("1234567890123456");

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(new WalletTransaction());

        String result = walletService.topUpWallet(walletId, creditDto, userId);


        assertEquals("Wallet TopUp successfully ", result);
        assertEquals(BigDecimal.TEN, wallet.getBalance());
        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void TopUpWallet_Fail_InvalidAmount() {
        Long walletId = 1L;
        UUID userId = UUID.randomUUID();
        CreditRequestDto creditDto = new CreditRequestDto();
        creditDto.setAmount(BigDecimal.ZERO);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        DomainException exception = assertThrows(DomainException.class,
                () -> walletService.topUpWallet(walletId, creditDto, userId));

        assertEquals("Amount must be greater than 0", exception.getMessage());
    }

    @Test
    void TopUpWallet_Fail_WalletNotFound() {
        Long walletId = 1L;
        UUID userId = UUID.randomUUID();
        CreditRequestDto creditDto = new CreditRequestDto();
        creditDto.setAmount(BigDecimal.TEN);

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> walletService.topUpWallet(walletId, creditDto, userId));

        assertTrue(exception.getMessage().contains("Wallet not found"));
    }

    // --- Transfer Tests ---

    @Test
    void Transfer_Success() {
        UUID userId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(2L);
        debitDto.setAmount(BigDecimal.TEN);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUserId(userId);
        senderWallet.setBalance(new BigDecimal("100"));

        Wallet recipientWallet = new Wallet();
        recipientWallet.setId(2L);
        recipientWallet.setUserId(recipientUserId);
        recipientWallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockingById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockingById(2L)).thenReturn(Optional.of(recipientWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(new WalletTransaction());

        String result = walletService.transfer(debitDto, userId, "ACTIVE");


        assertEquals("Transfer Successful! ", result);
        assertEquals(new BigDecimal("90"), senderWallet.getBalance());
        assertEquals(BigDecimal.TEN, recipientWallet.getBalance());
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(walletTransactionRepository, times(2)).save(any(WalletTransaction.class));
    }

    @Test
    void Transfer_Fail_InvalidAmount() {
        UUID userId = UUID.randomUUID();
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setSendingWalletId(1L);
        debitDto.setAmount(BigDecimal.ZERO);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUserId(userId);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));

        DomainException exception = assertThrows(DomainException.class, () -> walletService.transfer(debitDto, userId, "ACTIVE"));

        assertEquals("Amount must be greater than 0", exception.getMessage());
    }

    @Test
    void Transfer_Fail_SameWallet() {
        UUID userId = UUID.randomUUID();
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setAmount(BigDecimal.TEN);
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(1L);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUserId(userId);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));

        DomainException exception = assertThrows(DomainException.class, () -> walletService.transfer(debitDto, userId, "ACTIVE"));

        assertEquals("Cannot transfer funds to the same wallet", exception.getMessage());
    }

    @Test
    void Transfer_Fail_SenderWalletNotFound() {
        UUID userId = UUID.randomUUID();
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setAmount(BigDecimal.TEN);
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(2L);

        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () -> walletService.transfer(debitDto, userId, "ACTIVE"));

        assertTrue(exception.getMessage().contains("Wallet not found"));
    }

    @Test
    void Transfer_Fail_RecipientWalletNotFound() {
        UUID userId = UUID.randomUUID();
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setAmount(BigDecimal.TEN);
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(2L);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUserId(userId);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockingById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockingById(2L)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () -> walletService.transfer(debitDto, userId, "ACTIVE"));

        assertTrue(exception.getMessage().contains("Recipient wallet not found"));
    }

    @Test
    void Transfer_Fail_InsufficientBalance() {
        UUID userId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setAmount(new BigDecimal("100"));
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(2L);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUserId(userId);
        senderWallet.setBalance(BigDecimal.TEN); // 10 < 100

        Wallet recipientWallet = new Wallet();
        recipientWallet.setId(2L);
        recipientWallet.setUserId(recipientUserId);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockingById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockingById(2L)).thenReturn(Optional.of(recipientWallet));

        DomainException exception = assertThrows(DomainException.class, () -> walletService.transfer(debitDto, userId, "ACTIVE"));

        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void shouldHandleDecimalAmountsCorrectly() {
        Long walletId = 1L;
        UUID userId = UUID.randomUUID();
        CreditRequestDto creditDto = new CreditRequestDto();
        creditDto.setAmount(new BigDecimal("0.10"));
        creditDto.setCreditCardNumber("1234");

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("0.20"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.findWithLockingById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(new WalletTransaction());

        walletService.topUpWallet(walletId, creditDto, userId);


        assertEquals(0, new BigDecimal("0.30").compareTo(wallet.getBalance()));
    }

    // --- validateWalletOwnership Tests ---

    @Test
    void validateWalletOwnership_Success() {
        Long walletId = 1L;
        UUID userId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertDoesNotThrow(() -> walletService.validateWalletOwnership(walletId, userId));
    }

    @Test
    void validateWalletOwnership_Fail_WalletNotFound() {
        Long walletId = 1L;
        UUID userId = UUID.randomUUID();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> walletService.validateWalletOwnership(walletId, userId));
        assertTrue(exception.getMessage().contains("Wallet not found"));
    }

    @Test
    void validateWalletOwnership_Fail_WrongUser() {
        Long walletId = 1L;
        UUID userId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(anotherUserId); // Different user

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        DomainException exception = assertThrows(DomainException.class,
                () -> walletService.validateWalletOwnership(walletId, userId));
        assertTrue(exception.getMessage().contains("Wallet does not belong to user"));
    }
}
