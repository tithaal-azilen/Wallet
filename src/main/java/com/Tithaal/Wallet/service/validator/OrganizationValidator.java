package com.Tithaal.Wallet.service.validator;

import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.entity.User;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationValidator {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public void validateAdminOwnership(Long orgId, Long adminId) {
        log.info("Validating admin ownership for orgId: " + orgId + " and adminId: " + adminId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new DomainException(ErrorType.UNAUTHORIZED, "Admin user not found"));

        if (admin.getOrganization() == null || !admin.getOrganization().getId().equals(orgId)) {
            throw new DomainException(ErrorType.FORBIDDEN,
                    "You do not have permission to access this organization");
        }
    }

    public void validateActiveOrganization(Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException(ErrorType.NOT_FOUND, "Organization not found"));

        if (organization.getStatus() != OrganizationStatus.ACTIVE) {
            throw new DomainException(ErrorType.BUSINESS_RULE_VIOLATION,
                    "This feature is only available for active organizations. Current status: "
                            + organization.getStatus());
        }
    }
}
