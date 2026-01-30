package com.capstone.locationservice.model;

import com.capstone.locationservice.dto.WeatherDto;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Weather {
    private String condition;
    private Double temperature;
    private Double hi_temperature;
    private Double low_temperature;
    private Double cloudCoverage;
    private Double windSpeed;
    private Double precipitation;
    private Instant fetchedAt;

    public WeatherDto toDto() {
        return WeatherDto.builder()
                .condition(condition)
                .temperature(temperature)
                .hi_temperature(hi_temperature)
                .low_temperature(low_temperature)
                .cloudCoverage(cloudCoverage)
                .windSpeed(windSpeed)
                .precipitation(precipitation)
                .fetchedAt(fetchedAt)
                .build();
    }
}
