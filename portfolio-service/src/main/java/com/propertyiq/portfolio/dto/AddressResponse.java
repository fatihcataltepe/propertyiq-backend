package com.propertyiq.portfolio.dto;

import com.propertyiq.portfolio.model.Address;
import com.propertyiq.portfolio.model.Country;
import com.propertyiq.portfolio.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private Country country;
    private Currency currency;
    private String fullAddress;
    private String displayAddress;
    private String currencySymbol;

    public static AddressResponse fromEntity(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponse.builder()
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .currency(address.getCurrency())
                .fullAddress(address.getFullAddress())
                .displayAddress(address.getDisplayAddress())
                .currencySymbol(address.getCurrencySymbol())
                .build();
    }
}
