package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.ApiResponse;
import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.security.SecurityUtils;
import com.Tithaal.Wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Wallet operations — userId is always extracted from the JWT token, never from the URL.
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet Operations", description = "Operations related to Wallet Management (TopUp, Transfer)")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Top Up Wallet", description = "Credit funds into a specific wallet. User identity taken from JWT.")
    @PostMapping("/{walletId}/topup")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<String>> processPayment(
            @PathVariable Long walletId,
            @Valid @RequestBody CreditRequestDto creditRequestDto) {
        UUID userId = SecurityUtils.getCurrentUserId();
        String result = walletService.topUpWallet(walletId, creditRequestDto, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
    }

    @Operation(summary = "Transfer Funds", description = "Transfer funds between wallets. Sender identity taken from JWT.")
    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<String>> transferMoney(
            @Valid @RequestBody DebitRequestDto debitRequestDto) {
        UUID userId = SecurityUtils.getCurrentUserId();
        String status = SecurityUtils.getCurrentStatus();
        String result = walletService.transfer(debitRequestDto, userId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
    }
}
