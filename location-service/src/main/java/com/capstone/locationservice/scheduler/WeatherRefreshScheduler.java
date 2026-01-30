package com.capstone.locationservice.scheduler;

import com.capstone.locationservice.dto.WeatherDto;
import com.capstone.locationservice.model.Location;
import com.capstone.locationservice.repository.LocationRepository;
import com.capstone.locationservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherRefreshScheduler {

    private final LocationRepository locationRepository;
    private final LocationService locationService;

    @Scheduled(fixedRateString = "PT1M")
    @SchedulerLock(
            name = "refreshWeatherForAllLocations",
            lockAtMostFor = "PT10M",
            lockAtLeastFor = "PT30S"
    )
    public void refreshWeatherForAllLocations() {

        log.info("Starting weather refresh for all locations....");
        List<Location> locations = locationRepository.findAll();

        for (Location loc : locations) {
            String city = loc.getAddress().getCity();
            String state = loc.getAddress().getStateCode();
            String country = loc.getAddress().getCountryCode();

            try {
                WeatherDto dto = locationService.getWeatherData(city, state, country);
                if (dto != null) {
                    loc.setWeather(dto.toWeather());
                    locationRepository.save(loc);
                } else {
                    log.warn("Weather refresh returned null for {},{},{}", city, state, country);
                }
            } catch (Exception ex) {
                log.warn("Weather refresh failed for {},{},{} : {}", city, state, country, ex.getMessage());

            }
        }
    }

}