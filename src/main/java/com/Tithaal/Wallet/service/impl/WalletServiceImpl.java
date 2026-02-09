package com.Tithaal.Wallet.service.impl;

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
    public Wallet creditWallet(Long walletId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Wallet not found with id: " + walletId));

        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .type("CREDIT")
                .amount(amount)
                .description("Wallet credit")
                .balanceAfter(savedWallet.getBalance())
                .createdAt(Instant.now())
                .build();

        walletTransactionRepository.save(transaction);

        return savedWallet;
    }

    @Override
    @Transactional
    public Wallet transferFunds(Long senderWalletId, Long recipientWalletId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }

        if (senderWalletId.equals(recipientWalletId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Cannot transfer funds to the same wallet");
        }

        Wallet senderWallet = walletRepository.findById(senderWalletId)
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND,
                        "Sender wallet not found with id: " + senderWalletId));

        Wallet recipientWallet = walletRepository.findById(recipientWalletId)
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND,
                        "Recipient wallet not found with id: " + recipientWalletId));

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        // Debit sender
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        Wallet savedSenderWallet = walletRepository.save(senderWallet);

        WalletTransaction senderTransaction = WalletTransaction.builder()
                .wallet(savedSenderWallet)
                .type("DEBIT")
                .amount(amount)
                .description("Transfer to wallet id: " + recipientWalletId)
                .balanceAfter(savedSenderWallet.getBalance())
                .createdAt(Instant.now())
                .build();
        walletTransactionRepository.save(senderTransaction);

        // Credit recipient
        recipientWallet.setBalance(recipientWallet.getBalance().add(amount));
        Wallet savedRecipientWallet = walletRepository.save(recipientWallet);

        WalletTransaction recipientTransaction = WalletTransaction.builder()
                .wallet(savedRecipientWallet)
                .type("CREDIT")
                .amount(amount)
                .description("Transfer from wallet id: " + senderWalletId)
                .balanceAfter(savedRecipientWallet.getBalance())
                .createdAt(Instant.now())
                .build();
        walletTransactionRepository.save(recipientTransaction);

        return savedSenderWallet;
    }
}
