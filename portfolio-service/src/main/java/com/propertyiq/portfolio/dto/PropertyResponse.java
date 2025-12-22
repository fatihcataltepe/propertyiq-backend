package com.propertyiq.portfolio.dto;

import com.propertyiq.portfolio.model.Currency;
import com.propertyiq.portfolio.model.Property;
import com.propertyiq.portfolio.model.PropertyStatus;
import com.propertyiq.portfolio.model.ValuationSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponse {

    private UUID id;
    private UUID userId;
    private AddressResponse address;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private Currency currency;
    private String currencySymbol;
    private BigDecimal currentValue;
    private LocalDate lastValuationDate;
    private ValuationSource valuationSource;
    private PropertyStatus status;
    private LocalDate soldDate;
    private BigDecimal soldPrice;
    private String propertyType;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer squareFootage;
    private Integer yearBuilt;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PropertyResponse fromEntity(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .userId(property.getUserId())
                .address(AddressResponse.fromEntity(property.getAddress()))
                .purchasePrice(property.getPurchasePrice())
                .purchaseDate(property.getPurchaseDate())
                .currency(property.getCurrency())
                .currencySymbol(property.getCurrency() != null ? property.getCurrency().getSymbol() : "")
                .currentValue(property.getCurrentValue())
                .lastValuationDate(property.getLastValuationDate())
                .valuationSource(property.getValuationSource())
                .status(property.getStatus())
                .soldDate(property.getSoldDate())
                .soldPrice(property.getSoldPrice())
                .propertyType(property.getPropertyType())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .squareFootage(property.getSquareFootage())
                .yearBuilt(property.getYearBuilt())
                .description(property.getDescription())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }
}
