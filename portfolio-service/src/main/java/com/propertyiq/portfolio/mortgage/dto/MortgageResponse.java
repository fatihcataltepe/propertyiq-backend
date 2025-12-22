package com.propertyiq.portfolio.mortgage.dto;

import com.propertyiq.portfolio.mortgage.model.Mortgage;
import com.propertyiq.portfolio.mortgage.model.MortgageType;
import com.propertyiq.portfolio.mortgage.model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MortgageResponse {

    private UUID id;
    private UUID propertyId;
    private String lender;
    private BigDecimal originalLoanAmount;
    private BigDecimal interestRate;
    private Integer termYears;
    private MortgageType mortgageType;
    private ProductType productType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private BigDecimal currentBalance;
    private BigDecimal principalPaidToDate;
    private BigDecimal interestPaidToDate;
    private BigDecimal monthlyPayment;
    private Integer sequenceNumber;
    private UUID linkedToMortgageId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String formattedMonthlyPayment;
    private String formattedCurrentBalance;
    private String formattedPrincipalPaid;
    private String formattedInterestPaid;
    private Double remainingYears;
    private Integer remainingPayments;
    private Double percentageRepaid;

    public static MortgageResponse fromEntity(Mortgage mortgage) {
        int totalPayments = mortgage.getTermYears() * 12;
        long monthsElapsed = ChronoUnit.MONTHS.between(mortgage.getStartDate(), LocalDate.now());
        int remainingPayments = Math.max(0, totalPayments - (int) monthsElapsed);
        double remainingYears = remainingPayments / 12.0;

        double percentageRepaid = 0.0;
        if (mortgage.getOriginalLoanAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentageRepaid = mortgage.getPrincipalPaidToDate()
                    .divide(mortgage.getOriginalLoanAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return MortgageResponse.builder()
                .id(mortgage.getId())
                .propertyId(mortgage.getPropertyId())
                .lender(mortgage.getLender())
                .originalLoanAmount(mortgage.getOriginalLoanAmount())
                .interestRate(mortgage.getInterestRate())
                .termYears(mortgage.getTermYears())
                .mortgageType(mortgage.getMortgageType())
                .productType(mortgage.getProductType())
                .startDate(mortgage.getStartDate())
                .endDate(mortgage.getEndDate())
                .isActive(mortgage.getIsActive())
                .currentBalance(mortgage.getCurrentBalance())
                .principalPaidToDate(mortgage.getPrincipalPaidToDate())
                .interestPaidToDate(mortgage.getInterestPaidToDate())
                .monthlyPayment(mortgage.getMonthlyPayment())
                .sequenceNumber(mortgage.getSequenceNumber())
                .linkedToMortgageId(mortgage.getLinkedToMortgageId())
                .notes(mortgage.getNotes())
                .createdAt(mortgage.getCreatedAt())
                .updatedAt(mortgage.getUpdatedAt())
                .formattedMonthlyPayment(formatCurrency(mortgage.getMonthlyPayment()))
                .formattedCurrentBalance(formatCurrency(mortgage.getCurrentBalance()))
                .formattedPrincipalPaid(formatCurrency(mortgage.getPrincipalPaidToDate()))
                .formattedInterestPaid(formatCurrency(mortgage.getInterestPaidToDate()))
                .remainingYears(remainingYears)
                .remainingPayments(remainingPayments)
                .percentageRepaid(percentageRepaid)
                .build();
    }

    private static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "£0.00";
        }
        return String.format("£%,.2f", amount);
    }
}
