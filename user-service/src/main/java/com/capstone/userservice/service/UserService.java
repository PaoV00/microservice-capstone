package com.capstone.userservice.service;

import com.capstone.userservice.dto.UserRequest;
import com.capstone.userservice.dto.UserResponse;
import com.capstone.userservice.exceptions.AlreadyFavoritedException;
import com.capstone.userservice.exceptions.BadInputException;
import com.capstone.userservice.exceptions.DuplicateUserException;
import com.capstone.userservice.exceptions.UserNotFoundException;
import com.capstone.userservice.model.Address;
import com.capstone.userservice.model.User;
import com.capstone.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final WebClient.Builder webClient;

    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateUserException("Email already registered");
        }

        Address address = new Address(userRequest.getAddress());

        // Only call location-service if address is present/valid
        String locationId = getLocationId(address);
        address.setLocationId(locationId);
        log.info("Address set with locationId: {}", locationId);
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .email(userRequest.getEmail())
                .address(address)
                .build();

        userRepository.save(newUser);
        log.info("User created: {}", newUser.getUserId());
        return mapToUserResponse(newUser);
    }

    public UserResponse getUser(Long userId) {
        User user = getUserEntity(userId);
        log.info("User found: {}", user.getUserId());
        return mapToUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (updates.containsKey("firstName")) {
            user.updateFirstName((String) updates.get("firstName"));
            log.info("First name updated: {}", user.getFirstName());
        }

        if (updates.containsKey("lastName")) {
            user.updateLastName((String) updates.get("lastName"));
            log.info("Last name updated: {}", user.getLastName());
        }

        if (updates.containsKey("username")) {
            String newUsername = (String) updates.get("username");
            if (!newUsername.equals(user.getUsername())
                    && userRepository.existsByUsername(newUsername)) {
                throw new DuplicateUserException("Username already exists");
            }
            user.updateUsername(newUsername);
        }

        if (updates.containsKey("password")) {
            user.updatePassword((String) updates.get("password"));
            log.info("Password updated");
        }

        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (!newEmail.equals(user.getEmail())
                    && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateUserException("Email already registered");
            }
            try {
                user.updateEmail(newEmail);
            } catch (IllegalArgumentException ex) {
                throw new BadInputException(ex.getMessage());
            }
        }

        if(updates.containsKey("address")){
            Map<String, Object> addrUpdates = (Map<String, Object>) updates.get("address");

            Address address = user.getAddress();

            if(addrUpdates.containsKey("number")) address.setNumber(addrUpdates.get("number").toString());
            if(addrUpdates.containsKey("street")) address.setStreet(addrUpdates.get("street").toString());
            if(addrUpdates.containsKey("city")) {
                address.setCity(addrUpdates.get("city").toString());
                address.setLocationId(getLocationId(address));
            }
            if(addrUpdates.containsKey("stateCode")) {
                address.setStateCode(addrUpdates.get("stateCode").toString());
                address.setLocationId(getLocationId(address));
            }
            if(addrUpdates.containsKey("zip")) address.setZip(addrUpdates.get("zip").toString());
            if(addrUpdates.containsKey("countryCode")) {
                address.setCountryCode(addrUpdates.get("countryCode").toString());
                address.setLocationId(getLocationId(address));
            }
        }

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    public void deleteUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
        log.info("User deleted: {}", userId);
    }

    public Set<String> getFavoriteLocations(Long userId) {
        User user = getUserEntity(userId);
        UserResponse userResponse = mapToUserResponse(user);
        return userResponse.getFavoriteLocationIds();
    }

    public void addFavoriteLocation(Long userId, String locationId) {
        try {
            // verify location exists
            webClient.build().get()
                    .uri("http://location-service/api/location/" + locationId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new BadInputException("Location not found: " + locationId);
        }
        User user = getUserEntity(userId);
        // Check if location is already in favorites
        if (user.getFavoriteLocationIds().contains(locationId)) {
            throw new AlreadyFavoritedException("Location is already in favorites: " + locationId);
        }

        user = getUserEntity(userId);
        user.getFavoriteLocationIds().add(locationId);
        userRepository.save(user);
    }

    public void removeFavoriteLocation(Long userId, String locationId) {
        User user = getUserEntity(userId);
        user.getFavoriteLocationIds().remove(locationId);
        userRepository.save(user);
        log.info("Removed favorite location {} from user {}", locationId, userId);
    }

    public boolean isLocationFavorited(Long userId, String locationId) {
        User user = getUserEntity(userId);
        return user.getFavoriteLocationIds().contains(locationId);
    }

    public List<Long> getUsersByFavoriteLocationId(String locationId) {
        return userRepository.findUsersByFavoriteLocationId(locationId)
                .stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
    }

    public List<Long> getUsersByLocation(String city, String stateCode, String countryCode) {
        // Query users who live in this location (simplified - exact match)
        return userRepository.findAll().stream()
                .filter(user -> {
                    Address addr = user.getAddress();
                    return addr != null &&
                            addr.getCity().equalsIgnoreCase(city) &&
                            addr.getStateCode().equalsIgnoreCase(stateCode) &&
                            addr.getCountryCode().equalsIgnoreCase(countryCode);
                })
                .map(User::getUserId)
                .collect(Collectors.toList());
    }

    //------- Helper methods ------
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .address(user.getAddress().toDto())
                .favoriteLocationIds(user.getFavoriteLocationIds())
                .build();
    }

    private String getLocationId(Address address) {
        String locationId = webClient.build().get()
                .uri("http://location-service/api/location/id",
                uriBuilder -> uriBuilder
                        .queryParam("city", address.getCity())
                        .queryParam("stateCode", address.getStateCode())
                        .queryParam("countryCode", address.getCountryCode())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return locationId;
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
