package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.AmountDto;
import com.Tithaal.Wallet.dto.TransferDto;
import com.Tithaal.Wallet.entity.Wallet;
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
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long walletId, @Valid @RequestBody AmountDto amountDto) {
        Wallet wallet = walletService.creditWallet(walletId, amountDto.getAmount());
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/{walletId}/debit")
    public ResponseEntity<Wallet> transferFunds(@PathVariable Long walletId,
            @Valid @RequestBody TransferDto transferDto) {
        Wallet wallet = walletService.transferFunds(walletId, transferDto.getRecipientWalletId(),
                transferDto.getAmount());
        return ResponseEntity.ok(wallet);
    }
}
