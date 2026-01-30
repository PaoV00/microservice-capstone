package com.capstone.userservice.dto;

import com.capstone.userservice.model.Address;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddressDto {
    private String locationId;
    private String number;
    private String street;
    private String city;
    private String stateCode;
    private String zip;
    private String countryCode;

    public Address toAddress() {
        return Address.builder()
                .locationId(locationId != null ? locationId : null)
                .number(number)
                .street(street)
                .city(city)
                .stateCode(stateCode)
                .countryCode(countryCode)
                .zip(zip)
                .build();
    }
}
