package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.AmountDto;
import com.Tithaal.Wallet.dto.TransferDto;
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

    @PostMapping("/{walletId}/credit")
    public ResponseEntity<String> creditWallet(@PathVariable Long walletId, @Valid @RequestBody AmountDto amountDto) {
        walletService.creditWallet(walletId, amountDto.getAmount());
        return ResponseEntity.ok(
                "Wallet credited successfully to wallet id: " + walletId + " with amount: " + amountDto.getAmount());
    }

    @PostMapping("/{walletId}/debit")
    public ResponseEntity<String> transferFunds(@PathVariable Long walletId,
            @Valid @RequestBody TransferDto transferDto) {
        walletService.transferFunds(walletId, transferDto.getRecipientWalletId(),
                transferDto.getAmount());
        return ResponseEntity.ok("Wallet debited successfully from wallet id: " + walletId + " to wallet id: "
                + transferDto.getRecipientWalletId() + " with amount: " + transferDto.getAmount());
    }
}
