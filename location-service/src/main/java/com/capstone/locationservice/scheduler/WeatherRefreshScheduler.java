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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherRefreshScheduler {

    private final LocationRepository locationRepository;
    private final AlertProducer alertProducer;
    private final WebClient.Builder webClient;

    @Value("${weather.thresholds.wind-speed}")
    private double windSpeedThreshold;

    @Value("${weather.thresholds.high-temp}")
    private double highTempThreshold;

    @Value("${weather.thresholds.low-temp}")
    private double lowTempThreshold;

    @Value("${weather.thresholds.precipitation}")
    private double precipitationThreshold;

    @Value("${alert.cooldown.minutes:60}")  // Don't send same alert within 1 hour
    private long alertCooldownMinutes;

    private final Map<String, Map<AlertEvent.AlertType, Instant>> lastAlertTimes = new ConcurrentHashMap<>();

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
            if (shouldSendAlert(locationId, AlertEvent.AlertType.HIGH_WIND)) {
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

                recordAlertSent(locationId, AlertEvent.AlertType.HIGH_WIND);
            }
        }

        // Check high temperature
        if (weather.getTemperature() != null && weather.getTemperature() > highTempThreshold) {
            if (shouldSendAlert(locationId, AlertEvent.AlertType.EXTREME_TEMPERATURE)){
                alertProducer.sendWeatherAlert(
                        locationId, city, state, country,
                        AlertEvent.AlertType.EXTREME_TEMPERATURE,
                        "Heat Warning",
                        weather.getTemperature(),
                        highTempThreshold,
                        "HIGH"
                );
                recordAlertSent(locationId, AlertEvent.AlertType.EXTREME_TEMPERATURE);
            }
        }

        // Check low temperature
        if (weather.getTemperature() != null && weather.getTemperature() < lowTempThreshold) {
            if (shouldSendAlert(locationId, AlertEvent.AlertType.EXTREME_TEMPERATURE)) {
                alertProducer.sendWeatherAlert(
                        locationId, city, state, country,
                        AlertEvent.AlertType.EXTREME_TEMPERATURE,
                        "Cold Warning",
                        weather.getTemperature(),
                        lowTempThreshold,
                        "HIGH"
                );
                recordAlertSent(locationId, AlertEvent.AlertType.EXTREME_TEMPERATURE);
            }
        }

        // Check precipitation
        if (weather.getPrecipitation() != null && weather.getPrecipitation() > precipitationThreshold) {
            if (shouldSendAlert(locationId, AlertEvent.AlertType.HEAVY_PRECIPITATION)) {
                alertProducer.sendWeatherAlert(
                        locationId, city, state, country,
                        AlertEvent.AlertType.HEAVY_PRECIPITATION,
                        "Heavy Precipitation Expected",
                        weather.getPrecipitation(),
                        precipitationThreshold,
                        "MEDIUM"
                );
                recordAlertSent(locationId, AlertEvent.AlertType.HEAVY_PRECIPITATION);
            }
        }
    }

    private boolean shouldSendAlert(String locationId, AlertEvent.AlertType alertType) {
        Map<AlertEvent.AlertType, Instant> locationAlerts = lastAlertTimes.get(locationId);
        if (locationAlerts == null) {
            return true; // No previous alerts for this location
        }

        Instant lastAlert = locationAlerts.get(alertType);
        if (lastAlert == null) {
            return true; // No previous alert of this type
        }

        // Check if cooldown period has passed
        Instant cooldownEnd = lastAlert.plus(Duration.ofMinutes(alertCooldownMinutes));
        return Instant.now().isAfter(cooldownEnd);
    }

    private void recordAlertSent(String locationId, AlertEvent.AlertType alertType) {
        lastAlertTimes.computeIfAbsent(locationId, k -> new ConcurrentHashMap<>())
                .put(alertType, Instant.now());
    }
}