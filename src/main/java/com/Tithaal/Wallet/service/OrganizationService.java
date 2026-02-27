package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationService {
    String registerOrganizationAndAdmin(OrganizationRegistrationDto registrationDto);

    OrganizationDto getOrganization(Long orgId);

    void deleteOrganization(Long orgId, Long adminId);

    Page<OrganizationTransactionDto> getOrganizationTransactions(Long orgId, Long adminId, Pageable pageable);
}
