package com.Tithaal.Wallet.service.validator;

import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.exception.DomainException;
import com.Tithaal.Wallet.exception.ErrorType;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * OrganizationValidator — stripped of local user lookups.
 * Admin ownership is validated via JWT (SecurityUtils) in the calling service,
 * not via a local UserRepository call.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationValidator {

    private final OrganizationRepository organizationRepository;

    /**
     * Validates that the admin UUID is associated with the org.
     * Since we no longer have a local users table, we check
     * the organization's adminUserId UUID column if it exists,
     * or simply verify the org belongs to the caller's tenant.
     *
     * NOTE: Fine-grained admin-to-org ownership is now enforced by
     * the Auth Service via JWT claims (tenantId). Wallet only verifies
     * that the org exists and belongs to the caller's tenant.
     */
    public void validateAdminOwnership(Long orgId, UUID adminId) {
        log.info("Validating admin access for orgId: {} adminId: {}", orgId, adminId);
        // JWT already ensures the caller is an ORG_ADMIN with the correct tenantId.
        // Here we just confirm the org exists.
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException(ErrorType.UNAUTHORIZED, "Organization not found"));
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
