package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

        private final WalletService walletService;

        @PostMapping("/user/{userId}/wallet/{walletId}/payment")
        public ResponseEntity<String> processPayment(@PathVariable Long userId, @PathVariable Long walletId,
                        @Valid @RequestBody CreditRequestDto creditRequestDto) {
                walletService.validateWalletOwnership(walletId, userId);
                walletService.creditWallet(walletId, creditRequestDto.getAmount());
                return ResponseEntity.ok(
                                "Wallet credited successfully to wallet id: " + walletId + " with amount: "
                                                + creditRequestDto.getAmount());
        }

        @PostMapping("/user/{userId}/transfer")
        public ResponseEntity<String> transferMoney(@PathVariable Long userId,
                        @Valid @RequestBody DebitRequestDto debitRequestDto) {
                walletService.validateWalletOwnership(debitRequestDto.getSendingWalletId(), userId);
                walletService.transferFunds(debitRequestDto.getSendingWalletId(),
                                debitRequestDto.getReceivingWalletId(),
                                debitRequestDto.getAmount());
                return ResponseEntity.ok(
                                "Wallet debited successfully from wallet id: " + debitRequestDto.getSendingWalletId()
                                                + " to wallet id: " + debitRequestDto.getReceivingWalletId()
                                                + " with amount: " + debitRequestDto.getAmount());
        }
}
