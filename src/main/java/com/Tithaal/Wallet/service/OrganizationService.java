package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;

public interface OrganizationService {
    String registerOrganizationAndAdmin(OrganizationRegistrationDto registrationDto);

    OrganizationDto getOrganization(Long orgId);

    void deleteOrganization(Long orgId, Long adminId);

    PagedResponse<OrganizationTransactionDto> getOrganizationTransactions(Long orgId, Long adminId, int page, int size,
            String sortBy, String sortDir);
}
