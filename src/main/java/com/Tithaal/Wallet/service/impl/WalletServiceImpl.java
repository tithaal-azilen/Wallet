package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.dto.OrderTransactionRequestDto;
import com.Tithaal.Wallet.dto.UserResponseDTO;
import com.Tithaal.Wallet.client.AuthServiceClient;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.entity.TransactionType;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.event.WalletCreatedEvent;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public String addWallet(UUID userId, UUID tenantId) {
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .tenantId(tenantId)
                .balance(java.math.BigDecimal.ZERO)
                .createdAt(Instant.now())
                .nextDeductionDate(calculateNextDeductionDate())
                .build();

        Wallet saved = walletRepository.save(wallet);

        // Publish event for notification — listener will fetch user details
        eventPublisher.publishEvent(new WalletCreatedEvent(this, userId, saved.getId()));

        log.info("Created wallet {} for user {} tenant {}", saved.getId(), userId, tenantId);
        return "Wallet created successfully with id: " + saved.getId();
    }

    private java.time.LocalDate calculateNextDeductionDate() {
        java.time.LocalDate today = java.time.LocalDate.now();
        int day = today.getDayOfMonth();
        if (day <= 7)
            return today.withDayOfMonth(1).plusMonths(1);
        if (day <= 14)
            return today.withDayOfMonth(8).plusMonths(1);
        if (day <= 21)
            return today.withDayOfMonth(15).plusMonths(1);
        return today.withDayOfMonth(22).plusMonths(1);
    }

    @Override
    @Transactional
    @Retryable(retryFor = {
            CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    public String topUpWallet(Long walletId, CreditRequestDto creditRequestDto, UUID userId) {
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
                .userId(savedWallet.getUserId())
                .tenantId(savedWallet.getTenantId())
                .createdAt(Instant.now())
                .build();

        walletTransactionRepository.save(transaction);
        log.info("Wallet {} successfully topped up with amount {}", walletId, creditRequestDto.getAmount());
        return "Wallet TopUp successfully";
    }

    @Override
    @Transactional
    @Retryable(
        retryFor = { CannotAcquireLockException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public String transfer(DebitRequestDto debitRequestDto, UUID userId, String senderStatus) {
        validateWalletOwnership(debitRequestDto.getSendingWalletId(), userId);

        if (debitRequestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(ErrorType.INVALID_INPUT, "Amount must be greater than 0");
        }

        if (debitRequestDto.getSendingWalletId().equals(debitRequestDto.getReceivingWalletId())) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Cannot transfer funds to the same wallet");
        }

        // Validate sender status from JWT claim — no DB join needed
        if ("SUSPENDED".equals(senderStatus)) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                    "Your account is suspended. Transfers are disabled until your balance is non-negative. You may top-up your wallet.");
        }
        if (!"ACTIVE".equals(senderStatus)) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                    "Sender account is not active. Current status: " + senderStatus);
        }

        Long sendingId = debitRequestDto.getSendingWalletId();
        Long receivingId = debitRequestDto.getReceivingWalletId();

        // Lock in consistent order to prevent deadlocks
        Wallet senderWallet;
        Wallet recipientWallet;
        if (sendingId < receivingId) {
            senderWallet = walletRepository.findWithLockingById(sendingId)
                    .orElseThrow(
                            () -> new DomainException(ErrorType.NOT_FOUND, "Sender wallet not found: " + sendingId));
            recipientWallet = walletRepository.findWithLockingById(receivingId)
                    .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND,
                            "Recipient wallet not found: " + receivingId));
        } else {
            recipientWallet = walletRepository.findWithLockingById(receivingId)
                    .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND,
                            "Recipient wallet not found: " + receivingId));
            senderWallet = walletRepository.findWithLockingById(sendingId)
                    .orElseThrow(
                            () -> new DomainException(ErrorType.NOT_FOUND, "Sender wallet not found: " + sendingId));
        }

        if (senderWallet.getBalance().compareTo(debitRequestDto.getAmount()) < 0) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Insufficient balance");
        }

        senderWallet.debit(debitRequestDto.getAmount());
        walletRepository.save(senderWallet);

        recipientWallet.credit(debitRequestDto.getAmount());
        walletRepository.save(recipientWallet);

        String description = debitRequestDto.getDescription() != null ? debitRequestDto.getDescription() 
                : "Transfer to wallet id: " + receivingId;

        walletTransactionRepository.save(WalletTransaction.builder()
                .wallet(senderWallet)
                .recipientWallet(recipientWallet)
                .type(TransactionType.DEBIT)
                .amount(debitRequestDto.getAmount())
                .description(description)
                .balanceAfter(senderWallet.getBalance())
                .userId(senderWallet.getUserId())
                .tenantId(senderWallet.getTenantId())
                .createdAt(Instant.now())
                .build());

        String recipientDescription = debitRequestDto.getDescription() != null ? debitRequestDto.getDescription() 
                : "Transfer from wallet id: " + sendingId;

        walletTransactionRepository.save(WalletTransaction.builder()
                .wallet(recipientWallet)
                .recipientWallet(senderWallet)
                .type(TransactionType.CREDIT)
                .amount(debitRequestDto.getAmount())
                .description(recipientDescription)
                .balanceAfter(recipientWallet.getBalance())
                .userId(recipientWallet.getUserId())
                .tenantId(recipientWallet.getTenantId())
                .createdAt(Instant.now())
                .build());

        log.info("Successfully transferred {} from Wallet {} to Wallet {}", debitRequestDto.getAmount(), sendingId,
                receivingId);
        return "Transfer Successful!";
    }

    @Override
    @Transactional
    public String orderTransaction(OrderTransactionRequestDto request) {
        log.info("Processing orderTransaction from {} to {} for amount {}", 
            request.getSenderId(), request.getRecipientId(), request.getAmount());

        Wallet senderWallet = walletRepository.findFirstByUserId(request.getSenderId())
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, 
                    "No wallet found for sender: " + request.getSenderId()));

        Wallet recipientWallet = walletRepository.findFirstByUserId(request.getRecipientId())
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, 
                    "No wallet found for recipient: " + request.getRecipientId()));

        // Fetch sender status from Auth Service for enforcement
        UserResponseDTO sender = authServiceClient.getUserById(request.getSenderId());
        String status = (sender != null) ? sender.getStatus() : "SUSPENDED"; // Fail-safe to suspended if user not found

        DebitRequestDto debitRequestDto = new DebitRequestDto();
        debitRequestDto.setSendingWalletId(senderWallet.getId());
        debitRequestDto.setReceivingWalletId(recipientWallet.getId());
        debitRequestDto.setAmount(request.getAmount());
        debitRequestDto.setDescription(request.getDescription());

        // Reuse existing transfer logic
        return this.transfer(debitRequestDto, request.getSenderId(), status);
    }

    @Override
    public void validateWalletOwnership(Long walletId, UUID userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Wallet not found with id: " + walletId));

        if (!wallet.getUserId().equals(userId)) {
            throw new DomainException(ErrorType.FORBIDDEN, "Wallet does not belong to the authenticated user");
        }
    }

    @Recover
    public String recoverFromTopUpLockingFailure(CannotAcquireLockException e, Long walletId,
            CreditRequestDto creditRequestDto, UUID userId) {
        log.error("TopUp Failed: lock exhausted for Wallet ID {}", walletId);
        throw new DomainException(ErrorType.INTERNAL_ERROR, "System is exceptionally busy. Please try again.");
    }

    @Recover
    public String recoverFromTransferLockingFailure(CannotAcquireLockException e,
            DebitRequestDto debitRequestDto, UUID userId, String senderStatus) {
        log.error("Transfer Failed: lock exhausted for Wallet {} → Wallet {}",
                debitRequestDto.getSendingWalletId(), debitRequestDto.getReceivingWalletId());
        throw new DomainException(ErrorType.INTERNAL_ERROR, "System is exceptionally busy. Please try again.");
    }
}