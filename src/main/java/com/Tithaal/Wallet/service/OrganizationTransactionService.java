package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.AdminTransactionFilterDto;
import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;

import java.util.List;
import java.util.UUID;

public interface OrganizationTransactionService {
    PagedResponse<OrganizationTransactionDto> getPaginatedTransactions(UUID orgId, java.util.UUID adminId, int page, int size, String sortBy, String sortDir, AdminTransactionFilterDto filterDto);
    
    List<OrganizationTransactionDto> getAllTransactionsList(UUID orgId, java.util.UUID adminId, String sortBy, String sortDir, AdminTransactionFilterDto filterDto);

}
