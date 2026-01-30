package com.capstone.weatherservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String stateCode;

    @Column(nullable = false)
    private String countryCode;

    private String condition;
    private Double temperature;
    private Double hiTemperature;
    private Double lowTemperature;
    private Double cloudCoverage;
    private Double windSpeed;
    private Double precipitation;

    @Column(nullable = false)
    private Instant fetchedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private String source;

    @PrePersist
    protected void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = Instant.now();
        }
        if (expiresAt == null) {
            expiresAt = fetchedAt.plusSeconds(300); // 5 minutes TTL
        }
        if (source == null) {
            source = "OPENWEATHER";
        }
    }
}
