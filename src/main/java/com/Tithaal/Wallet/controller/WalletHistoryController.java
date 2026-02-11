package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.service.WalletHistoryService;
import com.Tithaal.Wallet.service.WalletService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class WalletHistoryController {

    private final WalletHistoryService walletHistoryService;
    private final WalletService walletService;

    @GetMapping("/{userid}")
    public ResponseEntity<List<WalletTransactionEntryDto>> getUserHistory(@PathVariable Long userid) {
        List<WalletTransactionEntryDto> ledger = walletHistoryService.getUserHistory(userid);
        return ResponseEntity.ok(ledger);
    }

    @GetMapping("/{userid}/wallet/{walletid}")
    public ResponseEntity<List<WalletTransactionEntryDto>> getWalletHistory(@PathVariable Long walletid,
            @PathVariable Long userid) {
        walletService.validateWalletOwnership(walletid, userid);
        List<WalletTransactionEntryDto> ledger = walletHistoryService.getWalletHistory(walletid);
        return ResponseEntity.ok(ledger);
    }
}
