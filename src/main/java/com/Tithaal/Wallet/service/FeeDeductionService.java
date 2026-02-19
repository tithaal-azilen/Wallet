package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeDeductionService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Value("${wallet.monthly.fee.amount}")
    private BigDecimal monthlyFeeAmount;

    public void deductFees() {
        log.info("Starting monthly fee deduction process for date: {}", LocalDate.now());
        int batchSize = 100;

        while (true) {
            Pageable pageable = PageRequest.of(0, batchSize);
            List<Wallet> wallets = walletRepository.findWalletsDueForDeduction(LocalDate.now(), pageable);

            if (wallets.isEmpty()) {
                break;
            }

            for (Wallet wallet : wallets) {
                try {
                    processSingleWallet(wallet.getId());
                } catch (Exception e) {
                    log.error("Failed to process fee deduction for wallet id: {}", wallet.getId(), e);
                }
            }
        }
        log.info("Completed monthly fee deduction process.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleWallet(Long walletId) {
        Wallet wallet = walletRepository.findWithLockingById(walletId).orElse(null);
        if (wallet == null)
            return;

        LocalDate today = LocalDate.now();

        if (wallet.getNextDeductionDate().isAfter(today)) {
            return;
        }
        if (wallet.getLastDeductionAttempt() != null && wallet.getLastDeductionAttempt().equals(today)) {
            return;
        }

        if (wallet.getBalance().compareTo(monthlyFeeAmount) < 0) {
            wallet.setLastDeductionAttempt(today);
            walletRepository.save(wallet);
            log.info("Skipped fee deduction for wallet {} due to insufficient funds (Balance: {})", walletId,
                    wallet.getBalance());
            return;
        }

        wallet.setBalance(wallet.getBalance().subtract(monthlyFeeAmount));

        LocalDate currentDeductionDate = wallet.getNextDeductionDate();
        wallet.setNextDeductionDate(currentDeductionDate.plusMonths(1));
        wallet.setLastDeductionAttempt(today);

        Wallet savedWallet = walletRepository.save(wallet);

        String feeReference = "FEE-" + currentDeductionDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .type("DEBIT")
                .amount(monthlyFeeAmount)
                .description("Monthly Maintenance Fee for "
                        + currentDeductionDate.format(DateTimeFormatter.ofPattern("MMM yyyy")))
                .balanceAfter(savedWallet.getBalance())
                .referenceId(feeReference)
                .createdAt(Instant.now())
                .build();

        walletTransactionRepository.save(transaction);
        log.info("Deducted fee for wallet {}. New Balance: {}", walletId, savedWallet.getBalance());
    }
}
