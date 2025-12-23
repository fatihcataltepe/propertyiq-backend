package com.propertyiq.portfolio.mortgage.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MortgageCalculator {

    private static final int ROUNDING_SCALE = 10;
    private static final int DISPLAY_SCALE = 2;

    /**
     * Calculate monthly payment using amortization formula
     * M = P × [r(1+r)^n] / [(1+r)^n - 1]
     *
     * @param principal  Original loan amount
     * @param annualRate Annual interest rate (e.g., 4.5 for 4.5%)
     * @param termYears  Loan term in years
     * @return Monthly payment amount
     */
    public BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, Integer termYears) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        if (termYears == null || termYears <= 0) {
            throw new IllegalArgumentException("Term must be positive");
        }

        BigDecimal monthlyRate = getMonthlyRate(annualRate);
        int numberOfPayments = termYears * 12;

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(numberOfPayments), DISPLAY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = monthlyRate.add(BigDecimal.ONE);
        BigDecimal onePlusRToN = power(onePlusR, numberOfPayments);

        BigDecimal numerator = monthlyRate.multiply(onePlusRToN);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);

        return principal
                .multiply(numerator)
                .divide(denominator, ROUNDING_SCALE, RoundingMode.HALF_UP)
                .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate interest for a month
     * Interest = Current Balance × (Annual Rate / 12 / 100)
     */
    public BigDecimal calculateMonthlyInterest(BigDecimal balance, BigDecimal annualRate) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }

        BigDecimal monthlyRate = getMonthlyRate(annualRate);
        return balance.multiply(monthlyRate).setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate principal portion of payment
     * Principal = Monthly Payment - Interest
     */
    public BigDecimal calculatePrincipalPortion(BigDecimal monthlyPayment, BigDecimal monthlyInterest) {
        if (monthlyPayment == null || monthlyInterest == null) {
            throw new IllegalArgumentException("Payment and interest must not be null");
        }
        return monthlyPayment.subtract(monthlyInterest).setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate balance after payment
     * Balance = Previous Balance - Principal
     */
    public BigDecimal calculateRemainingBalance(BigDecimal previousBalance, BigDecimal principalPaid) {
        if (previousBalance == null || principalPaid == null) {
            throw new IllegalArgumentException("Balance and principal must not be null");
        }
        return previousBalance.subtract(principalPaid)
                .max(BigDecimal.ZERO)
                .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Get monthly interest rate
     * r = Annual Rate / 12 / 100
     */
    public BigDecimal getMonthlyRate(BigDecimal annualRate) {
        if (annualRate == null) {
            return BigDecimal.ZERO;
        }
        return annualRate.divide(BigDecimal.valueOf(1200), ROUNDING_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate remaining payments
     */
    public Integer calculateRemainingPayments(Integer originalPayments, Integer paymentsCompleted) {
        if (originalPayments == null || paymentsCompleted == null) {
            return 0;
        }
        return Math.max(0, originalPayments - paymentsCompleted);
    }

    /**
     * Calculate percentage repaid
     */
    public Double calculatePercentageRepaid(BigDecimal originalAmount, BigDecimal amountRepaid) {
        if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (amountRepaid == null) {
            return 0.0;
        }
        return amountRepaid.divide(originalAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculate total interest over loan lifetime
     * Total Interest = (Monthly Payment × Number of Payments) - Principal
     */
    public BigDecimal calculateTotalInterest(BigDecimal monthlyPayment, Integer numberOfPayments, BigDecimal originalPrincipal) {
        if (monthlyPayment == null || numberOfPayments == null || originalPrincipal == null) {
            throw new IllegalArgumentException("All parameters must not be null");
        }
        BigDecimal totalPayment = monthlyPayment.multiply(BigDecimal.valueOf(numberOfPayments));
        return totalPayment.subtract(originalPrincipal).setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal power(BigDecimal base, int exponent) {
        if (exponent < 0) {
            throw new IllegalArgumentException("Exponent must be non-negative");
        }
        if (exponent == 0) {
            return BigDecimal.ONE;
        }
        BigDecimal result = BigDecimal.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(base).setScale(ROUNDING_SCALE, RoundingMode.HALF_UP);
        }
        return result;
    }
}
