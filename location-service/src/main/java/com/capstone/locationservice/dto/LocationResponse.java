package com.capstone.locationservice.dto;

import com.capstone.locationservice.model.Location;
import com.capstone.locationservice.model.Weather;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LocationResponse {
    private String locationId;
    private String name;
    private AddressDto address;
    private WeatherDto weather;

    public LocationResponse(Location location) {
        this.locationId = location.getLocationId();
        this.name = location.getName();
        this.address = location.getAddress().toDto();
        this.weather = location.getWeather() != null ? location.getWeather().toDto() : null;
    }
}
