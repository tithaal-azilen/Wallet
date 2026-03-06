package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.TransactionFilterDto;
import com.Tithaal.Wallet.service.PlatformTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/transactions")
@RequiredArgsConstructor
@Tag(name = "Platform Transaction Management", description = "Endpoints for Super Admin to view global transactions")
public class PlatformTransactionController {

    private final PlatformTransactionService platformTransactionService;

    @Operation(summary = "Get all platform transactions (Super Admin only)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<OrganizationTransactionDto>> getAllTransactions(
            @ModelAttribute TransactionFilterDto filter,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(defaultValue = "DESC", required = false) String sortDir) {

        PagedResponse<OrganizationTransactionDto> transactions = platformTransactionService.getAllTransactions(filter,
                page,
                size, sortBy, sortDir);
        return ResponseEntity.ok(transactions);
    }
}
