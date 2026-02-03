package com.capstone.locationservice.dto;

import com.capstone.locationservice.model.Weather;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class WeatherDto {
    private String condition;
    private Double temperature;
    private Double hi_temperature;
    private Double low_temperature;
    private Double cloudCoverage;
    private Double windSpeed;
    private Double precipitation;
    private Instant fetchedAt;

    public Weather toWeather(){
        Weather weather = new Weather();
        weather.setCondition(condition);
        weather.setTemperature(temperature);
        weather.setHi_temperature(hi_temperature);
        weather.setLow_temperature(low_temperature);
        weather.setCloudCoverage(cloudCoverage);
        weather.setWindSpeed(windSpeed);
        weather.setPrecipitation(precipitation);
        weather.setFetchedAt(fetchedAt);
        return weather;
    }
}
