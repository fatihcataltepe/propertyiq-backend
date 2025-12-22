package com.propertyiq.portfolio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(name = "address_line_1", nullable = false, length = 255)
    private String line1;

    @Column(name = "address_line_2", length = 255)
    private String line2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Country country;

    public String getFullAddress() {
        return String.format("%s%s, %s%s, %s, %s",
                line1,
                line2 != null ? ", " + line2 : "",
                city,
                state != null ? ", " + state : "",
                postalCode,
                country);
    }

    public String getDisplayAddress() {
        switch (country) {
            case UK:
                return String.format("%s, %s %s, %s", line1, city, postalCode, country);
            case USA:
                return String.format("%s, %s, %s %s", line1, city, state, postalCode);
            case ITALY:
                return String.format("%s, %s %s, %s", line1, postalCode, city, country);
            default:
                return getFullAddress();
        }
    }
}
