package com.capstone.weatherservice.service;

import com.capstone.weatherservice.dto.WeatherDto;
import com.capstone.weatherservice.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherDto getWeatherData(String city, String stateCode, String countryCode) {
        WeatherResponse resp = this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("q", city + "," + stateCode + "," + countryCode)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .block();

        return toDto(resp);
    }

    public WeatherDto toDto(WeatherResponse weather) {
        if (weather == null) {
            return null;
        }

        String condition = Optional.ofNullable(weather.getWeather())
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    WeatherResponse.Weather w = list.get(0);
                    return (w.getDescription() != null && !w.getDescription().isBlank())
                            ? w.getDescription()
                            : w.getMain();
                })
                .orElse(null);

        Double temperature   = weather.getMain() != null ? weather.getMain().getTemp()     : null;
        Double lowTemperature= weather.getMain() != null ? weather.getMain().getTempMin()  : null;
        Double hiTemperature = weather.getMain() != null ? weather.getMain().getTempMax()  : null;
        Double windSpeed     = weather.getWind() != null ? weather.getWind().getSpeed()    : null;

        Double cloudCoverage = (weather.getClouds() != null && weather.getClouds().getAll() != null)
                ? weather.getClouds().getAll().doubleValue()
                : null;


        Instant fetchedAt = Instant.ofEpochSecond(weather.getDt());

        return WeatherDto.builder()
                .condition(condition)
                .temperature(temperature)
                .low_temperature(lowTemperature)
                .hi_temperature(hiTemperature)
                .cloudCoverage(cloudCoverage)
                .windSpeed(windSpeed)
                .fetchedAt(fetchedAt)
                .build();
    }

}
