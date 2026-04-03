package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.OrganizationUpdateDto;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.service.OrganizationService;
import com.Tithaal.Wallet.service.validator.OrganizationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationValidator validator;

    @Override
    public OrganizationDto getOrganization(Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));
        return mapToDto(organization);
    }

    @Override
    public OrganizationDto getOrganizationDetails(Long orgId, UUID adminId) {
        validator.validateAdminOwnership(orgId, adminId);
        return getOrganization(orgId);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long orgId, UUID adminId) {
        validator.validateAdminOwnership(orgId, adminId);
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));

        // NOTE: active-user check is removed — user status is now owned by the Auth Service.
        organization.setStatus(OrganizationStatus.DELETED);
        organizationRepository.save(organization);
        log.info("Admin {} deleted organization {}", adminId, orgId);
    }

    @Override
    @Transactional
    public void updateOrganization(Long orgId, UUID adminId, OrganizationUpdateDto updateDto) {
        validator.validateAdminOwnership(orgId, adminId);
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));

        if (updateDto.getName() != null && !updateDto.getName().trim().isEmpty()) {
            String newName = updateDto.getName().trim();
            if (!newName.equalsIgnoreCase(organization.getName())
                    && organizationRepository.existsByName(newName)) {
                throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                        "Organization name already exists!");
            }
            organization.setName(newName);
        }
        organizationRepository.save(organization);
        log.info("Admin {} updated organization {}", adminId, orgId);
    }

    private OrganizationDto mapToDto(Organization organization) {
        return OrganizationDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .orgCode(organization.getOrgCode())
                .status(organization.getStatus())
                .createdAt(organization.getCreatedAt())
                .build();
    }
}
