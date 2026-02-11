package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.exception.APIException;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.WalletService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    @Transactional
    public String TopUpWallet(Long walletId, CreditRequestDto creditRequestDto) {
        if (creditRequestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Wallet not found with id: " + walletId));

        wallet.setBalance(wallet.getBalance().add(creditRequestDto.getAmount()));
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .type("CREDIT")
                .amount(creditRequestDto.getAmount())
                .description("Wallet credited using credit card number: " + creditRequestDto.getCreditCardNumber())
                .balanceAfter(savedWallet.getBalance())
                .createdAt(Instant.now())
                .build();

        walletTransactionRepository.save(transaction);

        if (savedWallet != null && transaction != null) {
            return "Wallet TopUp successfully ";
        } else {
            return "Wallet TopUp failed!";
        }
    }

    @Override
    @Transactional
    public String Transfer(DebitRequestDto debitRequestDto) {
        if (debitRequestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }

        if (debitRequestDto.getSendingWalletId().equals(debitRequestDto.getReceivingWalletId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Cannot transfer funds to the same wallet");
        }

        Wallet senderWallet = walletRepository.findById(debitRequestDto.getSendingWalletId())
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND,
                        "Sender wallet not found with id: " + debitRequestDto.getSendingWalletId()));

        Wallet recipientWallet = walletRepository.findById(debitRequestDto.getReceivingWalletId())
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND,
                        "Recipient wallet not found with id: " + debitRequestDto.getReceivingWalletId()));

        if (senderWallet.getBalance().compareTo(debitRequestDto.getAmount()) < 0) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        // Debit sender
        senderWallet.setBalance(senderWallet.getBalance().subtract(debitRequestDto.getAmount()));
        Wallet savedSenderWallet = walletRepository.save(senderWallet);

        WalletTransaction senderTransaction = WalletTransaction.builder()
                .wallet(savedSenderWallet)
                .type("DEBIT")
                .amount(debitRequestDto.getAmount())
                .description("Transfer to wallet id: " + debitRequestDto.getReceivingWalletId())
                .balanceAfter(savedSenderWallet.getBalance())
                .createdAt(Instant.now())
                .build();
        walletTransactionRepository.save(senderTransaction);

        // Credit recipient
        recipientWallet.setBalance(recipientWallet.getBalance().add(debitRequestDto.getAmount()));
        Wallet savedRecipientWallet = walletRepository.save(recipientWallet);

        WalletTransaction recipientTransaction = WalletTransaction.builder()
                .wallet(savedRecipientWallet)
                .type("CREDIT")
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
    @Transactional
    public void validateWalletOwnership(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Wallet not found with id: " + walletId));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Wallet does not belong to user with id: " + userId);
        }
    }
}