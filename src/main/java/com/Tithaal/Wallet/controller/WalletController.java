package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.Wallet;
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
                Wallet updatedWallet = walletService.creditWallet(walletId, creditRequestDto);
                return ResponseEntity.ok(
                                "Wallet credited successfully to wallet id: " + walletId + " with amount: "
                                                + creditRequestDto.getAmount()
                                                + " and new balance is: " + updatedWallet.getBalance());
        }

        @PostMapping("/transfer")
        public ResponseEntity<String> transferMoney(@PathVariable Long userId,
                        @Valid @RequestBody DebitRequestDto debitRequestDto) {
                walletService.validateWalletOwnership(debitRequestDto.getSendingWalletId(), userId);
                Wallet updatedWallet = walletService.transferFunds(debitRequestDto);
                return ResponseEntity.ok(
                                "Wallet debited successfully from wallet id: " + debitRequestDto.getSendingWalletId()
                                                + " to wallet id: " + debitRequestDto.getReceivingWalletId()
                                                + " with amount: " + debitRequestDto.getAmount()
                                                + " and new balance is: " + updatedWallet.getBalance());
        }
}
