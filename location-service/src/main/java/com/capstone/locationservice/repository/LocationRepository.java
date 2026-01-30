package com.capstone.locationservice.repository;

import com.capstone.locationservice.model.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationRepository extends MongoRepository<Location, String> {

    Boolean existsByAddress_CityAndAddress_StateCodeAndAddress_CountryCode(
            String city, String stateCode, String countryCode
    );

    Location findByAddress_CityAndAddress_StateCodeAndAddress_CountryCode(String city, String stateCode, String countryCode);


}
