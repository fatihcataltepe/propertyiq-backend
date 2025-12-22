package com.propertyiq.portfolio.mortgage.service;

import com.propertyiq.portfolio.mortgage.dto.MortgagePaymentResponse;
import com.propertyiq.portfolio.mortgage.dto.RecordPaymentRequest;
import com.propertyiq.portfolio.mortgage.exception.MortgageNotFoundException;
import com.propertyiq.portfolio.mortgage.model.Mortgage;
import com.propertyiq.portfolio.mortgage.model.MortgagePayment;
import com.propertyiq.portfolio.mortgage.model.PaymentSource;
import com.propertyiq.portfolio.mortgage.model.PaymentStatus;
import com.propertyiq.portfolio.mortgage.model.PaymentType;
import com.propertyiq.portfolio.mortgage.repository.MortgagePaymentRepository;
import com.propertyiq.portfolio.mortgage.repository.MortgageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MortgagePaymentService {

    private final MortgagePaymentRepository paymentRepository;
    private final MortgageRepository mortgageRepository;
    private final MortgageCalculator mortgageCalculator;

    @Transactional
    public MortgagePaymentResponse recordPayment(UUID userId, UUID mortgageId, RecordPaymentRequest request) {
        Mortgage mortgage = findMortgageByIdAndUserId(mortgageId, userId);

        BigDecimal expectedMonthlyPayment = mortgage.getMonthlyPayment();
        BigDecimal paymentAmount = request.getAmount();

        PaymentType paymentType = determinePaymentType(paymentAmount, expectedMonthlyPayment, request.getTopupReason());

        BigDecimal currentBalance = mortgage.getCurrentBalance();
        BigDecimal monthlyInterest = mortgageCalculator.calculateMonthlyInterest(
                currentBalance, mortgage.getInterestRate());

        BigDecimal principalPortion;
        BigDecimal interestPortion;

        if (paymentType == PaymentType.TOPUP) {
            principalPortion = paymentAmount;
            interestPortion = BigDecimal.ZERO;
        } else {
            interestPortion = monthlyInterest.min(paymentAmount);
            principalPortion = paymentAmount.subtract(interestPortion).max(BigDecimal.ZERO);
        }

        BigDecimal newBalance = mortgageCalculator.calculateRemainingBalance(currentBalance, principalPortion);

        Integer nextPaymentNumber = paymentRepository.findMaxPaymentNumber(mortgageId) + 1;

        MortgagePayment payment = MortgagePayment.builder()
                .mortgageId(mortgageId)
                .paymentNumber(paymentType == PaymentType.SCHEDULED ? nextPaymentNumber : null)
                .paymentType(paymentType)
                .source(PaymentSource.USER_INITIATED)
                .dueDate(request.getPaymentDate())
                .actualPaymentDate(request.getPaymentDate())
                .principal(principalPortion)
                .interest(interestPortion)
                .totalAmount(paymentAmount)
                .balanceBefore(currentBalance)
                .balanceAfter(newBalance)
                .status(PaymentStatus.PAID)
                .topupReason(request.getTopupReason())
                .build();

        MortgagePayment savedPayment = paymentRepository.save(payment);

        mortgage.setCurrentBalance(newBalance);
        mortgage.setPrincipalPaidToDate(mortgage.getPrincipalPaidToDate().add(principalPortion));
        mortgage.setInterestPaidToDate(mortgage.getInterestPaidToDate().add(interestPortion));
        mortgageRepository.save(mortgage);

        return MortgagePaymentResponse.fromEntity(savedPayment);
    }

    @Transactional(readOnly = true)
    public Page<MortgagePaymentResponse> getPaymentHistory(UUID userId, UUID mortgageId, Pageable pageable) {
        findMortgageByIdAndUserId(mortgageId, userId);

        return paymentRepository.findByMortgageIdOrderByDueDateDesc(mortgageId, pageable)
                .map(MortgagePaymentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<MortgagePaymentResponse> getAllPayments(UUID userId, UUID mortgageId) {
        findMortgageByIdAndUserId(mortgageId, userId);

        return paymentRepository.findByMortgageIdOrderByDueDateDesc(mortgageId)
                .stream()
                .map(MortgagePaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MortgagePaymentResponse> getTopUpPayments(UUID userId, UUID mortgageId) {
        findMortgageByIdAndUserId(mortgageId, userId);

        return paymentRepository.findTopUpPaymentsByMortgageId(mortgageId)
                .stream()
                .map(MortgagePaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MortgagePayment generateScheduledPayment(Mortgage mortgage, LocalDate dueDate) {
        if (paymentRepository.existsByMortgageIdAndDueDateAndPaymentType(
                mortgage.getId(), dueDate, PaymentType.SCHEDULED)) {
            return null;
        }

        BigDecimal currentBalance = mortgage.getCurrentBalance();
        BigDecimal monthlyInterest = mortgageCalculator.calculateMonthlyInterest(
                currentBalance, mortgage.getInterestRate());
        BigDecimal principalPortion = mortgageCalculator.calculatePrincipalPortion(
                mortgage.getMonthlyPayment(), monthlyInterest);

        Integer nextPaymentNumber = paymentRepository.findMaxPaymentNumber(mortgage.getId()) + 1;

        MortgagePayment payment = MortgagePayment.builder()
                .mortgageId(mortgage.getId())
                .paymentNumber(nextPaymentNumber)
                .paymentType(PaymentType.SCHEDULED)
                .source(PaymentSource.SYSTEM_GENERATED)
                .dueDate(dueDate)
                .principal(principalPortion)
                .interest(monthlyInterest)
                .totalAmount(mortgage.getMonthlyPayment())
                .balanceBefore(currentBalance)
                .status(PaymentStatus.SCHEDULED)
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public void markPaymentAsPaid(UUID paymentId, LocalDate actualPaymentDate) {
        MortgagePayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SCHEDULED) {
            return;
        }

        Mortgage mortgage = mortgageRepository.findById(payment.getMortgageId())
                .orElseThrow(() -> new MortgageNotFoundException(payment.getMortgageId().toString()));

        BigDecimal newBalance = mortgageCalculator.calculateRemainingBalance(
                payment.getBalanceBefore(), payment.getPrincipal());

        payment.setStatus(PaymentStatus.PAID);
        payment.setActualPaymentDate(actualPaymentDate);
        payment.setBalanceAfter(newBalance);
        paymentRepository.save(payment);

        mortgage.setCurrentBalance(newBalance);
        mortgage.setPrincipalPaidToDate(mortgage.getPrincipalPaidToDate().add(payment.getPrincipal()));
        mortgage.setInterestPaidToDate(mortgage.getInterestPaidToDate().add(payment.getInterest()));
        mortgageRepository.save(mortgage);
    }

    @Transactional
    public int markOverduePayments(LocalDate asOfDate) {
        List<MortgagePayment> overduePayments = paymentRepository.findOverduePayments(asOfDate);
        int count = 0;

        for (MortgagePayment payment : overduePayments) {
            payment.setStatus(PaymentStatus.MISSED);
            paymentRepository.save(payment);
            count++;
        }

        return count;
    }

    private PaymentType determinePaymentType(BigDecimal paymentAmount, BigDecimal expectedPayment, String topupReason) {
        if (topupReason != null && !topupReason.isBlank()) {
            return PaymentType.TOPUP;
        }

        BigDecimal tolerance = expectedPayment.multiply(new BigDecimal("0.05"));
        BigDecimal lowerBound = expectedPayment.subtract(tolerance);
        BigDecimal upperBound = expectedPayment.add(tolerance);

        if (paymentAmount.compareTo(lowerBound) >= 0 && paymentAmount.compareTo(upperBound) <= 0) {
            return PaymentType.SCHEDULED;
        }

        return PaymentType.TOPUP;
    }

    private Mortgage findMortgageByIdAndUserId(UUID mortgageId, UUID userId) {
        return mortgageRepository.findByIdAndUserId(mortgageId, userId)
                .orElseThrow(() -> new MortgageNotFoundException(mortgageId.toString()));
    }
}
