package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.AdminTransactionFilterDto;
import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.entity.WalletTransaction;
import com.Tithaal.Wallet.exception.APIException;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.repository.WalletTransactionSpecification;
import com.Tithaal.Wallet.security.SecurityUtils;
import com.Tithaal.Wallet.service.OrganizationTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationTransactionServiceImpl implements OrganizationTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public PagedResponse<OrganizationTransactionDto> getPaginatedTransactions(java.util.UUID orgId, java.util.UUID adminId, int page, int size, String sortBy, String sortDir, AdminTransactionFilterDto filterDto) {
        validateTenantAccess(orgId);

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
    public List<OrganizationTransactionDto> getAllTransactionsList(java.util.UUID orgId, java.util.UUID adminId, String sortBy, String sortDir, AdminTransactionFilterDto filterDto) {
        validateTenantAccess(orgId);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = WalletTransactionSpecification.getAdminTransactions(orgId, filterDto);
        List<WalletTransaction> transactions = walletTransactionRepository.findAll(spec, sort);

        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void validateTenantAccess(UUID orgId) {
        String currentTenantId = SecurityUtils.getCurrentTenantId();
        String currentStatus = SecurityUtils.getCurrentStatus();

        if (currentTenantId == null || !UUID.fromString(currentTenantId).equals(orgId)) {
            log.error("Access denied: Admin {} attempted to access organization {}", SecurityUtils.getCurrentUserId(), orgId);
            throw new APIException(HttpStatus.FORBIDDEN, "Access denied: You do not have ownership of this organization");
        }

        if (currentStatus == null || !currentStatus.equalsIgnoreCase("ACTIVE")) {
            log.error("Access denied: Organization {} is not ACTIVE (Status: {})", orgId, currentStatus);
            throw new APIException(HttpStatus.FORBIDDEN, "Access denied: Organization is not active");
        }

        if (!SecurityUtils.hasRole("ORG_ADMIN")) {
            log.error("Access denied: Admin {} does not have ORG_ADMIN role", SecurityUtils.getCurrentUserId());
            throw new APIException(HttpStatus.FORBIDDEN, "Access denied: Required role ORG_ADMIN not found");
        }
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
