package com.capstone.locationservice.model;

import com.capstone.locationservice.dto.AddressDto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @NotBlank
    private String city;
    @NotBlank
    private String stateCode;
    @NotBlank
    private String countryCode;
    @NotBlank
    private String locationId;

    public void normalize() {
        city = normalize(city);
        stateCode = normalize(stateCode);
        countryCode = normalize(countryCode);
        locationId = normalize(locationId);
    }

    private String normalize(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }

    public AddressDto toDto() {
        return AddressDto.builder()
                .city(this.city)
                .stateCode(this.stateCode)
                .countryCode(this.countryCode)
                .locationId(this.locationId)
                .build();
    }
}
