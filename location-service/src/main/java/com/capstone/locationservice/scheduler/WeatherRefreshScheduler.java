package com.capstone.locationservice.scheduler;

import com.capstone.locationservice.dto.WeatherDto;
import com.capstone.locationservice.model.Address;
import com.capstone.locationservice.model.Location;
import com.capstone.locationservice.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherRefreshScheduler {

    private final LocationRepository locationRepository;
    private final WebClient.Builder webClient;

    @Scheduled(fixedRateString = "PT1M")
    public void refreshWeatherForAllLocations(){
        log.info("Starting weather refresh for all locations....");

        List<Location> locations = locationRepository.findAll();
        for(Location location : locations){
            Address address = location.getAddress();

            WeatherDto weather = webClient.build().get()
                    .uri("http://weather-service/api/weather",
                            uriBuilder -> uriBuilder
                                    .queryParam("city", address.getCity())
                                    .queryParam("stateCode", address.getStateCode())
                                    .queryParam("countryCode", address.getCountryCode())
                                    .build())
                    .retrieve()
                    .bodyToMono(WeatherDto.class)
                    .block();
            location.setWeather(weather.toWeather());
        }
        locationRepository.saveAll(locations);
        log.info("Completed scheduled weather refresh for {} locations", locations.size());
    }

}
