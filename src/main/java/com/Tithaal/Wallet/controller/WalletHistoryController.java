package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.service.WalletHistoryService;
import com.Tithaal.Wallet.service.WalletService;

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

    @Operation(summary = "Get User Transaction History", description = "Retrieve transaction history for a specific user")
    @GetMapping("/{userid}")
    @PreAuthorize("#userid == principal.id")
    public ResponseEntity<List<WalletTransactionEntryDto>> getUserHistory(@PathVariable Long userid) {
        List<WalletTransactionEntryDto> ledger = walletHistoryService.getUserHistory(userid);
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
}
