package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;

import com.Tithaal.Wallet.dto.TransactionFilterDto;

public interface PlatformTransactionService {

    PagedResponse<OrganizationTransactionDto> getAllTransactions(TransactionFilterDto filter, int page, int size,
            String sortBy, String sortDir);
}
