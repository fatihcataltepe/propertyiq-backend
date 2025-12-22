package com.propertyiq.portfolio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class MarkAsSoldRequest {

    @NotNull(message = "Sold date is required")
    private LocalDate soldDate;

    @NotNull(message = "Sold price is required")
    @Positive(message = "Sold price must be positive")
    private BigDecimal soldPrice;
}
