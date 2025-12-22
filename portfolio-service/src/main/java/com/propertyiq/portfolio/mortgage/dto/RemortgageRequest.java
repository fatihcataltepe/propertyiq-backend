package com.propertyiq.portfolio.mortgage.dto;

import com.propertyiq.portfolio.mortgage.model.MortgageType;
import com.propertyiq.portfolio.mortgage.model.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemortgageRequest {

    @NotBlank(message = "Lender name is required")
    private String lender;

    @NotNull(message = "New loan amount is required")
    @DecimalMin(value = "0.01", message = "Loan amount must be greater than 0")
    private BigDecimal newLoanAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    private BigDecimal interestRate;

    @NotNull(message = "Term years is required")
    @Min(value = 1, message = "Term must be at least 1 year")
    private Integer termYears;

    @NotNull(message = "Mortgage type is required")
    private MortgageType mortgageType;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private String notes;

    private Boolean releaseEquity;

    private BigDecimal equityReleaseAmount;
}
