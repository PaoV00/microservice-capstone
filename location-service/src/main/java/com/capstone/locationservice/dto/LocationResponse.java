package com.capstone.locationservice.dto;

import com.capstone.locationservice.model.Location;
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

    public LocationResponse(Location location) {
        this.locationId = location.getLocationId();
        this.name = location.getName();
        this.address = location.getAddress().toDto();
    }
}
