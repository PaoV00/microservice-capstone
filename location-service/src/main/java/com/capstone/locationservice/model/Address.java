package com.capstone.locationservice.model;

import com.capstone.locationservice.dto.AddressDto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @NotBlank
    private String number;
    @NotBlank
    private String street;
    @NotBlank
    private String city;
    @NotBlank
    private String stateCode;
    @NotBlank
    private String zip;
    @NotBlank
    private String countryCode;

    public void normalize() {
        number = normalize(number);
        street = normalize(street);
        city = normalize(city);
        stateCode = normalize(stateCode);
        zip = normalize(zip);
        countryCode = normalize(countryCode);
    }

    private String normalize(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }

    public AddressDto toDto() {
        return AddressDto.builder()
                .number(this.number)
                .street(this.street)
                .city(this.city)
                .stateCode(this.stateCode)
                .zip(this.zip)
                .countryCode(this.countryCode)
                .build();
    }
}
