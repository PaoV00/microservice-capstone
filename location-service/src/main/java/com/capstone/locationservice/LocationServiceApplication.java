package com.capstone.locationservice;

import com.capstone.locationservice.model.Address;
import com.capstone.locationservice.model.Location;
import com.capstone.locationservice.repository.LocationRepository;
import com.capstone.locationservice.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Slf4j
public class LocationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(LocationRepository locationRepository ) {
        return (args) -> {
            Address address1 = new Address();
            address1.setCity("saint paul");
            address1.setStateCode("MN");
            address1.setCountryCode("US");

            Location location1 = new Location();
            location1.setLocationId("test123id");
            address1.setLocationId(location1.getLocationId());

            location1.setAddress(address1);
            location1.setName("Test Location");


            locationRepository.save(location1);
            log.info("Seed data inserted location with id: {}", location1.getLocationId());
        };
    }
}
