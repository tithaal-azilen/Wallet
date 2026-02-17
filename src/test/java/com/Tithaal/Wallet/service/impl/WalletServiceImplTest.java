package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.exception.APIException;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

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
        Long userId = 100L;
        CreditRequestDto creditDto = new CreditRequestDto();
        creditDto.setAmount(BigDecimal.TEN);
        creditDto.setCreditCardNumber("1234567890123456");

        User user = new User();
        user.setId(userId);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(new WalletTransaction());

        String result = walletService.TopUpWallet(walletId, creditDto, userId);

        assertEquals("Wallet TopUp successfully ", result);
        assertEquals(BigDecimal.TEN, wallet.getBalance());
        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void TopUpWallet_Fail_InvalidAmount() {
        Long walletId = 1L;
        Long userId = 100L;
        CreditRequestDto creditDto = new CreditRequestDto();
        creditDto.setAmount(BigDecimal.ZERO);

        // We need wallet mock for verification if it's called before amount check?
        // Logic: validateWalletOwnership -> amount check.
        // So we need to mock wallet finding first.

        User user = new User();
        user.setId(userId);
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUser(user);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        APIException exception = assertThrows(APIException.class,
                () -> walletService.TopUpWallet(walletId, creditDto, userId));
        assertEquals("Amount must be greater than 0", exception.getMessage());
    }

    @Test
    void TopUpWallet_Fail_WalletNotFound() {
        Long walletId = 1L;
        Long userId = 100L;
        CreditRequestDto creditDto = new CreditRequestDto();
        creditDto.setAmount(BigDecimal.TEN);

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class,
                () -> walletService.TopUpWallet(walletId, creditDto, userId));
        assertTrue(exception.getMessage().contains("Wallet not found"));
    }

    // --- Transfer Tests ---

    @Test
    void Transfer_Success() {
        Long userId = 100L;
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(2L);
        debitDto.setAmount(BigDecimal.TEN);

        User user = new User();
        user.setId(userId);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUser(user);
        senderWallet.setBalance(new BigDecimal("100"));

        Wallet recipientWallet = new Wallet();
        recipientWallet.setId(2L);
        recipientWallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(recipientWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(new WalletTransaction());

        String result = walletService.Transfer(debitDto, userId);

        assertEquals("Transfer Successful! ", result);
        assertEquals(new BigDecimal("90"), senderWallet.getBalance());
        assertEquals(BigDecimal.TEN, recipientWallet.getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class)); // Sender and Recipient saved
        verify(walletTransactionRepository, times(2)).save(any(WalletTransaction.class)); // 2 transactions
    }

    @Test
    void Transfer_Fail_InvalidAmount() {
        Long userId = 100L;
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setSendingWalletId(1L);
        debitDto.setAmount(BigDecimal.ZERO);

        User user = new User();
        user.setId(userId);
        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUser(user);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));

        APIException exception = assertThrows(APIException.class, () -> walletService.Transfer(debitDto, userId));
        assertEquals("Amount must be greater than 0", exception.getMessage());
    }

    @Test
    void Transfer_Fail_SameWallet() {
        Long userId = 100L;
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setAmount(BigDecimal.TEN);
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(1L);

        User user = new User();
        user.setId(userId);
        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUser(user);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));

        APIException exception = assertThrows(APIException.class, () -> walletService.Transfer(debitDto, userId));
        assertEquals("Cannot transfer funds to the same wallet", exception.getMessage());
    }

    @Test
    void Transfer_Fail_SenderWalletNotFound() {
        Long userId = 100L;
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setAmount(BigDecimal.TEN);
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(2L);

        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () -> walletService.Transfer(debitDto, userId));
        // Expect "Wallet not found" because validateOwnership calls findById first and
        // throws "Wallet not found"
        // The original test expected "Sender wallet not found", but validateOwnership
        // runs first now.
        assertTrue(exception.getMessage().contains("Wallet not found"));
    }

    @Test
    void Transfer_Fail_RecipientWalletNotFound() {
        Long userId = 100L;
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setAmount(BigDecimal.TEN);
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(2L);

        User user = new User();
        user.setId(userId);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUser(user);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () -> walletService.Transfer(debitDto, userId));
        assertTrue(exception.getMessage().contains("Recipient wallet not found"));
    }

    @Test
    void Transfer_Fail_InsufficientBalance() {
        Long userId = 100L;
        DebitRequestDto debitDto = new DebitRequestDto();
        debitDto.setAmount(new BigDecimal("100"));
        debitDto.setSendingWalletId(1L);
        debitDto.setReceivingWalletId(2L);

        User user = new User();
        user.setId(userId);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(1L);
        senderWallet.setUser(user);
        senderWallet.setBalance(BigDecimal.TEN); // 10 < 100

        Wallet recipientWallet = new Wallet();
        recipientWallet.setId(2L);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(recipientWallet));

        APIException exception = assertThrows(APIException.class, () -> walletService.Transfer(debitDto, userId));
        assertEquals("Insufficient balance", exception.getMessage());
    }

    // --- validateWalletOwnership Tests ---

    @Test
    void validateWalletOwnership_Success() {
        Long walletId = 1L;
        Long userId = 100L;

        User user = new User();
        user.setId(userId);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUser(user);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertDoesNotThrow(() -> walletService.validateWalletOwnership(walletId, userId));
    }

    @Test
    void validateWalletOwnership_Fail_WalletNotFound() {
        Long walletId = 1L;
        Long userId = 100L;

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class,
                () -> walletService.validateWalletOwnership(walletId, userId));
        assertTrue(exception.getMessage().contains("Wallet not found"));
    }

    @Test
    void validateWalletOwnership_Fail_WrongUser() {
        Long walletId = 1L;
        Long userId = 100L;
        Long anotherUserId = 200L;

        User user = new User();
        user.setId(anotherUserId); // Different user

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUser(user);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        APIException exception = assertThrows(APIException.class,
                () -> walletService.validateWalletOwnership(walletId, userId));
        assertTrue(exception.getMessage().contains("Wallet does not belong to user"));
    }

    // --- deductMonthlyFees Tests ---

    @Test
    void deductMonthlyFees_Success() {
        Wallet wallet1 = new Wallet();
        wallet1.setId(1L);
        wallet1.setBalance(new BigDecimal("100.00"));

        Wallet wallet2 = new Wallet();
        wallet2.setId(2L);
        wallet2.setBalance(new BigDecimal("5.00"));

        java.util.List<Wallet> wallets = java.util.Arrays.asList(wallet1, wallet2);

        when(walletRepository.findAll()).thenReturn(wallets);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(new WalletTransaction());

        walletService.deductMonthlyFees();

        assertEquals(new BigDecimal("90.00"), wallet1.getBalance());
        assertEquals(new BigDecimal("-5.00"), wallet2.getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(walletTransactionRepository, times(2)).save(any(WalletTransaction.class));
    }
}
