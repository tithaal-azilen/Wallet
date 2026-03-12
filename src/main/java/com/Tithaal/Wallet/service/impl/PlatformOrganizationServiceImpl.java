package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.OrganizationFilterDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.service.PlatformOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformOrganizationServiceImpl implements PlatformOrganizationService {

        private final OrganizationRepository organizationRepository;

        @Override
        public PagedResponse<OrganizationDto> getAllOrganizations(OrganizationFilterDto filter, int page, int size,
                        String sortBy, String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<Organization> orgsPage = organizationRepository.findAllWithFilters(
                                filter.getName() != null ? filter.getName().trim() : null,
                                filter.getOrgCode() != null ? filter.getOrgCode().trim() : null,
                                filter.getStatus(), filter.getStartDate(),
                                filter.getEndDate(), pageable);

                List<OrganizationDto> content = orgsPage.getContent().stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());

                return new PagedResponse<>(
                                content,
                                orgsPage.getNumber(),
                                orgsPage.getSize(),
                                orgsPage.getTotalElements(),
                                orgsPage.getTotalPages(),
                                orgsPage.isLast());
        }

        @Override
        @Transactional
        public void updateOrganizationStatus(Long orgId, OrganizationStatus status) {
                Organization organization = organizationRepository.findById(orgId)
                                .orElseThrow(
                                                () -> new DomainException(ErrorType.NOT_FOUND,
                                                                "Organization not found with id: " + orgId));

                if (organization.getStatus() == status) {
                        return;
                }

                if (organization.getStatus() == OrganizationStatus.DELETED && status == OrganizationStatus.ACTIVE) {
                        throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                                        "Cannot activate a deleted organization");
                }

                organization.setStatus(status);
                organizationRepository.save(organization);
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
