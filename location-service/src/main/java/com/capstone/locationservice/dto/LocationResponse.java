package com.capstone.locationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationResponse {
    private String locationId;
    private String name;
    private AddressDto address;
    private WeatherDto weather;
}
