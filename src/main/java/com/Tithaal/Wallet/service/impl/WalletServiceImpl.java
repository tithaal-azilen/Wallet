package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.entity.TransactionType;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.WalletService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    @Transactional
    @Retryable(
        retryFor = { CannotAcquireLockException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public String TopUpWallet(Long walletId, CreditRequestDto creditRequestDto, Long userId) {
        validateWalletOwnership(walletId, userId);

        if (creditRequestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(ErrorType.INVALID_INPUT, "Amount must be greater than 0");
        }

        Wallet wallet = walletRepository.findWithLockingById(walletId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Wallet not found with id: " + walletId));

        wallet.credit(creditRequestDto.getAmount());
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .recipientWallet(null)
                .type(TransactionType.CREDIT)
                .amount(creditRequestDto.getAmount())
                .description("Wallet credited using credit card number: " + creditRequestDto.getCreditCardNumber())
                .balanceAfter(savedWallet.getBalance())
                .createdAt(Instant.now())
                .build();

        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        if (savedWallet != null && savedTransaction != null) {
            return "Wallet TopUp successfully ";
        } else {
            return "Wallet TopUp failed!";
        }
    }

    @Override
    @Transactional
    @Retryable(
        retryFor = { CannotAcquireLockException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public String Transfer(DebitRequestDto debitRequestDto, Long userId) {
        validateWalletOwnership(debitRequestDto.getSendingWalletId(), userId);

        if (debitRequestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(ErrorType.INVALID_INPUT, "Amount must be greater than 0");
        }

        if (debitRequestDto.getSendingWalletId().equals(debitRequestDto.getReceivingWalletId())) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Cannot transfer funds to the same wallet");
        }

        Long sendingId = debitRequestDto.getSendingWalletId();
        Long receivingId = debitRequestDto.getReceivingWalletId();

        Wallet senderWallet;
        Wallet recipientWallet;

        if (sendingId < receivingId) {
            senderWallet = walletRepository.findWithLockingById(sendingId)
                    .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Sender wallet not found with id: " + sendingId));
            recipientWallet = walletRepository.findWithLockingById(receivingId)
                    .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Recipient wallet not found with id: " + receivingId));
        } else {
            recipientWallet = walletRepository.findWithLockingById(receivingId)
                    .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Recipient wallet not found with id: " + receivingId));
            senderWallet = walletRepository.findWithLockingById(sendingId)
                    .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Sender wallet not found with id: " + sendingId));
        }

        // Validate sender user status
        if (senderWallet.getUser().getStatus() == com.Tithaal.Wallet.entity.UserStatus.SUSPENDED) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                    "Your account is suspended. Transfers are disabled until your balance is non-negative. You may top-up your wallet.");
        }
        if (senderWallet.getUser().getStatus() != com.Tithaal.Wallet.entity.UserStatus.ACTIVE) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                    "Sender account is not active. Current status: " + senderWallet.getUser().getStatus());
        }

        // Validate recipient user is active
        if (recipientWallet.getUser().getStatus() != com.Tithaal.Wallet.entity.UserStatus.ACTIVE) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                    "Recipient account is not active. Current status: " + recipientWallet.getUser().getStatus());
        }

        if (senderWallet.getBalance().compareTo(debitRequestDto.getAmount()) < 0) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Insufficient balance");
        }

        senderWallet.debit(debitRequestDto.getAmount());
        Wallet savedSenderWallet = walletRepository.save(senderWallet);

        recipientWallet.credit(debitRequestDto.getAmount());
        Wallet savedRecipientWallet = walletRepository.save(recipientWallet);

        WalletTransaction senderTransaction = WalletTransaction.builder()
                .wallet(savedSenderWallet)
                .recipientWallet(savedRecipientWallet)
                .type(TransactionType.DEBIT)
                .amount(debitRequestDto.getAmount())
                .description("Transfer to wallet id: " + debitRequestDto.getReceivingWalletId())
                .balanceAfter(savedSenderWallet.getBalance())
                .createdAt(Instant.now())
                .build();
        walletTransactionRepository.save(senderTransaction);

        WalletTransaction recipientTransaction = WalletTransaction.builder()
                .wallet(savedRecipientWallet)
                .recipientWallet(savedSenderWallet)
                .type(TransactionType.CREDIT)
                .amount(debitRequestDto.getAmount())
                .description("Transfer from wallet id: " + debitRequestDto.getSendingWalletId())
                .balanceAfter(savedRecipientWallet.getBalance())
                .createdAt(Instant.now())
                .build();
        walletTransactionRepository.save(recipientTransaction);

        if (savedSenderWallet != null && savedRecipientWallet != null && senderTransaction != null
                && recipientTransaction != null) {
            return "Transfer Successful! ";
        } else {
            return "Transfer Failed!";
        }
    }

    @Override
    public void validateWalletOwnership(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Wallet not found with id: " + walletId));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new DomainException(ErrorType.FORBIDDEN, "Wallet does not belong to user with id: " + userId);
        }
    }

}