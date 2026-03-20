package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.entity.Role;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.Tithaal.Wallet.service.OrganizationRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationRegistrationServiceImpl implements OrganizationRegistrationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
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
        if (userRepository.existsByEmail(registrationDto.getEmail().trim())) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Email is already taken!");
        }

        String orgName = registrationDto.getOrgName().trim();
        String orgCodePrefix = orgName.length() >= 3 ? orgName.substring(0, 3).toUpperCase() : orgName.toUpperCase();
        String orgCode;
        int attempts = 0;
        do {
            orgCode = orgCodePrefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            attempts++;
            if (attempts > 5) {
                throw new DomainException(ErrorType.INTERNAL_ERROR, "Failed to generate a unique organization code");
            }
        } while (organizationRepository.existsByOrgCode(orgCode));

        Organization organization = Organization.builder()
                .name(orgName)
                .orgCode(orgCode)
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        organization = organizationRepository.save(organization);

        User adminUser = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail().trim())
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
}
