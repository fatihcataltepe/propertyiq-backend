package com.propertyiq.portfolio.service;

import com.propertyiq.portfolio.dto.*;
import com.propertyiq.portfolio.exception.InvalidPropertyStateException;
import com.propertyiq.portfolio.exception.PropertyNotFoundException;
import com.propertyiq.portfolio.model.*;
import com.propertyiq.portfolio.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 6, 15);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @Mock
    private PropertyRepository propertyRepository;

    private PropertyService propertyService;

    private UUID userId;
    private UUID propertyId;
    private CreatePropertyRequest createPropertyRequest;
    private Property savedProperty;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyService(propertyRepository, FIXED_CLOCK);
        userId = UUID.randomUUID();
        propertyId = UUID.randomUUID();

        AddressRequest addressRequest = AddressRequest.builder()
                .line1("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country(Country.USA)
                .build();

        createPropertyRequest = CreatePropertyRequest.builder()
                .address(addressRequest)
                .purchasePrice(new BigDecimal("500000"))
                .purchaseDate(LocalDate.of(2024, 1, 15))
                .currency(Currency.USD)
                .propertyType("Single Family")
                .bedrooms(3)
                .bathrooms(2)
                .squareFootage(2000)
                .yearBuilt(2010)
                .description("Beautiful home")
                .build();

        Address address = Address.builder()
                .line1("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country(Country.USA)
                .build();

        savedProperty = Property.builder()
                .id(propertyId)
                .userId(userId)
                .address(address)
                .purchasePrice(new BigDecimal("500000"))
                .purchaseDate(LocalDate.of(2024, 1, 15))
                .currency(Currency.USD)
                .currentValue(new BigDecimal("500000"))
                .propertyType("Single Family")
                .bedrooms(3)
                .bathrooms(2)
                .squareFootage(2000)
                .yearBuilt(2010)
                .description("Beautiful home")
                .status(PropertyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create a new property")
    void createProperty_WithValidRequest_ShouldReturnPropertyResponse() {
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        PropertyResponse response = propertyService.createProperty(userId, createPropertyRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(propertyId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getPurchasePrice()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(response.getCurrency()).isEqualTo(Currency.USD);
        assertThat(response.getCurrencySymbol()).isEqualTo("$");
        assertThat(response.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
        assertThat(response.getAddress().getCity()).isEqualTo("New York");

        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    @DisplayName("Should set currentValue equal to purchasePrice on creation")
    void createProperty_ShouldSetCurrentValueToPurchasePrice() {
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        propertyService.createProperty(userId, createPropertyRequest);

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());

        Property capturedProperty = propertyCaptor.getValue();
        assertThat(capturedProperty.getCurrentValue()).isEqualByComparingTo(capturedProperty.getPurchasePrice());
    }

    @Test
    @DisplayName("Should set status to ACTIVE on creation")
    void createProperty_ShouldSetStatusToActive() {
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        propertyService.createProperty(userId, createPropertyRequest);

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());

        Property capturedProperty = propertyCaptor.getValue();
        assertThat(capturedProperty.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return all properties for user when status is null")
    void getProperties_WithNullStatus_ShouldReturnAllProperties() {
        List<Property> properties = List.of(savedProperty);
        when(propertyRepository.findByUserId(userId)).thenReturn(properties);

        List<PropertyResponse> responses = propertyService.getProperties(userId, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(propertyId);
        verify(propertyRepository).findByUserId(userId);
        verify(propertyRepository, never()).findByUserIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("Should return filtered properties when status is provided")
    void getProperties_WithStatus_ShouldReturnFilteredProperties() {
        List<Property> properties = List.of(savedProperty);
        when(propertyRepository.findByUserIdAndStatus(userId, PropertyStatus.ACTIVE)).thenReturn(properties);

        List<PropertyResponse> responses = propertyService.getProperties(userId, PropertyStatus.ACTIVE);

        assertThat(responses).hasSize(1);
        verify(propertyRepository).findByUserIdAndStatus(userId, PropertyStatus.ACTIVE);
        verify(propertyRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("Should return property by id and userId")
    void getProperty_WithValidIds_ShouldReturnProperty() {
        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));

        PropertyResponse response = propertyService.getProperty(userId, propertyId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(propertyId);
        verify(propertyRepository).findByIdAndUserId(propertyId, userId);
    }

    @Test
    @DisplayName("Should throw PropertyNotFoundException when property not found")
    void getProperty_WithInvalidId_ShouldThrowException() {
        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.getProperty(userId, propertyId))
                .isInstanceOf(PropertyNotFoundException.class);

        verify(propertyRepository).findByIdAndUserId(propertyId, userId);
    }

    @Test
    @DisplayName("Should successfully update property")
    void updateProperty_WithValidRequest_ShouldReturnUpdatedProperty() {
        AddressRequest newAddressRequest = AddressRequest.builder()
                .line1("456 Oak Ave")
                .city("Los Angeles")
                .state("CA")
                .postalCode("90001")
                .country(Country.USA)
                .build();

        UpdatePropertyRequest updateRequest = UpdatePropertyRequest.builder()
                .address(newAddressRequest)
                .propertyType("Condo")
                .bedrooms(2)
                .build();

        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        PropertyResponse response = propertyService.updateProperty(userId, propertyId, updateRequest);

        assertThat(response).isNotNull();
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    @DisplayName("Should throw exception when updating archived property")
    void updateProperty_WithArchivedProperty_ShouldThrowException() {
        savedProperty.setStatus(PropertyStatus.ARCHIVED);
        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));

        UpdatePropertyRequest updateRequest = UpdatePropertyRequest.builder()
                .propertyType("Condo")
                .build();

        assertThatThrownBy(() -> propertyService.updateProperty(userId, propertyId, updateRequest))
                .isInstanceOf(InvalidPropertyStateException.class)
                .hasMessageContaining("archived");

        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    @DisplayName("Should successfully update valuation for active property")
    void updateValuation_WithActiveProperty_ShouldReturnUpdatedProperty() {
        UpdateValuationRequest valuationRequest = UpdateValuationRequest.builder()
                .newValue(new BigDecimal("550000"))
                .source(ValuationSource.USER_INPUT)
                .build();

        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        PropertyResponse response = propertyService.updateValuation(userId, propertyId, valuationRequest);

        assertThat(response).isNotNull();

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());

        Property capturedProperty = propertyCaptor.getValue();
        assertThat(capturedProperty.getCurrentValue()).isEqualByComparingTo(new BigDecimal("550000"));
        assertThat(capturedProperty.getValuationSource()).isEqualTo(ValuationSource.USER_INPUT);
        assertThat(capturedProperty.getLastValuationDate()).isEqualTo(FIXED_DATE);
    }

    @Test
    @DisplayName("Should throw exception when updating valuation for non-active property")
    void updateValuation_WithSoldProperty_ShouldThrowException() {
        savedProperty.setStatus(PropertyStatus.SOLD);
        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));

        UpdateValuationRequest valuationRequest = UpdateValuationRequest.builder()
                .newValue(new BigDecimal("550000"))
                .source(ValuationSource.USER_INPUT)
                .build();

        assertThatThrownBy(() -> propertyService.updateValuation(userId, propertyId, valuationRequest))
                .isInstanceOf(InvalidPropertyStateException.class)
                .hasMessageContaining("non-active");

        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    @DisplayName("Should successfully mark active property as sold")
    void markAsSold_WithActiveProperty_ShouldReturnSoldProperty() {
        MarkAsSoldRequest soldRequest = MarkAsSoldRequest.builder()
                .soldDate(LocalDate.of(2024, 6, 15))
                .soldPrice(new BigDecimal("600000"))
                .build();

        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        PropertyResponse response = propertyService.markAsSold(userId, propertyId, soldRequest);

        assertThat(response).isNotNull();

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());

        Property capturedProperty = propertyCaptor.getValue();
        assertThat(capturedProperty.getStatus()).isEqualTo(PropertyStatus.SOLD);
        assertThat(capturedProperty.getSoldDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(capturedProperty.getSoldPrice()).isEqualByComparingTo(new BigDecimal("600000"));
    }

    @Test
    @DisplayName("Should throw exception when marking non-active property as sold")
    void markAsSold_WithArchivedProperty_ShouldThrowException() {
        savedProperty.setStatus(PropertyStatus.ARCHIVED);
        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));

        MarkAsSoldRequest soldRequest = MarkAsSoldRequest.builder()
                .soldDate(LocalDate.of(2024, 6, 15))
                .soldPrice(new BigDecimal("600000"))
                .build();

        assertThatThrownBy(() -> propertyService.markAsSold(userId, propertyId, soldRequest))
                .isInstanceOf(InvalidPropertyStateException.class)
                .hasMessageContaining("active");

        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    @DisplayName("Should successfully archive active property")
    void archiveProperty_WithActiveProperty_ShouldReturnArchivedProperty() {
        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        PropertyResponse response = propertyService.archiveProperty(userId, propertyId);

        assertThat(response).isNotNull();

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());

        Property capturedProperty = propertyCaptor.getValue();
        assertThat(capturedProperty.getStatus()).isEqualTo(PropertyStatus.ARCHIVED);
    }

    @Test
    @DisplayName("Should throw exception when archiving already archived property")
    void archiveProperty_WithArchivedProperty_ShouldThrowException() {
        savedProperty.setStatus(PropertyStatus.ARCHIVED);
        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));

        assertThatThrownBy(() -> propertyService.archiveProperty(userId, propertyId))
                .isInstanceOf(InvalidPropertyStateException.class)
                .hasMessageContaining("already archived");

        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    @DisplayName("Should archive sold property successfully")
    void archiveProperty_WithSoldProperty_ShouldReturnArchivedProperty() {
        savedProperty.setStatus(PropertyStatus.SOLD);
        when(propertyRepository.findByIdAndUserId(propertyId, userId)).thenReturn(Optional.of(savedProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        PropertyResponse response = propertyService.archiveProperty(userId, propertyId);

        assertThat(response).isNotNull();

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());

        Property capturedProperty = propertyCaptor.getValue();
        assertThat(capturedProperty.getStatus()).isEqualTo(PropertyStatus.ARCHIVED);
    }

    @Test
    @DisplayName("Should set currency from request on property creation")
    void createProperty_ShouldSetCurrencyFromRequest() {
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        propertyService.createProperty(userId, createPropertyRequest);

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());

        Property capturedProperty = propertyCaptor.getValue();
        assertThat(capturedProperty.getCurrency()).isEqualTo(Currency.USD);
    }

    @Test
    @DisplayName("Should create property with UK address and GBP currency")
    void createProperty_WithUKAddress_ShouldSetCorrectCurrency() {
        AddressRequest ukAddressRequest = AddressRequest.builder()
                .line1("10 Downing Street")
                .city("London")
                .postalCode("SW1A 2AA")
                .country(Country.UK)
                .build();

        CreatePropertyRequest ukPropertyRequest = CreatePropertyRequest.builder()
                .address(ukAddressRequest)
                .purchasePrice(new BigDecimal("1000000"))
                .purchaseDate(LocalDate.of(2024, 1, 15))
                .currency(Currency.GBP)
                .propertyType("Townhouse")
                .build();

        Property ukProperty = Property.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .address(Address.builder()
                        .line1("10 Downing Street")
                        .city("London")
                        .postalCode("SW1A 2AA")
                        .country(Country.UK)
                        .build())
                .purchasePrice(new BigDecimal("1000000"))
                .purchaseDate(LocalDate.of(2024, 1, 15))
                .currency(Currency.GBP)
                .currentValue(new BigDecimal("1000000"))
                .status(PropertyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(propertyRepository.save(any(Property.class))).thenReturn(ukProperty);

        PropertyResponse response = propertyService.createProperty(userId, ukPropertyRequest);

        assertThat(response.getCurrency()).isEqualTo(Currency.GBP);
        assertThat(response.getCurrencySymbol()).isEqualTo("£");
    }

    @Test
    @DisplayName("Should create property with Italy address and EUR currency")
    void createProperty_WithItalyAddress_ShouldSetCorrectCurrency() {
        AddressRequest italyAddressRequest = AddressRequest.builder()
                .line1("Via Roma 1")
                .city("Rome")
                .postalCode("00100")
                .country(Country.ITALY)
                .build();

        CreatePropertyRequest italyPropertyRequest = CreatePropertyRequest.builder()
                .address(italyAddressRequest)
                .purchasePrice(new BigDecimal("750000"))
                .purchaseDate(LocalDate.of(2024, 1, 15))
                .currency(Currency.EUR)
                .propertyType("Apartment")
                .build();

        Property italyProperty = Property.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .address(Address.builder()
                        .line1("Via Roma 1")
                        .city("Rome")
                        .postalCode("00100")
                        .country(Country.ITALY)
                        .build())
                .purchasePrice(new BigDecimal("750000"))
                .purchaseDate(LocalDate.of(2024, 1, 15))
                .currency(Currency.EUR)
                .currentValue(new BigDecimal("750000"))
                .status(PropertyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(propertyRepository.save(any(Property.class))).thenReturn(italyProperty);

        PropertyResponse response = propertyService.createProperty(userId, italyPropertyRequest);

        assertThat(response.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(response.getCurrencySymbol()).isEqualTo("€");
    }
}
