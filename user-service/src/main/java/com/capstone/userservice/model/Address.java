package com.capstone.userservice.model;

import com.capstone.userservice.dto.AddressDto;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@Builder
public class Address {

    private String number;
    private String street;
    private String city;
    private String state;
    private String zip;

    public Address(String number, String street, String city, String state, String zip) {
        this.number = number.trim().toUpperCase();
        this.street = street.trim().toUpperCase();
        this.city = city.trim().toUpperCase();
        this.state = state.trim().toUpperCase();
        this.zip = zip.trim().toUpperCase();
    }

    public Address(AddressDto addressDto) {
        this.number = addressDto.getNumber().trim().toUpperCase();
        this.street = addressDto.getStreet().trim().toUpperCase();
        this.city = addressDto.getCity().trim().toUpperCase();
        this.state = addressDto.getState().trim().toUpperCase();
        this.zip = addressDto.getZip().trim().toUpperCase();
    }

}

