package com.capstone.locationservice.service;

import com.capstone.locationservice.dto.AddressDto;
import com.capstone.locationservice.dto.LocationRequest;
import com.capstone.locationservice.dto.LocationResponse;
import com.capstone.locationservice.dto.WeatherDto;
import com.capstone.locationservice.exceptions.DuplicateException;
import com.capstone.locationservice.exceptions.NotFoundException;
import com.capstone.locationservice.model.Address;
import com.capstone.locationservice.model.Location;

import com.capstone.locationservice.model.Weather;
import com.capstone.locationservice.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationService {

    private final LocationRepository repository;
    private final WebClient.Builder webClient;


    public LocationResponse create(LocationRequest req) {
        if(repository.existsByAddress_CityAndAddress_StateCodeAndAddress_CountryCode(
                req.getAddress().getCity(), req.getAddress().getStateCode(),
                req.getAddress().getCountryCode()
        )){
            throw new DuplicateException("Location already exists");
        }
        Address newAddress = buildAddress(req);
        //get weather base on address
        WeatherDto weatherDto = getWeatherData(newAddress.getCity(), newAddress.getStateCode(), newAddress.getCountryCode());
        Location location = Location.builder()
                .locationId(UUID.randomUUID().toString())
                .name(req.getName() != null ? req.getName() : null)
                .weather(weatherDto.toWeather())
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
        Weather weather = getWeatherData(city, stateCode, countryCode).toWeather();

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

    public String getLocationIdIfDontExistCreateNewLocation(String city, String stateCode, String countryCode){
        boolean exist = repository.existsByAddress_CityAndAddress_StateCodeAndAddress_CountryCode(city, stateCode, countryCode);
        if(exist){
            Location location = repository.findByAddress_CityAndAddress_StateCodeAndAddress_CountryCode(city, stateCode, countryCode);
            log.info("Returning location id: {} " + location.getLocationId());
            return location.getLocationId();
        }

        Location newLocation = createLocation(city, stateCode, countryCode);
        log.info("Returning location id: {} " + newLocation.getLocationId());
        return newLocation.getLocationId();
    }

    public List<LocationResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public LocationResponse update(String id, Map<String, Object> updates) {
        Location location = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));

        String oldCity = location.getAddress().getCity();
        String oldState = location.getAddress().getStateCode();
        String oldCountry = location.getAddress().getCountryCode();

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

        String newCity = location.getAddress().getCity();
        String newState = location.getAddress().getStateCode();
        String newCountry = location.getAddress().getCountryCode();

        if(changed(oldCity, newCity) || changed(oldState, newState) || changed(oldCountry, newCountry)) {
            WeatherDto weatherDto = getWeatherData(newCity, newState, newCountry);
            location.setWeather(weatherDto.toWeather());
        }

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
    // that will be set when a new location is created
    private Address buildAddress(LocationRequest request) {
        Address address = Address.builder()
                .city(request.getAddress().getCity())
                .stateCode(request.getAddress().getStateCode())
                .countryCode(request.getAddress().getCountryCode())
                .build();
        address.normalize();

        return address;
    }

    public WeatherDto getWeatherData(String city, String stateCode, String countryCode) {
        WeatherDto weatherDto = webClient.build().get()
                .uri("http://weather-service/api/weather",
                        uriBuilder -> uriBuilder
                                .queryParam("city", city)
                                .queryParam("stateCode", stateCode)
                                .queryParam("countryCode", countryCode)
                                .build())
                .retrieve()
                .bodyToMono(WeatherDto.class)
                .block();
        return weatherDto;
    }

    private boolean changed(String oldValue, String newValue){
        if(!oldValue.equals(newValue)) return true;
        return false;
    }

}