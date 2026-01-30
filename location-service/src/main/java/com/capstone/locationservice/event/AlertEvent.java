package com.capstone.locationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEvent {
    private String eventId;
    private String locationId;
    private String city;
    private String stateCode;
    private String countryCode;

    // Weather conditions that triggered alert
    private AlertType alertType;
    private String condition;
    private Double temperature;
    private Double windSpeed;
    private Double precipitation;
    private Double thresholdValue;

    private Instant triggeredAt;
    private String severity; // LOW, MEDIUM, HIGH, SEVERE

    public enum AlertType {
        HIGH_WIND,
        EXTREME_TEMPERATURE,
        HEAVY_PRECIPITATION,
        STORM_WARNING,
        HEAT_WAVE,
        COLD_WAVE
    }
}
