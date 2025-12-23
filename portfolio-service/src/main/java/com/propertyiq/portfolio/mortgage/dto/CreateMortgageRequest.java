package com.propertyiq.portfolio.mortgage.dto;

import com.propertyiq.portfolio.mortgage.model.MortgageType;
import com.propertyiq.portfolio.mortgage.model.ProductType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMortgageRequest {

    @NotBlank(message = "Lender is required")
    @Size(max = 255, message = "Lender name must not exceed 255 characters")
    private String lender;

    @NotNull(message = "Original loan amount is required")
    @DecimalMin(value = "1.00", message = "Loan amount must be greater than 0")
    private BigDecimal originalLoanAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be positive")
    @DecimalMax(value = "30.00", message = "Interest rate cannot exceed 30%")
    private BigDecimal interestRate;

    @NotNull(message = "Term years is required")
    @Min(value = 1, message = "Term must be at least 1 year")
    @Max(value = 40, message = "Term cannot exceed 40 years")
    private Integer termYears;

    @NotNull(message = "Mortgage type is required")
    private MortgageType mortgageType;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate startDate;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
