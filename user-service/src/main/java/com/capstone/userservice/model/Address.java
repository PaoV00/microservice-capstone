package com.capstone.userservice.model;

import com.capstone.userservice.dto.AddressDto;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String locationId;
    private String number;
    private String street;
    private String city;
    private String stateCode;
    private String countryCode;
    private String zip;

    public Address(AddressDto addressDto) {
        this.number = addressDto.getNumber().trim().toUpperCase();
        this.street = addressDto.getStreet().trim().toUpperCase();
        this.city = addressDto.getCity().trim().toUpperCase();
        this.stateCode = addressDto.getStateCode().trim().toUpperCase();
        this.countryCode = addressDto.getCountryCode().trim().toUpperCase();
        this.zip = addressDto.getZip().trim().toUpperCase();
    }

    public AddressDto toDto() {
        return AddressDto.builder()
                .number(this.number)
                .street(this.street)
                .city(this.city)
                .stateCode(this.stateCode)
                .zip(this.zip)
                .countryCode(this.countryCode)
                .locationId(this.locationId)
                .build();
    }
}

