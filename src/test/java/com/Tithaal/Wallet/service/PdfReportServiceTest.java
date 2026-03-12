package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.Tithaal.Wallet.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfReportServiceTest {

    private final PdfReportService pdfReportService = new PdfReportService();

    @Test
    public void shouldGenerateUserReport() {
        WalletTransactionEntryDto t1 = new WalletTransactionEntryDto();
        t1.setId(1L);
        t1.setType(TransactionType.CREDIT);
        t1.setAmount(BigDecimal.TEN);
        t1.setDescription("Test Topup");
        t1.setBalanceAfter(BigDecimal.TEN);
        t1.setCreatedAt(Instant.now());

        byte[] report = pdfReportService.generateUserTransactionReport(Arrays.asList(t1));

        assertNotNull(report);
        assertTrue(report.length > 0);
    }

    @Test
    public void shouldGenerateOrgReport() {
        OrganizationTransactionDto t1 = new OrganizationTransactionDto();
        t1.setId(1L);
        t1.setUsername("testuser");
        t1.setType(TransactionType.DEBIT);
        t1.setAmount(BigDecimal.ONE);
        t1.setDescription("Test Transfer");
        t1.setBalanceAfter(BigDecimal.valueOf(9));
        t1.setCreatedAt(Instant.now());

        byte[] report = pdfReportService.generateOrgTransactionReport(Arrays.asList(t1));

        assertNotNull(report);
        assertTrue(report.length > 0);
    }

    @Test
    public void shouldGenerateReportWithNoTransactions() {
        byte[] report = pdfReportService.generateUserTransactionReport(Collections.emptyList());

        assertNotNull(report);
        assertTrue(report.length > 0);
    }
}
