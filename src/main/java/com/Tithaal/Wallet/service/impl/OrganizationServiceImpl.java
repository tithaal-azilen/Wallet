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
                        throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                                        "Organization name already exists!");
                }
                if (userRepository.existsByUsername(registrationDto.getUsername())) {
                        throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Username is already taken!");
                }
                if (userRepository.existsByEmail(registrationDto.getEmail().trim())) {
                        throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION, "Email is already taken!");
                }

                String orgName = registrationDto.getOrgName().trim();
                String orgCodePrefix = orgName.length() >= 3 ? orgName.substring(0, 3).toUpperCase()
                                : orgName.toUpperCase();
                String orgCode;
                int attempts = 0;
                do {
                        orgCode = orgCodePrefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                        attempts++;
                        if (attempts > 5) {
                                throw new DomainException(ErrorType.INTERNAL_ERROR,
                                                "Failed to generate a unique organization code");
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

        @Override
        public OrganizationDto getOrganization(Long orgId) {
                Organization organization = organizationRepository.findById(orgId)
                                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));
                return mapToDto(organization);
        }

        @Override
        public OrganizationDto getOrganizationDetails(Long orgId, Long adminId) {
                validateAdminOwnership(orgId, adminId);
                return getOrganization(orgId);
        }

        @Override
        @Transactional
        public void deleteOrganization(Long orgId, Long adminId) {
                validateAdminOwnership(orgId, adminId);
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
        }

        @Override
        public PagedResponse<OrganizationTransactionDto> getOrganizationTransactions(Long orgId, Long adminId,
                        int page, int size, String sortBy, String sortDir,
                        com.Tithaal.Wallet.dto.AdminTransactionFilterDto filterDto) {
                validateAdminOwnership(orgId, adminId);
                validateActiveOrganization(orgId);

                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(page, size, sort);

                org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = com.Tithaal.Wallet.repository.WalletTransactionSpecification
                                .getAdminTransactions(orgId, filterDto);
                Page<WalletTransaction> transactions = walletTransactionRepository.findAll(spec, pageable);

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

        @Override
        public java.util.List<OrganizationTransactionDto> getAllOrganizationTransactions(Long orgId, Long adminId,
                        String sortBy, String sortDir, com.Tithaal.Wallet.dto.AdminTransactionFilterDto filterDto) {
                validateAdminOwnership(orgId, adminId);
                validateActiveOrganization(orgId);

                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                org.springframework.data.jpa.domain.Specification<WalletTransaction> spec = com.Tithaal.Wallet.repository.WalletTransactionSpecification
                                .getAdminTransactions(orgId, filterDto);
                java.util.List<WalletTransaction> transactions = walletTransactionRepository.findAll(spec, sort);

                return transactions.stream()
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
        }

        @Override
        @Transactional
        public void updateOrganization(Long orgId, Long adminId,
                        com.Tithaal.Wallet.dto.OrganizationUpdateDto updateDto) {
                validateAdminOwnership(orgId, adminId);
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
        }

        @Override
        public PagedResponse<com.Tithaal.Wallet.dto.UserSummaryDto> getOrganizationUsers(Long orgId, Long adminId,
                        int page, int size, String sortBy, String sortDir) {
                validateAdminOwnership(orgId, adminId);

                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(page, size, sort);

                Page<User> users = userRepository.findByOrganizationId(orgId, pageable);

                java.util.List<com.Tithaal.Wallet.dto.UserSummaryDto> content = users.getContent().stream()
                                .map(u -> com.Tithaal.Wallet.dto.UserSummaryDto.builder()
                                                .id(u.getId())
                                                .username(u.getUsername())
                                                .city(u.getCity())
                                                .phoneNumber(u.getPhoneNumber())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());

                return PagedResponse.<com.Tithaal.Wallet.dto.UserSummaryDto>builder()
                                .content(content)
                                .pageNo(users.getNumber())
                                .pageSize(users.getSize())
                                .totalElements(users.getTotalElements())
                                .totalPages(users.getTotalPages())
                                .last(users.isLast())
                                .build();
        }

        @Override
        @Transactional
        public void updateUserStatus(Long orgId, Long adminId, Long userId, UserStatus status) {
                validateAdminOwnership(orgId, adminId);
                validateActiveOrganization(orgId);

                User targetUser = userRepository.findById(userId)
                                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND,
                                                "User not found with id: " + userId));

                if (targetUser.getOrganization() == null || !targetUser.getOrganization().getId().equals(orgId)) {
                        throw new DomainException(ErrorType.FORBIDDEN,
                                        "User does not belong to your organization");
                }

                if (adminId.equals(userId) && (status == UserStatus.INACTIVE || status == UserStatus.SUSPENDED
                                || status == UserStatus.DELETED)) {
                        throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                                        "Admin cannot deactivate their own account");
                }

                if (targetUser.getStatus() == status) {
                        return;
                }

                if (targetUser.getStatus() == UserStatus.DELETED && status == UserStatus.ACTIVE) {
                        throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                                        "Cannot activate a deleted user");
                }

                targetUser.setStatus(status);
                userRepository.save(targetUser);
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

        private void validateActiveOrganization(Long orgId) {
                Organization organization = organizationRepository.findById(orgId)
                                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));

                if (organization.getStatus() != OrganizationStatus.ACTIVE) {
                        throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                                        "This feature is only available for active organizations. Current status: "
                                                        + organization.getStatus());
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
