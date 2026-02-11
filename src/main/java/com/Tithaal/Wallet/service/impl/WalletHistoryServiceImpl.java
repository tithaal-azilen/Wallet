package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.WalletHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletHistoryServiceImpl implements WalletHistoryService {

        private final WalletTransactionRepository walletTransactionRepository;

        @Override
        public List<WalletTransactionEntryDto> getUserHistory(Long userId) {
                List<WalletTransaction> transactions = walletTransactionRepository
                                .findAllByWalletUserIdOrderByCreatedAtDesc(userId);

                return transactions.stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }

        private WalletTransactionEntryDto mapToDto(WalletTransaction transaction) {
                return WalletTransactionEntryDto.builder()
                                .id(transaction.getId())
                                .type(transaction.getType())
                                .amount(transaction.getAmount())
                                .description(transaction.getDescription())
                                .balanceAfter(transaction.getBalanceAfter())
                                .createdAt(transaction.getCreatedAt())
                                .walletId(transaction.getWallet().getId())
                                .build();
        }

        @Override
        public List<WalletTransactionEntryDto> getWalletHistory(Long walletId) {
                List<WalletTransaction> transactions = walletTransactionRepository
                                .findAllByWalletIdOrderByCreatedAtDesc(walletId);

                return transactions.stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }
}
