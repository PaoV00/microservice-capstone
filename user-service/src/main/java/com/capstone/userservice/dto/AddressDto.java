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
    private String number;
    private String street;
    private String city;
    private String state;
    private String zip;

    public AddressDto(Address address) {
        this.number = address.getNumber();
        this.street = address.getStreet();
        this.city = address.getCity();
        this.state = address.getState();
        this.zip = address.getZip();
    }
}
