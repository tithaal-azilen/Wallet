package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.entity.OrganizationStatus;

import com.Tithaal.Wallet.dto.OrganizationFilterDto;

public interface PlatformOrganizationService {

    PagedResponse<OrganizationDto> getAllOrganizations(OrganizationFilterDto filter, int page, int size, String sortBy,
            String sortDir);

    void updateOrganizationStatus(Long orgId, OrganizationStatus status);
}
