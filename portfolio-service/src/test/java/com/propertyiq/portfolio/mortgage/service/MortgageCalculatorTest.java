package com.propertyiq.portfolio.mortgage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MortgageCalculatorTest {

    private MortgageCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MortgageCalculator();
    }

    @Nested
    @DisplayName("calculateMonthlyPayment")
    class CalculateMonthlyPaymentTests {

        @Test
        @DisplayName("should calculate correct monthly payment for standard mortgage")
        void shouldCalculateCorrectMonthlyPayment() {
            BigDecimal principal = new BigDecimal("360000");
            BigDecimal annualRate = new BigDecimal("4.5");
            Integer termYears = 25;

            BigDecimal monthlyPayment = calculator.calculateMonthlyPayment(principal, annualRate, termYears);

            assertEquals(new BigDecimal("2001.00"), monthlyPayment);
        }

        @Test
        @DisplayName("should calculate correct monthly payment for smaller loan")
        void shouldCalculateCorrectMonthlyPaymentForSmallerLoan() {
            BigDecimal principal = new BigDecimal("100000");
            BigDecimal annualRate = new BigDecimal("3.5");
            Integer termYears = 15;

            BigDecimal monthlyPayment = calculator.calculateMonthlyPayment(principal, annualRate, termYears);

            assertEquals(new BigDecimal("714.88"), monthlyPayment);
        }

        @Test
        @DisplayName("should handle zero interest rate")
        void shouldHandleZeroInterestRate() {
            BigDecimal principal = new BigDecimal("120000");
            BigDecimal annualRate = BigDecimal.ZERO;
            Integer termYears = 10;

            BigDecimal monthlyPayment = calculator.calculateMonthlyPayment(principal, annualRate, termYears);

            assertEquals(new BigDecimal("1000.00"), monthlyPayment);
        }

        @Test
        @DisplayName("should throw exception for negative principal")
        void shouldThrowExceptionForNegativePrincipal() {
            BigDecimal principal = new BigDecimal("-100000");
            BigDecimal annualRate = new BigDecimal("4.5");
            Integer termYears = 25;

            assertThrows(IllegalArgumentException.class, () ->
                    calculator.calculateMonthlyPayment(principal, annualRate, termYears));
        }

        @Test
        @DisplayName("should throw exception for null principal")
        void shouldThrowExceptionForNullPrincipal() {
            BigDecimal annualRate = new BigDecimal("4.5");
            Integer termYears = 25;

            assertThrows(IllegalArgumentException.class, () ->
                    calculator.calculateMonthlyPayment(null, annualRate, termYears));
        }

        @Test
        @DisplayName("should throw exception for negative interest rate")
        void shouldThrowExceptionForNegativeInterestRate() {
            BigDecimal principal = new BigDecimal("100000");
            BigDecimal annualRate = new BigDecimal("-4.5");
            Integer termYears = 25;

            assertThrows(IllegalArgumentException.class, () ->
                    calculator.calculateMonthlyPayment(principal, annualRate, termYears));
        }

        @Test
        @DisplayName("should throw exception for zero term years")
        void shouldThrowExceptionForZeroTermYears() {
            BigDecimal principal = new BigDecimal("100000");
            BigDecimal annualRate = new BigDecimal("4.5");
            Integer termYears = 0;

            assertThrows(IllegalArgumentException.class, () ->
                    calculator.calculateMonthlyPayment(principal, annualRate, termYears));
        }
    }

    @Nested
    @DisplayName("calculateMonthlyInterest")
    class CalculateMonthlyInterestTests {

        @Test
        @DisplayName("should calculate correct monthly interest")
        void shouldCalculateCorrectMonthlyInterest() {
            BigDecimal balance = new BigDecimal("360000");
            BigDecimal annualRate = new BigDecimal("4.5");

            BigDecimal monthlyInterest = calculator.calculateMonthlyInterest(balance, annualRate);

            assertEquals(new BigDecimal("1350.00"), monthlyInterest);
        }

        @Test
        @DisplayName("should return zero for zero balance")
        void shouldReturnZeroForZeroBalance() {
            BigDecimal balance = BigDecimal.ZERO;
            BigDecimal annualRate = new BigDecimal("4.5");

            BigDecimal monthlyInterest = calculator.calculateMonthlyInterest(balance, annualRate);

            assertEquals(new BigDecimal("0.00"), monthlyInterest);
        }

        @Test
        @DisplayName("should throw exception for negative balance")
        void shouldThrowExceptionForNegativeBalance() {
            BigDecimal balance = new BigDecimal("-100000");
            BigDecimal annualRate = new BigDecimal("4.5");

            assertThrows(IllegalArgumentException.class, () ->
                    calculator.calculateMonthlyInterest(balance, annualRate));
        }
    }

    @Nested
    @DisplayName("calculatePrincipalPortion")
    class CalculatePrincipalPortionTests {

        @Test
        @DisplayName("should calculate correct principal portion")
        void shouldCalculateCorrectPrincipalPortion() {
            BigDecimal monthlyPayment = new BigDecimal("2002.31");
            BigDecimal monthlyInterest = new BigDecimal("1350.00");

            BigDecimal principal = calculator.calculatePrincipalPortion(monthlyPayment, monthlyInterest);

            assertEquals(new BigDecimal("652.31"), principal);
        }

        @Test
        @DisplayName("should throw exception for null values")
        void shouldThrowExceptionForNullValues() {
            assertThrows(IllegalArgumentException.class, () ->
                    calculator.calculatePrincipalPortion(null, new BigDecimal("1350.00")));
        }
    }

    @Nested
    @DisplayName("calculateRemainingBalance")
    class CalculateRemainingBalanceTests {

        @Test
        @DisplayName("should calculate correct remaining balance")
        void shouldCalculateCorrectRemainingBalance() {
            BigDecimal previousBalance = new BigDecimal("360000");
            BigDecimal principalPaid = new BigDecimal("652.31");

            BigDecimal remainingBalance = calculator.calculateRemainingBalance(previousBalance, principalPaid);

            assertEquals(new BigDecimal("359347.69"), remainingBalance);
        }

        @Test
        @DisplayName("should not go below zero")
        void shouldNotGoBelowZero() {
            BigDecimal previousBalance = new BigDecimal("100");
            BigDecimal principalPaid = new BigDecimal("200");

            BigDecimal remainingBalance = calculator.calculateRemainingBalance(previousBalance, principalPaid);

            assertEquals(new BigDecimal("0.00"), remainingBalance);
        }
    }

    @Nested
    @DisplayName("getMonthlyRate")
    class GetMonthlyRateTests {

        @Test
        @DisplayName("should calculate correct monthly rate")
        void shouldCalculateCorrectMonthlyRate() {
            BigDecimal annualRate = new BigDecimal("4.5");

            BigDecimal monthlyRate = calculator.getMonthlyRate(annualRate);

            assertEquals(new BigDecimal("0.00375"), monthlyRate.setScale(5, java.math.RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("should return zero for null rate")
        void shouldReturnZeroForNullRate() {
            BigDecimal monthlyRate = calculator.getMonthlyRate(null);

            assertEquals(BigDecimal.ZERO, monthlyRate);
        }
    }

    @Nested
    @DisplayName("calculatePercentageRepaid")
    class CalculatePercentageRepaidTests {

        @Test
        @DisplayName("should calculate correct percentage repaid")
        void shouldCalculateCorrectPercentageRepaid() {
            BigDecimal originalAmount = new BigDecimal("360000");
            BigDecimal amountRepaid = new BigDecimal("36000");

            Double percentage = calculator.calculatePercentageRepaid(originalAmount, amountRepaid);

            assertEquals(10.0, percentage, 0.01);
        }

        @Test
        @DisplayName("should return zero for zero original amount")
        void shouldReturnZeroForZeroOriginalAmount() {
            BigDecimal originalAmount = BigDecimal.ZERO;
            BigDecimal amountRepaid = new BigDecimal("36000");

            Double percentage = calculator.calculatePercentageRepaid(originalAmount, amountRepaid);

            assertEquals(0.0, percentage);
        }
    }

    @Nested
    @DisplayName("calculateTotalInterest")
    class CalculateTotalInterestTests {

        @Test
        @DisplayName("should calculate correct total interest")
        void shouldCalculateCorrectTotalInterest() {
            BigDecimal monthlyPayment = new BigDecimal("2002.31");
            Integer numberOfPayments = 300;
            BigDecimal originalPrincipal = new BigDecimal("360000");

            BigDecimal totalInterest = calculator.calculateTotalInterest(monthlyPayment, numberOfPayments, originalPrincipal);

            assertEquals(new BigDecimal("240693.00"), totalInterest);
        }
    }

    @Nested
    @DisplayName("calculateRemainingPayments")
    class CalculateRemainingPaymentsTests {

        @Test
        @DisplayName("should calculate correct remaining payments")
        void shouldCalculateCorrectRemainingPayments() {
            Integer originalPayments = 300;
            Integer paymentsCompleted = 12;

            Integer remaining = calculator.calculateRemainingPayments(originalPayments, paymentsCompleted);

            assertEquals(288, remaining);
        }

        @Test
        @DisplayName("should not go below zero")
        void shouldNotGoBelowZero() {
            Integer originalPayments = 10;
            Integer paymentsCompleted = 20;

            Integer remaining = calculator.calculateRemainingPayments(originalPayments, paymentsCompleted);

            assertEquals(0, remaining);
        }
    }
}
