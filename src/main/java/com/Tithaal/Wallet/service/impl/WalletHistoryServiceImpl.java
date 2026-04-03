package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserTransactionFilterDto;
import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.repository.WalletTransactionSpecification;
import com.Tithaal.Wallet.service.WalletHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletHistoryServiceImpl implements WalletHistoryService {

    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public PagedResponse<WalletTransactionEntryDto> getUserHistory(UUID userId, UserTransactionFilterDto filterDto,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<WalletTransaction> spec = WalletTransactionSpecification.getUserTransactions(userId, filterDto);
        Page<WalletTransaction> transactionsPage = walletTransactionRepository.findAll(spec, pageable);

        List<WalletTransactionEntryDto> content = transactionsPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                transactionsPage.getNumber(),
                transactionsPage.getSize(),
                transactionsPage.getTotalElements(),
                transactionsPage.getTotalPages(),
                transactionsPage.isLast());
    }

    @Override
    public List<WalletTransactionEntryDto> getWalletHistory(Long walletId) {
        return walletTransactionRepository.findAllByWalletIdOrderByCreatedAtDesc(walletId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<WalletTransactionEntryDto> getAllUserHistory(UUID userId, UserTransactionFilterDto filterDto,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Specification<WalletTransaction> spec = WalletTransactionSpecification.getUserTransactions(userId, filterDto);
        return walletTransactionRepository.findAll(spec, sort)
                .stream().map(this::mapToDto).collect(Collectors.toList());
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
}
