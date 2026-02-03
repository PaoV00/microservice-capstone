package com.capstone.locationservice.service;

import com.capstone.locationservice.dto.LocationRequest;
import com.capstone.locationservice.dto.LocationResponse;
import com.capstone.locationservice.dto.WeatherDto;
import com.capstone.locationservice.exceptions.DuplicateException;
import com.capstone.locationservice.exceptions.NotFoundException;
import com.capstone.locationservice.model.Address;
import com.capstone.locationservice.model.Location;
import com.capstone.locationservice.model.Weather;
import com.capstone.locationservice.repository.LocationRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationService {

    private final LocationRepository repository;
    private final WebClient.Builder webClient;
    private WebClient builtWebClient;

    @PostConstruct
    public void init() {
        this.builtWebClient = webClient.build();
    }

    public LocationResponse create(LocationRequest req) {
        if(repository.existsByAddress_CityIgnoreCaseAndAddress_StateCodeIgnoreCaseAndAddress_CountryCodeIgnoreCase(
                req.getAddress().getCity(), req.getAddress().getStateCode(),
                req.getAddress().getCountryCode()
        )){
            throw new DuplicateException("Location already exists");
        }
        Address newAddress = buildAddress(req);
        log.info("Creating location for: {}, {}, {}", newAddress.getCity(), newAddress.getStateCode(), newAddress.getCountryCode());
        Weather weather = getWeather(newAddress.getCity(), newAddress.getStateCode(), newAddress.getCountryCode());
        log.info("Fetched weather: {}", weather);
        Location location = Location.builder()
                .locationId(UUID.randomUUID().toString())
                .name(req.getName() != null ? req.getName() : null)
                .weather(weather)
                .build();
        newAddress.setLocationId(location.getLocationId());
        location.setAddress(newAddress);
        location.normalize();

        repository.save(location);
        return toResponse(location);
    }

    public Weather getWeather(String city,String stateCode,String countryCode) {
        try{
            log.info("Attempting to fetch weather from weather-service for {},{},{}", city, stateCode, countryCode);
            WeatherDto weatherDto = builtWebClient
                    .get()
                    .uri("http://weather-service/api/weather",
                            uriBuilder -> uriBuilder
                                    .queryParam("city", city)
                                    .queryParam("stateCode", stateCode)
                                    .queryParam("countryCode", countryCode)
                                    .build())
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), response -> {
                        log.error("Weather service returned status: {}", response.getStatusCode());
                        return response.createException();
                    })
                    .bodyToMono(WeatherDto.class)
                    .block();
            log.info("Received WeatherDto: {}", weatherDto);
            if (weatherDto == null) {
                log.error("WeatherDto is null for {},{},{}", city, stateCode, countryCode);
                return createDefaultWeather();
            }

            Weather weather = weatherDto.toWeather();
            log.info("Converted to Weather: {}", weather);
            return weather;
        } catch (Exception e) {
            log.error("Failed to fetch weather for {},{},{}: {}", 
                    city, stateCode, countryCode, e.getMessage());
            log.error("Exception details: ", e);
            return createDefaultWeather();
        }
    }

    private Weather createDefaultWeather() {
        return Weather.builder()
                .condition("Unknown")
                .temperature(0.0)
                .hi_temperature(0.0)
                .low_temperature(0.0)
                .cloudCoverage(0.0)
                .windSpeed(0.0)
                .precipitation(0.0)
                .fetchedAt(Instant.now())
                .build();
    }

    public Location createLocation(String city, String stateCode, String countryCode) {
        Address newAddress = new Address();
        newAddress.setCity(city);
        newAddress.setStateCode(stateCode);
        newAddress.setCountryCode(countryCode);
        newAddress.normalize();
        Weather weather = getWeather(newAddress.getCity(), newAddress.getStateCode(), newAddress.getCountryCode());
        Location newLocation = Location.builder()
                .locationId(UUID.randomUUID().toString())
                .name(null)
                .weather(weather)
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

    public List<LocationResponse> getAll() {
        List<LocationResponse> locations = repository.findAll().stream()
                .map(LocationResponse::new)
                .collect(Collectors.toList());
        return  locations;
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
                .weather(loc.getWeather() != null ? loc.getWeather().toDto() : null)
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