package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.AdminTransactionFilterDto;
import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.repository.WalletTransactionSpecification;
import com.Tithaal.Wallet.service.OrganizationTransactionService;
import com.Tithaal.Wallet.service.validator.OrganizationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationTransactionServiceImpl implements OrganizationTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final OrganizationValidator validator;

    @Override
    public PagedResponse<OrganizationTransactionDto> getPaginatedTransactions(Long orgId, java.util.UUID adminId, int page, int size, String sortBy, String sortDir, AdminTransactionFilterDto filterDto) {
        validator.validateAdminOwnership(orgId, adminId);

        validator.validateActiveOrganization(orgId);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = WalletTransactionSpecification.getAdminTransactions(orgId, filterDto);
        Page<WalletTransaction> transactions = walletTransactionRepository.findAll(spec, pageable);

        List<OrganizationTransactionDto> content = transactions.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PagedResponse.<OrganizationTransactionDto>builder()
                .content(content)
                .pageNo(transactions.getNumber())
                .pageSize(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .last(transactions.isLast())
                .build();
    }

    @Override
    public List<OrganizationTransactionDto> getAllTransactionsList(Long orgId, java.util.UUID adminId, String sortBy, String sortDir, AdminTransactionFilterDto filterDto) {
        validator.validateAdminOwnership(orgId, adminId);

        validator.validateActiveOrganization(orgId);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = WalletTransactionSpecification.getAdminTransactions(orgId, filterDto);
        List<WalletTransaction> transactions = walletTransactionRepository.findAll(spec, sort);

        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
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
}
