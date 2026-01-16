package com.propertyiq.portfolio.mortgage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mortgages", schema = "portfolio_db", indexes = {
    @Index(name = "idx_mortgage_property", columnList = "property_id"),
    @Index(name = "idx_mortgage_user", columnList = "user_id"),
    @Index(name = "idx_mortgage_active", columnList = "is_active"),
    @Index(name = "idx_mortgage_sequence", columnList = "property_id,sequence_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mortgage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "lender", nullable = false, length = 255)
    private String lender;

    @Column(name = "original_loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalLoanAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "term_years", nullable = false)
    private Integer termYears;

    @Column(name = "mortgage_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MortgageType mortgageType;

    @Column(name = "product_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "principal_paid_to_date", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal principalPaidToDate = BigDecimal.ZERO;

    @Column(name = "interest_paid_to_date", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal interestPaidToDate = BigDecimal.ZERO;

    @Column(name = "monthly_payment", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "linked_to_mortgage_id")
    private UUID linkedToMortgageId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (principalPaidToDate == null) {
            principalPaidToDate = BigDecimal.ZERO;
        }
        if (interestPaidToDate == null) {
            interestPaidToDate = BigDecimal.ZERO;
        }
        if (currentBalance == null && originalLoanAmount != null) {
            currentBalance = originalLoanAmount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isRemortgaged() {
        return linkedToMortgageId != null;
    }

    public Integer getPaymentNumberForDate(LocalDate date) {
        if (date.isBefore(startDate)) {
            return 0;
        }
        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(startDate, date);
        return (int) (monthsBetween + 1);
    }

    public BigDecimal getMonthlyInterestRate() {
        return interestRate.divide(BigDecimal.valueOf(1200), 10, java.math.RoundingMode.HALF_UP);
    }
}
