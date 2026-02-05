package com.capstone.locationservice.messaging;

import com.capstone.locationservice.event.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertProducer {

    private final KafkaTemplate<String, AlertEvent> kafkaTemplate;

    public void sendWeatherAlert(String locationId, String city, String stateCode, String countryCode,
                                 AlertEvent.AlertType alertType, String condition,
                                 Double currentValue, Double thresholdValue, String severity) {

        AlertEvent event = AlertEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .locationId(locationId)
                .city(city)
                .stateCode(stateCode)
                .countryCode(countryCode)
                .alertType(alertType)
                .condition(condition)
                .temperature(alertType == AlertEvent.AlertType.EXTREME_TEMPERATURE ? currentValue : null)
                .windSpeed(alertType == AlertEvent.AlertType.HIGH_WIND ? currentValue : null)
                .precipitation(alertType == AlertEvent.AlertType.HEAVY_PRECIPITATION ? currentValue : null)
                .thresholdValue(thresholdValue)
                .triggeredAt(Instant.now())
                .severity(severity)
                .build();

        CompletableFuture<SendResult<String, AlertEvent>> future =
                kafkaTemplate.send("weatherAlerts", locationId, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Alert sent successfully for location {}: {}", locationId, alertType);
            } else {
                log.error("Failed to send alert for location {}: {}", locationId, ex.getMessage());
            }
        });
    }
}