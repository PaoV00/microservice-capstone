package com.capstone.weatherservice.service;

import com.capstone.weatherservice.dto.WeatherDto;
import com.capstone.weatherservice.dto.WeatherResponse;
import com.capstone.weatherservice.model.Weather;
import com.capstone.weatherservice.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WebClient webClient;
    private final WeatherRepository weatherRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.data.ttl.minutes:5}")
    private int dataTtlMinutes;

    public WeatherDto getWeatherData(String city, String stateCode, String countryCode) {
        Instant now = Instant.now();

        // 1. Try to get fresh data from database
        Optional<Weather> freshData = weatherRepository.findFreshWeather(
                city, stateCode, countryCode, now);

        if (freshData.isPresent()) {
            log.debug("Returning fresh weather data from database for {},{},{}",
                    city, stateCode, countryCode);
            return convertToDto(freshData.get());
        }

        log.info("No fresh data found, fetching from OpenWeather API for {},{},{}",
                city, stateCode, countryCode);

        // 2. Fetch from OpenWeather API
        try {
            WeatherResponse resp = fetchFromExternalApi(city, stateCode, countryCode);
            WeatherDto dto = convertFromApiResponse(resp);

            // 3. Save to database
            saveWeatherData(city, stateCode, countryCode, dto);

            return dto;
        } catch (Exception e) {
            log.error("Failed to fetch weather from OpenWeather API: {}", e.getMessage());

            // 4. Fallback: Get the most recent data (even if expired)
            Optional<Weather> staleData = weatherRepository
                    .findFirstByCityAndStateCodeAndCountryCodeOrderByFetchedAtDesc(
                            city, stateCode, countryCode);

            if (staleData.isPresent()) {
                log.warn("Returning stale weather data as fallback for {},{},{}",
                        city, stateCode, countryCode);
                return convertToDto(staleData.get());
            }

            // 5. No data at all
            throw new RuntimeException("Weather data unavailable and no cached data found", e);
        }
    }

    private WeatherResponse fetchFromExternalApi(String city, String stateCode, String countryCode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("q", city + "," + stateCode + "," + countryCode)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .block();
    }

    private void saveWeatherData(String city, String stateCode, String countryCode, WeatherDto dto) {
        Weather weatherData = Weather.builder()
                .city(city.toUpperCase())
                .stateCode(stateCode.toUpperCase())
                .countryCode(countryCode.toUpperCase())
                .condition(dto.getCondition())
                .temperature(dto.getTemperature())
                .hiTemperature(dto.getHi_temperature())
                .lowTemperature(dto.getLow_temperature())
                .cloudCoverage(dto.getCloudCoverage())
                .windSpeed(dto.getWindSpeed())
                .precipitation(dto.getPrecipitation())
                .fetchedAt(dto.getFetchedAt())
                .expiresAt(dto.getFetchedAt().plusSeconds(dataTtlMinutes * 60L))
                .build();

        weatherRepository.save(weatherData);
        log.debug("Saved weather data for {},{},{}", city, stateCode, countryCode);
    }

    // Convert from database entity to DTO
    private WeatherDto convertToDto(Weather data) {
        return WeatherDto.builder()
                .condition(data.getCondition())
                .temperature(data.getTemperature())
                .hi_temperature(data.getHiTemperature())
                .low_temperature(data.getLowTemperature())
                .cloudCoverage(data.getCloudCoverage())
                .windSpeed(data.getWindSpeed())
                .precipitation(data.getPrecipitation())
                .fetchedAt(data.getFetchedAt())
                .build();
    }

    // Convert from API response to DTO
    private WeatherDto convertFromApiResponse(WeatherResponse weather) {
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

        Double precipitation = null;
        if (weather.getRain() != null && weather.getRain().getOneHour() != null) {
            precipitation = weather.getRain().getOneHour();
        }
        else if (weather.getSnow() != null && weather.getSnow().getOneHour() != null) {
            precipitation = weather.getSnow().getOneHour();
        }
        else if (weather.getRain() != null && weather.getRain().getThreeHours() != null) {
            precipitation = weather.getRain().getThreeHours() / 3;
        }
        else if (weather.getSnow() != null && weather.getSnow().getThreeHours() != null) {
            precipitation = weather.getSnow().getThreeHours() / 3;
        }

        Instant fetchedAt = Instant.ofEpochSecond(weather.getDt());

        return WeatherDto.builder()
                .condition(condition)
                .temperature(temperature)
                .low_temperature(lowTemperature)
                .hi_temperature(hiTemperature)
                .cloudCoverage(cloudCoverage)
                .windSpeed(windSpeed)
                .precipitation(precipitation)
                .fetchedAt(fetchedAt)
                .build();
    }
}