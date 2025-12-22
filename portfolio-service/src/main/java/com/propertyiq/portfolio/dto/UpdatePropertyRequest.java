package com.propertyiq.portfolio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePropertyRequest {

    @Valid
    private AddressRequest address;

    @Size(max = 50, message = "Property type must not exceed 50 characters")
    private String propertyType;

    private Integer bedrooms;

    private Integer bathrooms;

    private Integer squareFootage;

    private Integer yearBuilt;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
