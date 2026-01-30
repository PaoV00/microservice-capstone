package com.capstone.locationservice.service;

import com.capstone.locationservice.dto.LocationRequest;
import com.capstone.locationservice.dto.LocationResponse;
import com.capstone.locationservice.exceptions.DuplicateException;
import com.capstone.locationservice.exceptions.NotFoundException;
import com.capstone.locationservice.model.Address;
import com.capstone.locationservice.model.Location;
import com.capstone.locationservice.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationService {

    private final LocationRepository repository;

    public LocationResponse create(LocationRequest req) {
        if(repository.existsByAddress_CityIgnoreCaseAndAddress_StateCodeIgnoreCaseAndAddress_CountryCodeIgnoreCase(
                req.getAddress().getCity(), req.getAddress().getStateCode(),
                req.getAddress().getCountryCode()
        )){
            throw new DuplicateException("Location already exists");
        }
        Address newAddress = buildAddress(req);

        Location location = Location.builder()
                .locationId(UUID.randomUUID().toString())
                .name(req.getName() != null ? req.getName() : null)
                .build();
        newAddress.setLocationId(location.getLocationId());
        location.setAddress(newAddress);
        location.normalize();

        repository.save(location);
        return toResponse(location);
    }

    public Location createLocation(String city, String stateCode, String countryCode) {
        Address newAddress = new Address();
        newAddress.setCity(city);
        newAddress.setStateCode(stateCode);
        newAddress.setCountryCode(countryCode);
        newAddress.normalize();

        Location newLocation = Location.builder()
                .locationId(UUID.randomUUID().toString())
                .name(null)
                .build();
        newAddress.setLocationId(newLocation.getLocationId());
        newLocation.setAddress(newAddress);
        newLocation.normalize();
        repository.save(newLocation);
        return newLocation;
    }

    public LocationResponse get(String id) {
        Location loc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));
        return toResponse(loc);
    }

    public String getLocationIdIfDontExistCreateNewLocation(String city, String stateCode, String countryCode) {

        // 1) Try to find existing location
        Optional<Location> existing = repository
                .findFirstByAddress_CityIgnoreCaseAndAddress_StateCodeIgnoreCaseAndAddress_CountryCodeIgnoreCase(
                        city, stateCode, countryCode);

        if (existing.isPresent()) {
            log.info("Returning existing location id: {}", existing.get().getLocationId());
            return existing.get().getLocationId();
        }

        // 2) Not found — build new Location (but do not assume save will succeed)
        Location newLocation = createLocation(city, stateCode, countryCode);

        try {
            repository.save(newLocation);
            log.info("Created new location id: {}", newLocation.getLocationId());
            return newLocation.getLocationId();
        } catch (Exception ex) {
            // Race: another request created it first. Re-query and return that id.
            Optional<Location> raced = repository
                    .findFirstByAddress_CityIgnoreCaseAndAddress_StateCodeIgnoreCaseAndAddress_CountryCodeIgnoreCase(
                            city, stateCode, countryCode);
            if (raced.isPresent()) {
                log.info("Race detected, returning existing location id: {}", raced.get().getLocationId());
                return raced.get().getLocationId();
            }
            // Unexpected:
            throw new DuplicateException("Location already exists");
        }
    }

    public LocationResponse update(String id, Map<String, Object> updates) {
        Location location = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));

        if(updates.containsKey("name")) {
            location.setName(updates.get("name").toString());
            log.info("Location name updated: {}", location.getName());
        }
        if(updates.containsKey("address")) {
            Map<String, Object> addrUpdates = (Map<String, Object>) updates.get("address");

            Address address = location.getAddress();

            if(addrUpdates.containsKey("city")) address.setCity(addrUpdates.get("city").toString());
            if(addrUpdates.containsKey("stateCode")) address.setStateCode(addrUpdates.get("stateCode").toString());
            if(addrUpdates.containsKey("countryCode")) address.setCountryCode(addrUpdates.get("countryCode").toString());
        }

        repository.save(location);
        return toResponse(location);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Location not found");
        }
        repository.deleteById(id);
    }

    private LocationResponse toResponse(Location loc) {
        return LocationResponse.builder()
                .locationId(loc.getLocationId())
                .name(loc.getName() != null ? loc.getName() : null)
                .address(loc.getAddress().toDto())
                .build();
    }

    // Builds address with all fields except locationId
    private Address buildAddress(LocationRequest request) {
        Address address = Address.builder()
                .city(request.getAddress().getCity())
                .stateCode(request.getAddress().getStateCode())
                .countryCode(request.getAddress().getCountryCode())
                .build();
        address.normalize();

        return address;
    }
}