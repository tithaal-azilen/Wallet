package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.OrganizationRegistrationDto;
import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.security.CustomUserDetails;
import com.Tithaal.Wallet.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Control", description = "Endpoints for managing organizations and viewing their transactions")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Register an Organization and Admin User")
    @PostMapping
    public ResponseEntity<String> registerOrganization(
            @Valid @RequestBody OrganizationRegistrationDto registrationDto) {
        String orgCode = organizationService.registerOrganizationAndAdmin(registrationDto);
        return new ResponseEntity<>("Organization and Admin created successfully! Organization Code: " + orgCode,
                HttpStatus.CREATED);
    }

    @Operation(summary = "Get paginated transactions for an organization (Admin only)")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @GetMapping("/{orgId}/transactions")
    public ResponseEntity<Page<OrganizationTransactionDto>> getTransactions(
            @PathVariable Long orgId,
            Authentication authentication,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        Page<OrganizationTransactionDto> transactions = organizationService.getOrganizationTransactions(orgId,
                adminId, pageable);
        return ResponseEntity.ok(transactions);
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
