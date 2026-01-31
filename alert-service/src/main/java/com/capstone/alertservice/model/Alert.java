package com.capstone.alertservice.model;

import com.capstone.alertservice.dto.AlertEvent;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String locationId;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String stateCode;

    @Column(nullable = false)
    private String alertType;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static Alert fromEvent(AlertEvent event, Long userId) {
        return Alert.builder()
                .userId(userId)
                .locationId(event.getLocationId())
                .city(event.getCity())
                .stateCode(event.getStateCode())
                .alertType(event.getAlertType().name())
                .message(String.format("%s in %s, %s. Current: %.1f",
                        event.getCondition(),
                        event.getCity(),
                        event.getStateCode(),
                        getCurrentValue(event)))
                .severity(event.getSeverity())
                .createdAt(Instant.now())
                .build();
    }

    private static Double getCurrentValue(AlertEvent event) {
        return switch (event.getAlertType()) {
            case HIGH_WIND -> event.getWindSpeed();
            case EXTREME_TEMPERATURE -> event.getTemperature();
            case HEAVY_PRECIPITATION -> event.getPrecipitation();
            default -> 0.0;
        };
    }
}