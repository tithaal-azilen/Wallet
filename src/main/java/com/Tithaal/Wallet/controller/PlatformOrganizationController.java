package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.OrganizationDto;
import com.Tithaal.Wallet.dto.OrganizationFilterDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.service.PlatformOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/organizations")
@RequiredArgsConstructor
@Tag(name = "Platform Organization Management", description = "Endpoints for Super Admin to manage organizations")
public class PlatformOrganizationController {

    private final PlatformOrganizationService platformOrganizationService;

    @Operation(summary = "Get all organizations (Super Admin only)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<OrganizationDto>> getAllOrganizations(
            @ModelAttribute OrganizationFilterDto filter,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String sortDir) {

        PagedResponse<OrganizationDto> orgs = platformOrganizationService.getAllOrganizations(filter, page, size,
                sortBy,
                sortDir);
        return ResponseEntity.ok(orgs);
    }

    @Operation(summary = "Update organization status (Super Admin only)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{orgId}/status")
    public ResponseEntity<String> updateOrganizationStatus(
            @PathVariable Long orgId,
            @RequestParam OrganizationStatus status) {

        platformOrganizationService.updateOrganizationStatus(orgId, status);
        return ResponseEntity.ok("Organization status updated successfully to " + status);
    }
}
