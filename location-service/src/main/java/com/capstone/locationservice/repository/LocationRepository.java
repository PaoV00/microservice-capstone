package com.capstone.locationservice.repository;

import com.capstone.locationservice.model.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LocationRepository extends MongoRepository<Location, String> {

    Boolean existsByAddress_CityIgnoreCaseAndAddress_StateCodeIgnoreCaseAndAddress_CountryCodeIgnoreCase(
            String city, String stateCode, String countryCode
    );

    Location findByAddress_CityIgnoreCaseAndAddress_StateCodeIgnoreCaseAndAddress_CountryCodeIgnoreCase(String city, String stateCode, String countryCode);

    Optional<Location> findFirstByAddress_CityIgnoreCaseAndAddress_StateCodeIgnoreCaseAndAddress_CountryCodeIgnoreCase(
            String city, String stateCode, String countryCode);

}
