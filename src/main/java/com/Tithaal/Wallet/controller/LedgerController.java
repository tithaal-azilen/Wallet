package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.LedgerEntryDto;
import com.Tithaal.Wallet.service.LedgerService;
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
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/{userid}")
    public ResponseEntity<List<LedgerEntryDto>> getUserLedger(@PathVariable Long userid) {
        List<LedgerEntryDto> ledger = ledgerService.getUserLedger(userid);
        return ResponseEntity.ok(ledger);
    }
}
