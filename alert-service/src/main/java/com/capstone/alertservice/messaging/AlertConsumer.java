package com.capstone.alertservice.messaging;

import com.capstone.alertservice.dto.AlertEvent;
import com.capstone.alertservice.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertConsumer {

    private final AlertService alertService;

    @KafkaListener(topics = "weather-alerts")
    public void consume(AlertEvent event) {
        log.info("Received alert for location: {}", event.getLocationId());
        alertService.processAlert(event);
    }
}