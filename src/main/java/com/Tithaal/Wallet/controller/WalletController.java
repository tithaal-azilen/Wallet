package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/{userId}/wallet")
@RequiredArgsConstructor
public class WalletController {

        private final WalletService walletService;

        @PostMapping("/{walletId}/payment")
        public ResponseEntity<String> processPayment(@PathVariable Long userId, @PathVariable Long walletId,
                        @Valid @RequestBody CreditRequestDto creditRequestDto) {
                walletService.validateWalletOwnership(walletId, userId);
                String result = walletService.TopUpWallet(walletId, creditRequestDto);
                return ResponseEntity.ok(result);
        }

        @PostMapping("/transfer")
        public ResponseEntity<String> transferMoney(@PathVariable Long userId,
                        @Valid @RequestBody DebitRequestDto debitRequestDto) {
                walletService.validateWalletOwnership(debitRequestDto.getSendingWalletId(), userId);
                String result = walletService.Transfer(debitRequestDto);
                return ResponseEntity.ok(result);
        }
}
