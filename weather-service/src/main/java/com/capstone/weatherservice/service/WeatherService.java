package com.capstone.weatherservice.service;

import com.capstone.weatherservice.dto.WeatherDto;
import com.capstone.weatherservice.dto.WeatherResponse;
import com.capstone.weatherservice.model.Weather;
import com.capstone.weatherservice.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WebClient.Builder webClient;
    private final WeatherRepository weatherRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.data.ttl.minutes:5}")
    private int dataTtlMinutes;

    /**
     * One row per (city,stateCode,countryCode).
     * If row exists and not expired -> return.
     * If expired -> fetch API -> update same row -> return.
     * If row not exist -> fetch API -> insert -> return.
     */
    @Transactional
    public WeatherDto getWeatherData(String city, String stateCode, String countryCode) {
        LocalDateTime now = LocalDateTime.now();

        // Normalize keys consistently (prevents case-based duplicates)
        city = normalize(city);
        stateCode = normalize(stateCode);
        countryCode = normalize(countryCode);

        // If you added repository locking method, use that:
        // Optional<Weather> existingOpt = weatherRepository.findForUpdate(city, stateCode, countryCode);
        Optional<Weather> existingOpt = weatherRepository.findByCityAndStateCodeAndCountryCode(city, stateCode, countryCode);

        // 1) If we already have a row, and it's not expired, return it
        if (existingOpt.isPresent()) {
            Weather existing = existingOpt.get();
            if (existing.getExpiresAt() != null && existing.getExpiresAt().isAfter(now)) {
                log.debug("Returning cached weather for {},{},{}", city, stateCode, countryCode);
                return convertToDto(existing);
            }
        }

        log.info("Weather expired/missing. Fetching from OpenWeather for {},{},{}", city, stateCode, countryCode);

        // 2) Fetch from API and update/insert
        try {
            WeatherResponse resp = fetchFromExternalApi(city, stateCode, countryCode);
            WeatherDto dto = convertFromApiResponse(resp);

            if (dto == null || dto.getFetchedAt() == null) {
                throw new RuntimeException("OpenWeather returned empty/invalid response");
            }

            Weather saved = upsertWeather(existingOpt.orElse(null), city, stateCode, countryCode, dto);
            return convertToDto(saved);

        } catch (Exception e) {
            log.error("Failed to fetch weather from OpenWeather: {}", e.getMessage(), e);

            // 3) Fallback: if we have an existing row (even stale), return it
            if (existingOpt.isPresent()) {
                log.warn("Returning stale weather as fallback for {},{},{}", city, stateCode, countryCode);
                return convertToDto(existingOpt.get());
            }

            // 4) Nothing in DB and API failed -> error
            throw new RuntimeException("Weather unavailable and no cached data found", e);
        }
    }

    private Weather upsertWeather(Weather existing, String city, String stateCode, String countryCode, WeatherDto dto) {
        LocalDateTime fetchedAt = dto.getFetchedAt();
        LocalDateTime expiresAt = fetchedAt.plusSeconds(dataTtlMinutes * 60L);

        if (existing == null) {
            // Insert new row (first time for this location)
            Weather weather = Weather.builder()
                    .city(city)
                    .stateCode(stateCode)
                    .countryCode(countryCode)
                    .condition(dto.getCondition())
                    .temperature(dto.getTemperature())
                    .hiTemperature(dto.getHi_temperature())
                    .lowTemperature(dto.getLow_temperature())
                    .cloudCoverage(dto.getCloudCoverage())
                    .windSpeed(dto.getWindSpeed())
                    .precipitation(dto.getPrecipitation())
                    .fetchedAt(fetchedAt)
                    .expiresAt(expiresAt)
                    .build();

            Weather saved = weatherRepository.save(weather);
            log.debug("Inserted weather row for {},{},{}", city, stateCode, countryCode);
            return saved;
        }

        // Update existing row (expired)
        existing.setCondition(dto.getCondition());
        existing.setTemperature(dto.getTemperature());
        existing.setHiTemperature(dto.getHi_temperature());
        existing.setLowTemperature(dto.getLow_temperature());
        existing.setCloudCoverage(dto.getCloudCoverage());
        existing.setWindSpeed(dto.getWindSpeed());
        existing.setPrecipitation(dto.getPrecipitation());
        existing.setFetchedAt(fetchedAt);
        existing.setExpiresAt(expiresAt);

        Weather saved = weatherRepository.save(existing);
        log.debug("Updated weather row for {},{},{}", city, stateCode, countryCode);
        return saved;
    }

    private String normalize(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }

    private WeatherResponse fetchFromExternalApi(String city, String stateCode, String countryCode) {
        return webClient.build()
                .get()
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

    // Convert from API response to DTO (kept as you wrote it)
    private WeatherDto convertFromApiResponse(WeatherResponse weather) {
        if (weather == null) return null;

        String condition = Optional.ofNullable(weather.getWeather())
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    WeatherResponse.Weather w = list.get(0);
                    return (w.getDescription() != null && !w.getDescription().isBlank())
                            ? w.getDescription()
                            : w.getMain();
                })
                .orElse(null);

        Double temperature    = weather.getMain() != null ? weather.getMain().getTemp()    : null;
        Double lowTemperature = weather.getMain() != null ? weather.getMain().getTempMin() : null;
        Double hiTemperature  = weather.getMain() != null ? weather.getMain().getTempMax() : null;
        Double windSpeed      = weather.getWind() != null ? weather.getWind().getSpeed()   : null;

        Double cloudCoverage = (weather.getClouds() != null && weather.getClouds().getAll() != null)
                ? weather.getClouds().getAll().doubleValue()
                : null;

        Double precipitation = null;
        if (weather.getRain() != null && weather.getRain().getOneHour() != null) {
            precipitation = weather.getRain().getOneHour();
        } else if (weather.getSnow() != null && weather.getSnow().getOneHour() != null) {
            precipitation = weather.getSnow().getOneHour();
        } else if (weather.getRain() != null && weather.getRain().getThreeHours() != null) {
            precipitation = weather.getRain().getThreeHours() / 3;
        } else if (weather.getSnow() != null && weather.getSnow().getThreeHours() != null) {
            precipitation = weather.getSnow().getThreeHours() / 3;
        }

        LocalDateTime fetchedAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(weather.getDt()), ZoneId.systemDefault());

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