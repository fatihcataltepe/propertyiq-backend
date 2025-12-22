package com.propertyiq.portfolio.mortgage.dto;

import com.propertyiq.portfolio.mortgage.model.MortgagePayment;
import com.propertyiq.portfolio.mortgage.model.PaymentSource;
import com.propertyiq.portfolio.mortgage.model.PaymentStatus;
import com.propertyiq.portfolio.mortgage.model.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MortgagePaymentResponse {

    private UUID id;
    private UUID mortgageId;
    private Integer paymentNumber;
    private PaymentType paymentType;
    private PaymentSource source;
    private LocalDate dueDate;
    private LocalDate actualPaymentDate;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal totalAmount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private PaymentStatus status;
    private String topupReason;
    private LocalDateTime createdAt;

    private String formattedPrincipal;
    private String formattedInterest;
    private String formattedTotalAmount;
    private String formattedBalanceBefore;
    private String formattedBalanceAfter;
    private String displayStatus;
    private String displayPaymentType;

    public static MortgagePaymentResponse fromEntity(MortgagePayment payment) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.UK);

        return MortgagePaymentResponse.builder()
                .id(payment.getId())
                .mortgageId(payment.getMortgageId())
                .paymentNumber(payment.getPaymentNumber())
                .paymentType(payment.getPaymentType())
                .source(payment.getSource())
                .dueDate(payment.getDueDate())
                .actualPaymentDate(payment.getActualPaymentDate())
                .principal(payment.getPrincipal())
                .interest(payment.getInterest())
                .totalAmount(payment.getTotalAmount())
                .balanceBefore(payment.getBalanceBefore())
                .balanceAfter(payment.getBalanceAfter())
                .status(payment.getStatus())
                .topupReason(payment.getTopupReason())
                .createdAt(payment.getCreatedAt())
                .formattedPrincipal(currencyFormat.format(payment.getPrincipal()))
                .formattedInterest(currencyFormat.format(payment.getInterest()))
                .formattedTotalAmount(currencyFormat.format(payment.getTotalAmount()))
                .formattedBalanceBefore(payment.getBalanceBefore() != null ? currencyFormat.format(payment.getBalanceBefore()) : null)
                .formattedBalanceAfter(payment.getBalanceAfter() != null ? currencyFormat.format(payment.getBalanceAfter()) : null)
                .displayStatus(payment.getDisplayStatus())
                .displayPaymentType(payment.getPaymentType() == PaymentType.SCHEDULED ? "Scheduled Payment" : "Top-up Payment")
                .build();
    }
}
