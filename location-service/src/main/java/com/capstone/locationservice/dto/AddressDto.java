package com.capstone.locationservice.dto;

import com.capstone.locationservice.model.Address;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {
    @NotBlank private String number;
    @NotBlank private String street;
    @NotBlank private String city;
    @NotBlank private String stateCode;
    @NotBlank private String zip;
    @NotBlank private String countryCode;

    public AddressDto(Address address) {
        this.number = address.getNumber();
        this.street = address.getStreet();
        this.city = address.getCity();
        this.stateCode = address.getStateCode();
        this.zip = address.getZip();
        this.countryCode = address.getCountryCode();
    }
}
