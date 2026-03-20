package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

        private final OrganizationRepository organizationRepository;
        private final UserRepository userRepository;
        private final com.Tithaal.Wallet.service.validator.OrganizationValidator validator;



        @Override
        public OrganizationDto getOrganization(Long orgId) {
                Organization organization = organizationRepository.findById(orgId)
                                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));
                return mapToDto(organization);
        }

        @Override
        public OrganizationDto getOrganizationDetails(Long orgId, Long adminId) {
                validator.validateAdminOwnership(orgId, adminId);
                return getOrganization(orgId);
        }

        @Override
        @Transactional
        public void deleteOrganization(Long orgId, Long adminId) {
                validator.validateAdminOwnership(orgId, adminId);
                Organization organization = organizationRepository.findById(orgId)
                                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));

                boolean hasActiveUsers = userRepository.findAll().stream()
                                .filter(u -> u.getOrganization() != null && u.getOrganization().getId().equals(orgId))
                                .anyMatch(u -> u.getStatus() == UserStatus.ACTIVE);

                if (hasActiveUsers) {
                        throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                                        "Cannot delete organization with active users");
                }

                organization.setStatus(OrganizationStatus.DELETED);
                organizationRepository.save(organization);
                log.info("Org Admin {} deleted organization {}", adminId, orgId);
        }

        @Override
        @Transactional
        public void updateOrganization(Long orgId, Long adminId,
                        com.Tithaal.Wallet.dto.OrganizationUpdateDto updateDto) {
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
                log.info("Org Admin {} updated organization {} details", adminId, orgId);
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
