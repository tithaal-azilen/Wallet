package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationDto;

public interface OrganizationService {
        OrganizationDto getOrganization(Long orgId);

        void deleteOrganization(Long orgId, Long adminId);

        OrganizationDto getOrganizationDetails(Long orgId, Long adminId);

        void updateOrganization(Long orgId, Long adminId, com.Tithaal.Wallet.dto.OrganizationUpdateDto updateDto);
}
