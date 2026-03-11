package com.Tithaal.Wallet.service;

import com.Tithaal.Wallet.dto.OrganizationTransactionDto;
import com.Tithaal.Wallet.dto.WalletTransactionEntryDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfReportService {

    public byte[] generateUserTransactionReport(List<WalletTransactionEntryDto> transactions) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Transaction History Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Spacer

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 4, 2, 2});

            // Headers
            addHeader(table, "Date");
            addHeader(table, "Type");
            addHeader(table, "Description");
            addHeader(table, "Amount");
            addHeader(table, "Balance After");

            // Data
            for (WalletTransactionEntryDto t : transactions) {
                table.addCell(t.getCreatedAt().toString());
                table.addCell(t.getType().toString());
                table.addCell(t.getDescription());
                table.addCell(t.getAmount().toString());
                table.addCell(t.getBalanceAfter().toString());
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return out.toByteArray();
    }

    public byte[] generateOrgTransactionReport(List<OrganizationTransactionDto> transactions) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Organization Transaction Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Spacer

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 2, 3, 2, 2});

            // Headers
            addHeader(table, "Date");
            addHeader(table, "User");
            addHeader(table, "Type");
            addHeader(table, "Description");
            addHeader(table, "Amount");
            addHeader(table, "Balance After");

            // Data
            for (OrganizationTransactionDto t : transactions) {
                table.addCell(t.getCreatedAt().toString());
                table.addCell(t.getUsername());
                table.addCell(t.getType().toString());
                table.addCell(t.getDescription());
                table.addCell(t.getAmount().toString());
                table.addCell(t.getBalanceAfter().toString());
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return out.toByteArray();
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setBorderWidth(2);
        header.setPhrase(new Phrase(text));
        table.addCell(header);
    }
}
