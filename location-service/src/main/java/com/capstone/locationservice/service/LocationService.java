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
        if(repository.existsByAddress_CityAndAddress_StateCodeAndAddress_ZipAndAddress_CountryCode(
                req.getAddress().getCity(), req.getAddress().getStateCode(),
                req.getAddress().getZip(), req.getAddress().getCountryCode()
        )){
            throw new DuplicateException("Cannot create a new location, because it already exists");
        }

        // Build address before adding to location
        Address newAddress = buildAddress(req);

        //get weather base on address
        WeatherDto weatherDto = getWeatherData(newAddress.getCity(), newAddress.getStateCode(), newAddress.getCountryCode());

        Location location = Location.builder()
                .locationId(UUID.randomUUID().toString())
                .name(req.getName() != null ? req.getName() : null)
                .address(newAddress)
                .weather(weatherDto.toWeather())
                .build();
        location.normalize();

        repository.save(location);
        return toResponse(location);
    }

    public LocationResponse get(String id) {
        Location loc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));
        return toResponse(loc);
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

            if(addrUpdates.containsKey("number")) address.setNumber(addrUpdates.get("number").toString());
            if(addrUpdates.containsKey("street")) address.setStreet(addrUpdates.get("street").toString());
            if(addrUpdates.containsKey("city")) address.setCity(addrUpdates.get("city").toString());
            if(addrUpdates.containsKey("stateCode")) address.setStateCode(addrUpdates.get("stateCode").toString());
            if(addrUpdates.containsKey("zip")) address.setZip(addrUpdates.get("zip").toString());
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
                .weather(loc.getWeather().toDto())
                .build();
    }

    private Address buildAddress(LocationRequest request) {
        Address address = Address.builder()
                .number(request.getAddress().getNumber())
                .street(request.getAddress().getStreet())
                .city(request.getAddress().getCity())
                .stateCode(request.getAddress().getStateCode())
                .zip(request.getAddress().getZip())
                .countryCode(request.getAddress().getCountryCode())
                .build();
        address.normalize();

        return address;
    }

    private WeatherDto getWeatherData(String city, String stateCode, String countryCode) {
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