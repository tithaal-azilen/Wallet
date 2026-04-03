package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserTransactionFilterDto;
import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.security.SecurityUtils;
import com.Tithaal.Wallet.service.WalletHistoryService;
import com.Tithaal.Wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.Tithaal.Wallet.service.PdfReportService;

import java.util.List;
import java.util.UUID;

/**
 * Ledger (transaction history) endpoints.
 * All identity is sourced from the JWT token — no userId in the URL.
 */
@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Tag(name = "Transaction History", description = "Operations related to Wallet Transaction History (Ledger)")
public class WalletHistoryController {

    private final WalletHistoryService walletHistoryService;
    private final WalletService walletService;
    private final PdfReportService pdfReportService;

    @Operation(summary = "Get My Transaction History", description = "Retrieve paginated transaction history for the authenticated user")
    @GetMapping("/me")
    public ResponseEntity<PagedResponse<WalletTransactionEntryDto>> getUserHistory(
            @ModelAttribute UserTransactionFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        UUID userId = SecurityUtils.getCurrentUserId();
        PagedResponse<WalletTransactionEntryDto> ledger =
                walletHistoryService.getUserHistory(userId, filterDto, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ledger);
    }

    @Operation(summary = "Get Wallet Transaction History", description = "Retrieve transaction history for a specific wallet owned by the authenticated user")
    @GetMapping("/me/wallet/{walletId}")
    public ResponseEntity<List<WalletTransactionEntryDto>> getWalletHistory(
            @PathVariable Long walletId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        walletService.validateWalletOwnership(walletId, userId);
        List<WalletTransactionEntryDto> ledger = walletHistoryService.getWalletHistory(walletId);
        return ResponseEntity.ok(ledger);
    }

    @Operation(summary = "Download My Transaction History as PDF", description = "Download transaction history for the authenticated user as a PDF file")
    @GetMapping("/me/download")
    public ResponseEntity<byte[]> downloadUserHistory(
            @ModelAttribute UserTransactionFilterDto filterDto,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<WalletTransactionEntryDto> transactions =
                walletHistoryService.getAllUserHistory(userId, filterDto, sortBy, sortDir);
        byte[] pdfBytes = pdfReportService.generateUserTransactionReport(transactions);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("transaction_report.pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
