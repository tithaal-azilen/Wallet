package com.Tithaal.Wallet.service.impl;

import com.Tithaal.Wallet.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendWalletCreationEmail(String toEmail, Long walletId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Wallet Created Successfully");
            message.setText("Dear User,\n\nYour wallet has been created successfully.\nWallet ID: " + walletId
                    + "\n\nThank you for using our service.");

            javaMailSender.send(message);
            log.info("Wallet creation email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send wallet creation email to {}", toEmail, e);
        }
    }

    @Override
    public void sendFeeDeductionEmail(String toEmail, Long walletId, BigDecimal amount, LocalDate date) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Monthly Fee Deduction Notification");
            message.setText("Dear User,\n\nA monthly fee of " + amount + " has been deducted from your wallet (ID: "
                    + walletId + ") on " + date + ".\n\nThank you.");

            javaMailSender.send(message);
            log.info("Fee deduction email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send fee deduction email to {}", toEmail, e);
        }
    }
}
