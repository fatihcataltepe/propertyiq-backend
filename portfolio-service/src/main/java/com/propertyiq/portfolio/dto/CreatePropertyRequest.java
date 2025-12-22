package com.propertyiq.portfolio.dto;

import com.propertyiq.portfolio.model.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class CreatePropertyRequest {

    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;

    @NotNull(message = "Purchase price is required")
    @Positive(message = "Purchase price must be positive")
    private BigDecimal purchasePrice;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @Size(max = 50, message = "Property type must not exceed 50 characters")
    private String propertyType;

    private Integer bedrooms;

    private Integer bathrooms;

    private Integer squareFootage;

    private Integer yearBuilt;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
