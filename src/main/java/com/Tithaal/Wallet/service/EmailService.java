package com.Tithaal.Wallet.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface EmailService {
    void sendWalletCreationEmail(String toEmail, Long walletId);

    void sendFeeDeductionEmail(String toEmail, Long walletId, BigDecimal amount, LocalDate date);
}
