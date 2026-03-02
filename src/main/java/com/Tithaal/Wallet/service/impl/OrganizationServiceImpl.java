package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String registerOrganizationAndAdmin(OrganizationRegistrationDto registrationDto) {
        if (organizationRepository.existsByName(registrationDto.getOrgName())) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Organization name already exists!");
        }
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Username is already taken!");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Email is already taken!");
        }

        String orgCode = registrationDto.getOrgName().substring(0, 3).toUpperCase()
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Organization organization = Organization.builder()
                .name(registrationDto.getOrgName())
                .orgCode(orgCode)
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        organization = organizationRepository.save(organization);

        User adminUser = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .city(registrationDto.getCity())
                .phoneNumber(registrationDto.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(registrationDto.getPassword()))
                .role(Role.ROLE_ORG_ADMIN)
                .status(UserStatus.ACTIVE)
                .organization(organization)
                .createdAt(Instant.now())
                .build();

        userRepository.save(adminUser);

        return orgCode;
    }

    @Override
    public OrganizationDto getOrganization(Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));
        return mapToDto(organization);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long orgId, Long adminId) {
        validateAdminOwnership(orgId, adminId);
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));
        organization.setStatus(OrganizationStatus.DELETED);
        organizationRepository.save(organization);
    }

    @Override
    public PagedResponse<OrganizationTransactionDto> getOrganizationTransactions(Long orgId, Long adminId,
            int page, int size, String sortBy, String sortDir) {
        validateAdminOwnership(orgId, adminId);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<WalletTransaction> transactions = walletTransactionRepository.findByOrganizationId(orgId, pageable);

        java.util.List<OrganizationTransactionDto> content = transactions.getContent().stream()
                .map(t -> OrganizationTransactionDto.builder()
                        .id(t.getId())
                        .description(t.getDescription())
                        .amount(t.getAmount())
                        .type(t.getType())
                        .balanceAfter(t.getBalanceAfter())
                        .walletId(t.getWallet().getId())
                        .userId(t.getWallet().getUser().getId())
                        .username(t.getWallet().getUser().getUsername())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return PagedResponse.<OrganizationTransactionDto>builder()
                .content(content)
                .pageNo(transactions.getNumber())
                .pageSize(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .last(transactions.isLast())
                .build();
    }

    private void validateAdminOwnership(Long orgId, Long adminId) {
        log.info("Validating admin ownership for orgId: " + orgId + " and adminId: " + adminId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new DomainException(ErrorType.UNAUTHORIZED, "Admin user not found"));

        if (admin.getOrganization() == null || !admin.getOrganization().getId().equals(orgId)) {
            throw new DomainException(ErrorType.FORBIDDEN,
                    "You do not have permission to access this organization");
        }
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
