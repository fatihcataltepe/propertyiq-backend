package com.propertyiq.portfolio.mortgage.service;

import com.propertyiq.portfolio.mortgage.dto.CreateMortgageRequest;
import com.propertyiq.portfolio.mortgage.dto.MortgageResponse;
import com.propertyiq.portfolio.mortgage.dto.MortgageSummary;
import com.propertyiq.portfolio.mortgage.exception.MortgageNotFoundException;
import com.propertyiq.portfolio.mortgage.model.Mortgage;
import com.propertyiq.portfolio.mortgage.repository.MortgageRepository;
import com.propertyiq.portfolio.exception.PropertyNotFoundException;
import com.propertyiq.portfolio.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MortgageService {

    private final MortgageRepository mortgageRepository;
    private final PropertyRepository propertyRepository;
    private final MortgageCalculator mortgageCalculator;

    @Transactional
    public MortgageResponse createMortgage(UUID userId, UUID propertyId, CreateMortgageRequest request) {
        if (!propertyRepository.existsByIdAndUserId(propertyId, userId)) {
            throw new PropertyNotFoundException(propertyId.toString());
        }

        BigDecimal monthlyPayment = mortgageCalculator.calculateMonthlyPayment(
                request.getOriginalLoanAmount(),
                request.getInterestRate(),
                request.getTermYears()
        );

        Integer sequenceNumber = mortgageRepository.findNextSequenceNumber(propertyId);

        Mortgage mortgage = Mortgage.builder()
                .userId(userId)
                .propertyId(propertyId)
                .sequenceNumber(sequenceNumber)
                .lender(request.getLender())
                .originalLoanAmount(request.getOriginalLoanAmount())
                .interestRate(request.getInterestRate())
                .termYears(request.getTermYears())
                .mortgageType(request.getMortgageType())
                .productType(request.getProductType())
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusYears(request.getTermYears()))
                .currentBalance(request.getOriginalLoanAmount())
                .monthlyPayment(monthlyPayment)
                .notes(request.getNotes())
                .isActive(true)
                .principalPaidToDate(BigDecimal.ZERO)
                .interestPaidToDate(BigDecimal.ZERO)
                .build();

        Mortgage savedMortgage = mortgageRepository.save(mortgage);
        return MortgageResponse.fromEntity(savedMortgage);
    }

    @Transactional(readOnly = true)
    public List<MortgageResponse> getMortgagesForProperty(UUID userId, UUID propertyId) {
        if (!propertyRepository.existsByIdAndUserId(propertyId, userId)) {
            throw new PropertyNotFoundException(propertyId.toString());
        }

        return mortgageRepository.findByPropertyIdOrderBySequenceNumber(propertyId)
                .stream()
                .map(MortgageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MortgageResponse> getActiveMortgagesForProperty(UUID userId, UUID propertyId) {
        if (!propertyRepository.existsByIdAndUserId(propertyId, userId)) {
            throw new PropertyNotFoundException(propertyId.toString());
        }

        return mortgageRepository.findByPropertyIdAndIsActiveTrue(propertyId)
                .stream()
                .map(MortgageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MortgageResponse getMortgage(UUID userId, UUID mortgageId) {
        Mortgage mortgage = findMortgageByIdAndUserId(mortgageId, userId);
        return MortgageResponse.fromEntity(mortgage);
    }

    @Transactional(readOnly = true)
    public MortgageSummary getMortgageSummary(UUID userId, UUID mortgageId) {
        Mortgage mortgage = findMortgageByIdAndUserId(mortgageId, userId);
        return MortgageSummary.fromEntity(mortgage);
    }

    @Transactional(readOnly = true)
    public List<MortgageResponse> getAllMortgagesForUser(UUID userId) {
        return mortgageRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(MortgageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MortgageResponse> getActiveMortgagesForUser(UUID userId) {
        return mortgageRepository.findByUserIdAndIsActiveTrueOrderBySequenceNumber(userId)
                .stream()
                .map(MortgageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private Mortgage findMortgageByIdAndUserId(UUID mortgageId, UUID userId) {
        return mortgageRepository.findByIdAndUserId(mortgageId, userId)
                .orElseThrow(() -> new MortgageNotFoundException(mortgageId.toString()));
    }
}
