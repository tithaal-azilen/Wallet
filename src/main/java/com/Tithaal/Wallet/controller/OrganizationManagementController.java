package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.OrganizationUpdateDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserSummaryDto;
import com.Tithaal.Wallet.entity.UserStatus;
import com.Tithaal.Wallet.security.CustomUserDetails;
import com.Tithaal.Wallet.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/manage")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "Endpoints for managing organizations and viewing associated users")
public class OrganizationManagementController {

    private final OrganizationService organizationService;
    private final com.Tithaal.Wallet.service.OrganizationUserService organizationUserService;

    @Operation(summary = "Get Organization Details (Admin only)")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @GetMapping("/{orgId}")
    public ResponseEntity<com.Tithaal.Wallet.dto.OrganizationDto> getOrganizationDetails(
            @PathVariable Long orgId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        com.Tithaal.Wallet.dto.OrganizationDto orgDetails = organizationService.getOrganizationDetails(orgId, adminId);
        return ResponseEntity.ok(orgDetails);
    }

    @Operation(summary = "Update organization details (Admin only)")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PutMapping("/{orgId}")
    public ResponseEntity<String> updateOrganization(
            @PathVariable Long orgId,
            @Valid @RequestBody OrganizationUpdateDto updateDto,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        organizationService.updateOrganization(orgId, adminId, updateDto);
        return ResponseEntity.ok("Organization updated successfully.");
    }

    @Operation(summary = "Get paginated users for an organization (Admin only)")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @GetMapping("/{orgId}/users")
    public ResponseEntity<PagedResponse<UserSummaryDto>> getOrganizationUsers(
            @PathVariable Long orgId,
            Authentication authentication,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "username", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String sortDir) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        PagedResponse<UserSummaryDto> users = organizationUserService.getOrganizationUsers(orgId,
                adminId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Update user status (Admin only)")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PutMapping("/{orgId}/users/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long orgId,
            @PathVariable Long userId,
            @RequestParam UserStatus status,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        organizationUserService.updateUserStatus(orgId, adminId, userId, status);
        return ResponseEntity.ok("User status updated successfully to " + status);
    }

    @Operation(summary = "Soft delete an organization (Admin only)")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @DeleteMapping("/{orgId}")
    public ResponseEntity<String> deleteOrganization(@PathVariable Long orgId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        organizationService.deleteOrganization(orgId, adminId);
        return ResponseEntity.ok("Organization soft-deleted successfully.");
    }
}
