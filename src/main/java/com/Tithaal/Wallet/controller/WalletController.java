package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.ApiResponse;
import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.service.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/{userId}/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet Operations", description = "Operations related to Wallet Management (TopUp, Transfer)")
public class WalletController {

        private final WalletService walletService;

        @PostMapping("/{walletId}/payment")
        @PreAuthorize("#userId == principal.id")
        public ResponseEntity<ApiResponse<String>> processPayment(@PathVariable Long userId,
                        @PathVariable Long walletId,
                        @Valid @RequestBody CreditRequestDto creditRequestDto) {
                String result = walletService.TopUpWallet(walletId, creditRequestDto, userId);
                return ResponseEntity.ok(new ApiResponse<>(true, result, null));
        }

        @PostMapping("/transfer")
        @PreAuthorize("#userId == principal.id")
        public ResponseEntity<ApiResponse<String>> transferMoney(@PathVariable Long userId,
                        @Valid @RequestBody DebitRequestDto debitRequestDto) {
                String result = walletService.Transfer(debitRequestDto, userId);
                return ResponseEntity.ok(new ApiResponse<>(true, result, null));
        }
}
