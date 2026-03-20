package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.AdminTransactionFilterDto;
import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;

import java.util.List;

public interface OrganizationTransactionService {
    PagedResponse<OrganizationTransactionDto> getPaginatedTransactions(Long orgId, Long adminId, int page, int size, String sortBy, String sortDir, AdminTransactionFilterDto filterDto);
    
    List<OrganizationTransactionDto> getAllTransactionsList(Long orgId, Long adminId, String sortBy, String sortDir, AdminTransactionFilterDto filterDto);
}
