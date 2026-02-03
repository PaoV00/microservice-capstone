package com.capstone.weatherservice.controller;

import com.capstone.weatherservice.dto.WeatherDto;
import com.capstone.weatherservice.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherRest {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<WeatherDto> getWeather(@RequestParam String city, @RequestParam String stateCode, @RequestParam String countryCode) {
        log.info("Weather API called for {},{},{}", city, stateCode, countryCode);
        WeatherDto result = weatherService.getWeatherData(city, stateCode, countryCode);
        log.info("Returning weather data: {}", result);
        return ResponseEntity.ok(result);
    }
}


