package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.PlatformTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformTransactionServiceImpl implements PlatformTransactionService {

        private final WalletTransactionRepository walletTransactionRepository;

        @Override
        public PagedResponse<OrganizationTransactionDto> getAllTransactions(com.Tithaal.Wallet.dto.SuperAdminTransactionFilterDto filter, int page,
                        int size, String sortBy,
                        String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                
                org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = com.Tithaal.Wallet.repository.WalletTransactionSpecification.getSuperAdminTransactions(filter);
                Page<WalletTransaction> transactionsPage = walletTransactionRepository.findAll(spec, pageable);

                List<OrganizationTransactionDto> content = transactionsPage.getContent().stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());

                return PagedResponse.<OrganizationTransactionDto>builder()
                                .content(content)
                                .pageNo(transactionsPage.getNumber())
                                .pageSize(transactionsPage.getSize())
                                .totalElements(transactionsPage.getTotalElements())
                                .totalPages(transactionsPage.getTotalPages())
                                .last(transactionsPage.isLast())
                                .build();
        }

        private OrganizationTransactionDto mapToDto(WalletTransaction t) {
                return OrganizationTransactionDto.builder()
                                .id(t.getId())
                                .description(t.getDescription())
                                .amount(t.getAmount())
                                .type(t.getType())
                                .balanceAfter(t.getBalanceAfter())
                                .walletId(t.getWallet().getId())
                                .userId(t.getWallet().getUserId())
                                .createdAt(t.getCreatedAt())
                                .build();
        }

        @Override
        public java.util.List<OrganizationTransactionDto> getAllPlatformTransactions(com.Tithaal.Wallet.dto.SuperAdminTransactionFilterDto filter, String sortBy, String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                
                org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = com.Tithaal.Wallet.repository.WalletTransactionSpecification.getSuperAdminTransactions(filter);
                java.util.List<WalletTransaction> transactions = walletTransactionRepository.findAll(spec, sort);

                return transactions.stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }
}
