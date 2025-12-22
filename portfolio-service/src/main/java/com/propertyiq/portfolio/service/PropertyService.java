package com.propertyiq.portfolio.service;

import com.propertyiq.portfolio.dto.*;
import com.propertyiq.portfolio.exception.InvalidPropertyStateException;
import com.propertyiq.portfolio.exception.PropertyNotFoundException;
import com.propertyiq.portfolio.model.*;
import com.propertyiq.portfolio.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Transactional
    public PropertyResponse createProperty(UUID userId, CreatePropertyRequest request) {
        Address address = Address.builder()
                .line1(request.getAddress().getLine1())
                .line2(request.getAddress().getLine2())
                .city(request.getAddress().getCity())
                .state(request.getAddress().getState())
                .postalCode(request.getAddress().getPostalCode())
                .country(request.getAddress().getCountry())
                .currency(request.getAddress().getCurrency())
                .build();

        Property property = Property.builder()
                .userId(userId)
                .address(address)
                .purchasePrice(request.getPurchasePrice())
                .purchaseDate(request.getPurchaseDate())
                .currentValue(request.getPurchasePrice())
                .propertyType(request.getPropertyType())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .squareFootage(request.getSquareFootage())
                .yearBuilt(request.getYearBuilt())
                .description(request.getDescription())
                .status(PropertyStatus.ACTIVE)
                .build();

        Property savedProperty = propertyRepository.save(property);
        return PropertyResponse.fromEntity(savedProperty);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getProperties(UUID userId, PropertyStatus status) {
        List<Property> properties;
        if (status != null) {
            properties = propertyRepository.findByUserIdAndStatus(userId, status);
        } else {
            properties = propertyRepository.findByUserId(userId);
        }
        return properties.stream()
                .map(PropertyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropertyResponse getProperty(UUID userId, UUID propertyId) {
        Property property = findPropertyByIdAndUserId(propertyId, userId);
        return PropertyResponse.fromEntity(property);
    }

    @Transactional
    public PropertyResponse updateProperty(UUID userId, UUID propertyId, UpdatePropertyRequest request) {
        Property property = findPropertyByIdAndUserId(propertyId, userId);

        if (property.getStatus() == PropertyStatus.ARCHIVED) {
            throw new InvalidPropertyStateException("Cannot update an archived property");
        }

        if (request.getAddress() != null) {
            Address address = Address.builder()
                    .line1(request.getAddress().getLine1())
                    .line2(request.getAddress().getLine2())
                    .city(request.getAddress().getCity())
                    .state(request.getAddress().getState())
                    .postalCode(request.getAddress().getPostalCode())
                    .country(request.getAddress().getCountry())
                    .currency(request.getAddress().getCurrency())
                    .build();
            property.setAddress(address);
        }

        if (request.getPropertyType() != null) {
            property.setPropertyType(request.getPropertyType());
        }
        if (request.getBedrooms() != null) {
            property.setBedrooms(request.getBedrooms());
        }
        if (request.getBathrooms() != null) {
            property.setBathrooms(request.getBathrooms());
        }
        if (request.getSquareFootage() != null) {
            property.setSquareFootage(request.getSquareFootage());
        }
        if (request.getYearBuilt() != null) {
            property.setYearBuilt(request.getYearBuilt());
        }
        if (request.getDescription() != null) {
            property.setDescription(request.getDescription());
        }

        Property savedProperty = propertyRepository.save(property);
        return PropertyResponse.fromEntity(savedProperty);
    }

    @Transactional
    public PropertyResponse updateValuation(UUID userId, UUID propertyId, UpdateValuationRequest request) {
        Property property = findPropertyByIdAndUserId(propertyId, userId);

        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new InvalidPropertyStateException("Cannot update valuation for a non-active property");
        }

        property.setCurrentValue(request.getNewValue());
        property.setLastValuationDate(LocalDate.now());
        property.setValuationSource(request.getSource());

        Property savedProperty = propertyRepository.save(property);
        return PropertyResponse.fromEntity(savedProperty);
    }

    @Transactional
    public PropertyResponse markAsSold(UUID userId, UUID propertyId, MarkAsSoldRequest request) {
        Property property = findPropertyByIdAndUserId(propertyId, userId);

        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new InvalidPropertyStateException("Only active properties can be marked as sold");
        }

        property.setStatus(PropertyStatus.SOLD);
        property.setSoldDate(request.getSoldDate());
        property.setSoldPrice(request.getSoldPrice());

        Property savedProperty = propertyRepository.save(property);
        return PropertyResponse.fromEntity(savedProperty);
    }

    @Transactional
    public PropertyResponse archiveProperty(UUID userId, UUID propertyId) {
        Property property = findPropertyByIdAndUserId(propertyId, userId);

        if (property.getStatus() == PropertyStatus.ARCHIVED) {
            throw new InvalidPropertyStateException("Property is already archived");
        }

        property.setStatus(PropertyStatus.ARCHIVED);

        Property savedProperty = propertyRepository.save(property);
        return PropertyResponse.fromEntity(savedProperty);
    }

    private Property findPropertyByIdAndUserId(UUID propertyId, UUID userId) {
        return propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId.toString()));
    }
}
