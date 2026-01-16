package com.propertyiq.portfolio.mortgage.dto;

import com.propertyiq.portfolio.mortgage.model.Mortgage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MortgageSummary {

    private UUID id;
    private String lender;
    private BigDecimal originalLoanAmount;
    private BigDecimal currentBalance;
    private BigDecimal monthlyPayment;
    private Integer sequenceNumber;
    private LocalDate endDate;
    private Boolean isActive;
    private Integer remainingPayments;
    private String formattedBalance;
    private String formattedMonthlyPayment;
    private String remainingTerm;

    public static MortgageSummary fromEntity(Mortgage mortgage) {
        int totalPayments = mortgage.getTermYears() * 12;
        long monthsElapsed = ChronoUnit.MONTHS.between(mortgage.getStartDate(), LocalDate.now());
        int remainingPayments = Math.max(0, totalPayments - (int) monthsElapsed);

        int years = remainingPayments / 12;
        int months = remainingPayments % 12;
        String remainingTerm = years > 0 
                ? String.format("%d years %d months", years, months)
                : String.format("%d months", months);

        return MortgageSummary.builder()
                .id(mortgage.getId())
                .lender(mortgage.getLender())
                .originalLoanAmount(mortgage.getOriginalLoanAmount())
                .currentBalance(mortgage.getCurrentBalance())
                .monthlyPayment(mortgage.getMonthlyPayment())
                .sequenceNumber(mortgage.getSequenceNumber())
                .endDate(mortgage.getEndDate())
                .isActive(mortgage.getIsActive())
                .remainingPayments(remainingPayments)
                .formattedBalance(formatCurrency(mortgage.getCurrentBalance()))
                .formattedMonthlyPayment(formatCurrency(mortgage.getMonthlyPayment()))
                .remainingTerm(remainingTerm)
                .build();
    }

    private static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "£0.00";
        }
        return String.format("£%,.2f", amount);
    }
}
