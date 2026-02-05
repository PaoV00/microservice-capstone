package com.capstone.userservice.controller;

import com.capstone.userservice.dto.AddressDto;
import com.capstone.userservice.dto.FavoriteRequest;
import com.capstone.userservice.dto.UserRequest;
import com.capstone.userservice.dto.UserResponse;
import com.capstone.userservice.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user")
public class UserRest {

    private final UserService userService;

    @PostMapping
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackMethod1")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        URI loc = URI.create("/user/" + userResponse.getUserId());

        return ResponseEntity.created(loc).body(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(userService.updateUser(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/favorites")
    public ResponseEntity<Set<String>> getFavoriteLocations(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getFavoriteLocations(id));
    }

    @PostMapping("/{id}/favorites")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackMethod2")
    public ResponseEntity<Void> addFavoriteLocation(
            @PathVariable Long id,
            @RequestBody FavoriteRequest request) {
        userService.addFavoriteLocation(id, request.getLocationId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/favorites/{locationId}")
    public ResponseEntity<Void> removeFavoriteLocation(
            @PathVariable Long id,
            @PathVariable String locationId) {
        userService.removeFavoriteLocation(id, locationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/favorites/{locationId}")
    public ResponseEntity<Boolean> isLocationFavorited(
            @PathVariable Long id,
            @PathVariable String locationId) {
        return ResponseEntity.ok(userService.isLocationFavorited(id, locationId));
    }

    //------- for alert service ---------
    @GetMapping("/favorites/location/{locationId}/users")
    public ResponseEntity<List<Long>> getUsersWhoFavoritedLocation(@PathVariable String locationId) {
        return ResponseEntity.ok(userService.getUsersByFavoriteLocationId(locationId));
    }

    @GetMapping("/location")
    public ResponseEntity<List<Long>> getUsersByLocation(
            @RequestParam String city,
            @RequestParam String stateCode,
            @RequestParam String countryCode) {
        return ResponseEntity.ok(userService.getUsersByLocation(city, stateCode, countryCode));
    }

    public ResponseEntity<UserResponse> fallbackMethod1(UserRequest userRequest, Throwable throwable) {
        log.error("Fallback: {}", throwable.getMessage());
        return ResponseEntity.status(503).body(
                UserResponse.builder()
                        .userId(0L)
                        .firstName("Service")
                        .lastName("Unavailable")
                        .username("temporary")
                        .email("temp@example.com")
                        .address(new AddressDto())
                        .favoriteLocationIds(new HashSet<>())
                        .build()
        );
    }

    public ResponseEntity<Void> fallbackMethod2(Long id, FavoriteRequest request, Throwable throwable) {
        log.error("Fallback: {}", throwable.getMessage());
        return ResponseEntity.status(503).build();
    }
}
