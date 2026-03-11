package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.service.WalletHistoryService;
import com.Tithaal.Wallet.service.WalletService;
import com.Tithaal.Wallet.dto.PagedResponse;
import com.Tithaal.Wallet.dto.UserTransactionFilterDto;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Tag(name = "Transaction History", description = "Operations related to Wallet Transaction History (Ledger)")
public class WalletHistoryController {

    private final WalletHistoryService walletHistoryService;
    private final WalletService walletService;
    private final com.Tithaal.Wallet.service.PdfReportService pdfReportService;

    @Operation(summary = "Get User Transaction History", description = "Retrieve transaction history for a specific user")
    @GetMapping("/{userid}")
    @PreAuthorize("#userid == principal.id")
    public ResponseEntity<PagedResponse<WalletTransactionEntryDto>> getUserHistory(
            @PathVariable Long userid,
            @ModelAttribute UserTransactionFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<WalletTransactionEntryDto> ledger = walletHistoryService.getUserHistory(userid, filterDto, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ledger);
    }

    @Operation(summary = "Get Wallet Transaction History", description = "Retrieve transaction history for a specific wallet")
    @GetMapping("/{userid}/wallet/{walletid}")
    @PreAuthorize("#userid == principal.id")
    public ResponseEntity<List<WalletTransactionEntryDto>> getWalletHistory(@PathVariable Long walletid,
            @PathVariable Long userid) {
        walletService.validateWalletOwnership(walletid, userid);
        List<WalletTransactionEntryDto> ledger = walletHistoryService.getWalletHistory(walletid);
        return ResponseEntity.ok(ledger);
    }

    @Operation(summary = "Download User Transaction History as PDF", description = "Download transaction history for a specific user as a PDF file")
    @GetMapping("/{userid}/download")
    @PreAuthorize("#userid == principal.id")
    public ResponseEntity<byte[]> downloadUserHistory(
            @PathVariable Long userid,
            @ModelAttribute UserTransactionFilterDto filterDto,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<WalletTransactionEntryDto> transactions = walletHistoryService.getAllUserHistory(userid, filterDto, sortBy, sortDir);
        byte[] pdfBytes = pdfReportService.generateUserTransactionReport(transactions);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename("transaction_report.pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
