package com.capstone.alertservice.service;

import com.capstone.alertservice.dto.AlertEvent;
import com.capstone.alertservice.model.Alert;
import com.capstone.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final WebClient.Builder webclient;
    private final AlertRepository alertRepository;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Transactional
    public void processAlert(AlertEvent alertEvent) {
        log.info("Processing alert for location: {}", alertEvent.getLocationId());

        // 1. Get users who have favorited this location
        List<Long> userIds = getUsersWhoFavoritedLocation(alertEvent.getLocationId());

        // 2. Get users who live in this location
        List<Long> usersInLocation = getUsersInLocation(
                alertEvent.getCity(), alertEvent.getStateCode(), alertEvent.getCountryCode());

        // 3. combine and get rid of duplicates
        List<Long> allUserIds = new ArrayList<>(userIds);
        allUserIds.addAll(usersInLocation);
        List<Long> uniqueUserIds = allUserIds.stream().distinct().collect(Collectors.toList());

        // 4. Store alerts for each user
        if (!uniqueUserIds.isEmpty()) {
            storeAlertsForUsers(uniqueUserIds, alertEvent);
            log.info("Stored alerts for {} users", uniqueUserIds.size());
        } else {
            log.info("No users to alert for location {}", alertEvent.getLocationId());
        }
    }

    private void storeAlertsForUsers(List<Long> userIds, AlertEvent alertEvent) {
        List<Alert> alerts = userIds.stream()
                .map(userId -> Alert.fromEvent(alertEvent, userId))
                .collect(Collectors.toList());

        alertRepository.saveAll(alerts);
        log.debug("Saved {} alerts to database", alerts.size());
    }

    // Frontend needs just ONE endpoint to fetch alerts
    @Transactional(readOnly = true)
    public List<Alert> getUserAlerts(Long userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }


    private List<Long> getUsersWhoFavoritedLocation(String locationId) {
        try {
            return webclient.build()
                    .get()
                    .uri(userServiceUrl + "/api/user/favorites/location/{locationId}/users", locationId)
                    .retrieve()
                    .bodyToFlux(Long.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch users who favorited location {}: {}", locationId, e.getMessage());
            return List.of();
        }
    }

    private List<Long> getUsersInLocation(String city, String stateCode, String countryCode) {
        try {
            return webclient.build()
                    .get()
                    .uri(userServiceUrl + "/api/user/location", uriBuilder -> uriBuilder
                            .queryParam("city", city)
                            .queryParam("stateCode", stateCode)
                            .queryParam("countryCode", countryCode)
                            .build())
                    .retrieve()
                    .bodyToFlux(Long.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch users in location: {}", e.getMessage());
            return List.of();
        }
    }
}