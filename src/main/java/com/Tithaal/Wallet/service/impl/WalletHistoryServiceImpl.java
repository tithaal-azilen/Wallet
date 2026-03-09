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
        public com.Tithaal.Wallet.dto.PagedResponse<WalletTransactionEntryDto> getUserHistory(Long userId, com.Tithaal.Wallet.dto.UserTransactionFilterDto filterDto, int page, int size, String sortBy, String sortDir) {
                
                org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? 
                        org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
                
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

                org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = com.Tithaal.Wallet.repository.WalletTransactionSpecification.getUserTransactions(userId, filterDto);
                
                org.springframework.data.domain.Page<WalletTransaction> transactionsPage = walletTransactionRepository.findAll(spec, pageable);

                List<WalletTransactionEntryDto> content = transactionsPage.getContent().stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
                
                return new com.Tithaal.Wallet.dto.PagedResponse<>(
                        content,
                        transactionsPage.getNumber(),
                        transactionsPage.getSize(),
                        transactionsPage.getTotalElements(),
                        transactionsPage.getTotalPages(),
                        transactionsPage.isLast()
                );
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
