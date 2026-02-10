package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.LedgerEntryDto;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

        private final WalletTransactionRepository walletTransactionRepository;

        @Override
        public List<LedgerEntryDto> getUserLedger(Long userId) {
                List<WalletTransaction> transactions = walletTransactionRepository
                                .findAllByWalletUserIdOrderByCreatedAtDesc(userId);

                return transactions.stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }

        private LedgerEntryDto mapToDto(WalletTransaction transaction) {
                return LedgerEntryDto.builder()
                                .id(transaction.getId())
                                .type(transaction.getType())
                                .amount(transaction.getAmount())
                                .description(transaction.getDescription())
                                .balanceAfter(transaction.getBalanceAfter())
                                .createdAt(transaction.getCreatedAt())
                                .walletId(transaction.getWallet().getId())
                                .build();
        }
}
