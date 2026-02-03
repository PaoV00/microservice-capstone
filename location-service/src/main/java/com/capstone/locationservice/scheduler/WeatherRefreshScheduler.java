package com.capstone.locationservice.scheduler;

import com.capstone.locationservice.dto.WeatherDto;
import com.capstone.locationservice.event.AlertEvent;
import com.capstone.locationservice.messaging.AlertProducer;
import com.capstone.locationservice.model.Location;
import com.capstone.locationservice.repository.LocationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherRefreshScheduler {

    private final LocationRepository locationRepository;
    private final AlertProducer alertProducer;
    private final WebClient.Builder webClient;

    @Value("${weather.thresholds.wind-speed:20.0}")
    private double windSpeedThreshold;

    @Value("${weather.thresholds.high-temp:35.0}")
    private double highTempThreshold;

    @Value("${weather.thresholds.low-temp:-10.0}")
    private double lowTempThreshold;

    @Value("${weather.thresholds.precipitation:50.0}")
    private double precipitationThreshold;

    // PT5M 5 minutes
    @Scheduled(fixedRateString = "PT5M")
    @SchedulerLock(
            name = "checkWeatherForAlerts",
            lockAtMostFor = "PT10M",
            lockAtLeastFor = "PT30S"
    )
    @CircuitBreaker(name = "weatherService", fallbackMethod = "schedulerFallback")
    public void checkWeatherForAlerts() {
        log.info("Checking weather conditions for alerts...");
        List<Location> locations = locationRepository.findAll();

        for (Location loc : locations) {
            String city = loc.getAddress().getCity();
            String state = loc.getAddress().getStateCode();
            String country = loc.getAddress().getCountryCode();
            String locationId = loc.getLocationId();

            try {
                WeatherDto dto = fetchWeatherFromService(city, state, country);
                loc.setWeather(dto.toWeather());
                if (dto != null) {
                    checkWeatherThresholds(locationId, city, state, country, dto);
                    log.debug("Weather check complete for location: {}", locationId);
                } else {
                    log.warn("Weather data null for {},{},{}", city, state, country);
                }
            } catch (Exception ex) {
                log.warn("Failed to check weather for {},{},{} : {}",
                        city, state, country, ex.getMessage());
            }
        }
        log.info("Completed weather alert check for {} locations", locations.size());
    }

    private WeatherDto fetchWeatherFromService(String city, String state, String country) {
        return webClient.build()
                .get()
                .uri("http://weather-service/api/weather",
                        uriBuilder -> uriBuilder
                                .queryParam("city", city)
                                .queryParam("stateCode", state)
                                .queryParam("countryCode", country)
                                .build())
                .retrieve()
                .bodyToMono(WeatherDto.class)
                .block();
    }

    // Fallback for when Weather Service is down
    private void schedulerFallback(Exception ex) {
        log.error("Weather Service unavailable, skipping alert checks: {}", ex.getMessage());
    }

    private void checkWeatherThresholds(String locationId, String city, String state,
                                        String country, WeatherDto weather) {
        // Check wind speed
        if (weather.getWindSpeed() != null && weather.getWindSpeed() > windSpeedThreshold) {
            String severity = weather.getWindSpeed() > 30.0 ? "SEVERE" :
                    weather.getWindSpeed() > 25.0 ? "HIGH" : "MEDIUM";

            alertProducer.sendWeatherAlert(
                    locationId, city, state, country,
                    AlertEvent.AlertType.HIGH_WIND,
                    "High Wind Warning",
                    weather.getWindSpeed(),
                    windSpeedThreshold,
                    severity
            );
        }

        // Check high temperature
        if (weather.getTemperature() != null && weather.getTemperature() > highTempThreshold) {
            alertProducer.sendWeatherAlert(
                    locationId, city, state, country,
                    AlertEvent.AlertType.EXTREME_TEMPERATURE,
                    "Heat Warning",
                    weather.getTemperature(),
                    highTempThreshold,
                    "HIGH"
            );
        }

        // Check low temperature
        if (weather.getTemperature() != null && weather.getTemperature() < lowTempThreshold) {
            alertProducer.sendWeatherAlert(
                    locationId, city, state, country,
                    AlertEvent.AlertType.EXTREME_TEMPERATURE,
                    "Cold Warning",
                    weather.getTemperature(),
                    lowTempThreshold,
                    "HIGH"
            );
        }

        // Check precipitation (cloud coverage as proxy)
        if (weather.getCloudCoverage() != null && weather.getCloudCoverage() > precipitationThreshold) {
            alertProducer.sendWeatherAlert(
                    locationId, city, state, country,
                    AlertEvent.AlertType.HEAVY_PRECIPITATION,
                    "Heavy Precipitation Expected",
                    weather.getCloudCoverage(),
                    precipitationThreshold,
                    "MEDIUM"
            );
        }
    }
}