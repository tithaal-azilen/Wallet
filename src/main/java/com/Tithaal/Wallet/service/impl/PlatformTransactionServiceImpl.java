package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.TransactionFilterDto;
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
        public PagedResponse<OrganizationTransactionDto> getAllTransactions(TransactionFilterDto filter, int page,
                        int size, String sortBy,
                        String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<WalletTransaction> transactionsPage = walletTransactionRepository.findAllPlatformWithFilters(
                                filter.getTransactionType(), filter.getStartDate(), filter.getEndDate(),
                                filter.getUserId(), filter.getWalletId(), filter.getOrganizationId(), pageable);

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
                                .userId(t.getWallet().getUser().getId())
                                .username(t.getWallet().getUser().getUsername())
                                .createdAt(t.getCreatedAt())
                                .build();
        }
}
