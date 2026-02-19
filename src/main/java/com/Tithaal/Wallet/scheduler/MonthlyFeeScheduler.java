package com.Tithaal.Wallet.scheduler;

import com.Tithaal.Wallet.service.FeeDeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonthlyFeeScheduler {

    private final FeeDeductionService feeDeductionService;

    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    public void runMonthlyFeeDeduction() {
        feeDeductionService.deductFees();
    }
}
