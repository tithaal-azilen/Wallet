package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.PagedResponse;
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
    private final com.Tithaal.Wallet.service.PdfReportService pdfReportService;

    @Operation(summary = "Get all platform transactions (Super Admin only)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<OrganizationTransactionDto>> getAllTransactions(
            @ModelAttribute com.Tithaal.Wallet.dto.SuperAdminTransactionFilterDto filter,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(defaultValue = "DESC", required = false) String sortDir) {

        PagedResponse<OrganizationTransactionDto> transactions = platformTransactionService.getAllTransactions(filter,
                page,
                size, sortBy, sortDir);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Download all platform transactions as PDF (Super Admin only)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadAllTransactions(
            @ModelAttribute com.Tithaal.Wallet.dto.SuperAdminTransactionFilterDto filter,
            @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(defaultValue = "DESC", required = false) String sortDir) {

        java.util.List<com.Tithaal.Wallet.dto.OrganizationTransactionDto> transactions = platformTransactionService.getAllPlatformTransactions(filter, sortBy, sortDir);
        byte[] pdfBytes = pdfReportService.generateOrgTransactionReport(transactions);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename("platform_transaction_report.pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
