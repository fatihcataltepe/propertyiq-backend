package com.propertyiq.portfolio.dto;

import com.propertyiq.portfolio.model.ValuationSource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateValuationRequest {

    @NotNull(message = "New value is required")
    @Positive(message = "New value must be positive")
    private BigDecimal newValue;

    @NotNull(message = "Valuation source is required")
    private ValuationSource source;
}
