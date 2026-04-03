package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.OrganizationUpdateDto;

import java.util.UUID;

public interface OrganizationService {
    OrganizationDto getOrganization(Long orgId);

    void deleteOrganization(Long orgId, UUID adminId);

    OrganizationDto getOrganizationDetails(Long orgId, UUID adminId);

    void updateOrganization(Long orgId, UUID adminId, OrganizationUpdateDto updateDto);
}
