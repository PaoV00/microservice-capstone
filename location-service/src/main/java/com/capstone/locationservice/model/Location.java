package com.capstone.locationservice.model;

import jakarta.validation.Valid;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "location")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(
        name = "unique_location",
        def = "{'address.city': 1, 'address.stateCode': 1, 'address.countryCode': 1}",
        unique = true
)
public class Location {

    @Id
    private String locationId;
    private String name;

    @Valid
    @Indexed(unique = true)
    private Address address;

    public void normalize() {
        if (name != null) name = name.trim();
        if (address != null) address.normalize();
    }
}
