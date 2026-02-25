package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.exception.APIException;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.Tithaal.Wallet.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

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
            throw new APIException(HttpStatus.BAD_REQUEST, "Organization name already exists!");
        }
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Username is already taken!");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Email is already taken!");
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
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Organization not found"));
        return mapToDto(organization);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Organization not found"));
        organization.setStatus(OrganizationStatus.DELETED);
        organizationRepository.save(organization);
    }

    @Override
    public Page<WalletTransactionEntryDto> getOrganizationTransactions(Long orgId, String adminUsername,
            Pageable pageable) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new APIException(HttpStatus.UNAUTHORIZED, "Admin user not found"));

        if (admin.getOrganization() == null || !admin.getOrganization().getId().equals(orgId)) {
            throw new APIException(HttpStatus.FORBIDDEN,
                    "You do not have permission to view transactions for this organization");
        }

        Page<WalletTransaction> transactions = walletTransactionRepository.findByOrganizationId(orgId, pageable);

        return transactions.map(t -> WalletTransactionEntryDto.builder()
                .id(t.getId())
                .description(t.getDescription())
                .amount(t.getAmount())
                .type(t.getType())
                .balanceAfter(t.getBalanceAfter())
                .walletId(t.getWallet().getId())
                .createdAt(t.getCreatedAt())
                .build());
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
