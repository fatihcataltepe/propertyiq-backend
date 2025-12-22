package com.propertyiq.portfolio.mortgage.scheduler;

import com.propertyiq.portfolio.mortgage.model.Mortgage;
import com.propertyiq.portfolio.mortgage.repository.MortgageRepository;
import com.propertyiq.portfolio.mortgage.service.MortgagePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MortgagePaymentScheduler {

    private final MortgageRepository mortgageRepository;
    private final MortgagePaymentService paymentService;

    @Scheduled(cron = "0 0 2 * * *")
    public void generateScheduledPayments() {
        log.info("Starting daily scheduled payment generation");
        LocalDate today = LocalDate.now();

        List<Mortgage> mortgagesDueToday = mortgageRepository.findByPaymentDueDate(today);
        int generatedCount = 0;

        for (Mortgage mortgage : mortgagesDueToday) {
            if (!mortgage.getIsActive()) {
                continue;
            }

            try {
                var payment = paymentService.generateScheduledPayment(mortgage, today);
                if (payment != null) {
                    generatedCount++;
                    log.debug("Generated payment for mortgage {}", mortgage.getId());
                }
            } catch (Exception e) {
                log.error("Failed to generate payment for mortgage {}: {}", mortgage.getId(), e.getMessage());
            }
        }

        log.info("Completed scheduled payment generation. Generated {} payments for {} mortgages",
                generatedCount, mortgagesDueToday.size());
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void markOverduePayments() {
        log.info("Starting daily overdue payment check");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        try {
            int markedCount = paymentService.markOverduePayments(yesterday);
            log.info("Marked {} payments as overdue", markedCount);
        } catch (Exception e) {
            log.error("Failed to mark overdue payments: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 1 1 * *")
    public void monthlyReconciliation() {
        log.info("Starting monthly mortgage reconciliation");
        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        List<Mortgage> activeMortgages = mortgageRepository.findAll().stream()
                .filter(Mortgage::getIsActive)
                .toList();

        for (Mortgage mortgage : activeMortgages) {
            try {
                reconcileMortgage(mortgage, lastMonth);
            } catch (Exception e) {
                log.error("Failed to reconcile mortgage {}: {}", mortgage.getId(), e.getMessage());
            }
        }

        log.info("Completed monthly reconciliation for {} mortgages", activeMortgages.size());
    }

    private void reconcileMortgage(Mortgage mortgage, LocalDate asOfDate) {
        log.debug("Reconciling mortgage {} as of {}", mortgage.getId(), asOfDate);
    }
}
