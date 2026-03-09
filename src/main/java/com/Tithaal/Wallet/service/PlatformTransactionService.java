package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;

public interface PlatformTransactionService {

    PagedResponse<OrganizationTransactionDto> getAllTransactions(com.Tithaal.Wallet.dto.SuperAdminTransactionFilterDto filter, int page, int size,
            String sortBy, String sortDir);
}
