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
@Table(name = "mortgage_payments", schema = "portfolio_db", indexes = {
    @Index(name = "idx_mortgage_payments", columnList = "mortgage_id,due_date"),
    @Index(name = "idx_payment_number", columnList = "mortgage_id,payment_number"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_type", columnList = "payment_type"),
    @Index(name = "idx_due_date", columnList = "due_date"),
    @Index(name = "idx_actual_payment_date", columnList = "actual_payment_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MortgagePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mortgage_id", nullable = false)
    private UUID mortgageId;

    @Column(name = "payment_number")
    private Integer paymentNumber;

    @Column(name = "payment_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "source", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentSource source;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "actual_payment_date")
    private LocalDate actualPaymentDate;

    @Column(name = "principal", nullable = false, precision = 10, scale = 2)
    private BigDecimal principal;

    @Column(name = "interest", nullable = false, precision = 10, scale = 2)
    private BigDecimal interest;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "balance_before", precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.SCHEDULED;

    @Column(name = "topup_reason", length = 255)
    private String topupReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.SCHEDULED;
        }
    }

    public boolean isScheduled() {
        return paymentType == PaymentType.SCHEDULED;
    }

    public boolean isTopUp() {
        return paymentType == PaymentType.TOPUP;
    }

    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }

    public boolean isOverdue() {
        return status == PaymentStatus.SCHEDULED && dueDate.isBefore(LocalDate.now());
    }

    public boolean isScheduledPayment() {
        return paymentNumber != null && paymentType == PaymentType.SCHEDULED;
    }

    public String getDisplayStatus() {
        return status != null ? status.getDisplayName() : "Unknown";
    }
}
