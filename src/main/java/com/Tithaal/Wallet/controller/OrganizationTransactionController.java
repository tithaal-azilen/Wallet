package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.security.CustomUserDetails;
import com.Tithaal.Wallet.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/transactions")
@RequiredArgsConstructor
@Tag(name = "Organization Transactions", description = "Endpoints for viewing organization transactions")
public class OrganizationTransactionController {

    private final OrganizationService organizationService;

    @Operation(summary = "Get paginated transactions for an organization (Admin only)")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @GetMapping("/{orgId}")
    public ResponseEntity<PagedResponse<OrganizationTransactionDto>> getTransactions(
            @PathVariable Long orgId,
            Authentication authentication,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(defaultValue = "DESC", required = false) String sortDir,
            @ModelAttribute com.Tithaal.Wallet.dto.TransactionFilterDto filterDto) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        PagedResponse<OrganizationTransactionDto> transactions = organizationService.getOrganizationTransactions(orgId,
                adminId, page, size, sortBy, sortDir, filterDto);
        return ResponseEntity.ok(transactions);
    }
}
