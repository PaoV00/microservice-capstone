package com.capstone.weatherservice.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class WeatherDto {
    private String condition;
    private Double temperature;
    private Double hi_temperature;
    private Double low_temperature;
    private Double cloudCoverage;
    private Double windSpeed;
    private Double precipitation;
    private Instant fetchedAt;
}
