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
    @NotBlank private String city;
    @NotBlank private String stateCode;
    @NotBlank private String countryCode;
    private String locationId;

    public AddressDto(Address address) {
        this.city = address.getCity();
        this.stateCode = address.getStateCode();
        this.countryCode = address.getCountryCode();
        this.locationId = address.getLocationId();
    }
}
