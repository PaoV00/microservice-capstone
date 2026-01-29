package com.capstone.userservice.service;

import com.capstone.userservice.dto.LocationResponse;
import com.capstone.userservice.dto.UserRequest;
import com.capstone.userservice.dto.UserResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .email(userRequest.getEmail())
                .favoriteLocationIds(userRequest.getFavoriteLocationIds() != null
                        ? userRequest.getFavoriteLocationIds() : new ArrayList<>())
                .build();



        userRepository.save(newUser);
        log.info("User created: {}", newUser.getUserId());
        return mapToUserResponse(newUser);
    }

    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        log.info("User found: {}", user.getUserId());
        return mapToUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToUserResponse).toList();
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

        if (updates.containsKey("locationId")) {
            user.updateLocationId((String) updates.get("locationId"));
            log.info("LocationId updated: {}", user.getLocationId());
        }

        if (updates.containsKey("favoriteLocationIds")) {
            Object listObj = updates.get("favoriteLocationIds");
            if (listObj instanceof List<?> list) {
                user.setFavoriteLocationIds(
                        list.stream()
                                .map(Object::toString)
                                .toList()
                );
            } else {
                throw new BadInputException("favoriteLocationIds must be a list");
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

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .locationId(user.getLocationId())
                .favoriteLocationIds(user.getFavoriteLocationIds())
                .build();
    }

    private String getLocationId(Address address) {
        LocationResponse locationId = webClient.build().get()
                .uri("http://location-service/api/location",)
    }

}
