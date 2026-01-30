package com.capstone.userservice;

import com.capstone.userservice.controller.UserRest;
import com.capstone.userservice.dto.UserRequest;
import com.capstone.userservice.dto.UserResponse;
import com.capstone.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRest.class)
class UserRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {
        // Arrange
        UserRequest userRequest = UserRequest.builder()
                .username("johndoe")
                .password("password123")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .userId(1L)
                .username("johndoe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        // Arrange
        UserResponse user1 = UserResponse.builder()
                .userId(1L)
                .username("user1")
                .email("user1@example.com")
                .firstName("First1")
                .lastName("Last1")
                .build();

        UserResponse user2 = UserResponse.builder()
                .userId(2L)
                .username("user2")
                .email("user2@example.com")
                .firstName("First2")
                .lastName("Last2")
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        // Act & Assert
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].userId").value(2L))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    void shouldGetUserById() throws Exception {
        // Arrange
        Long userId = 1L;
        UserResponse userResponse = UserResponse.builder()
                .userId(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userService.getUser(userId)).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // Arrange
        Long userId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetFavoriteLocations() throws Exception {
        // Arrange
        Long userId = 1L;
        Set<String> favorites = Set.of("loc1", "loc2", "loc3");

        when(userService.getFavoriteLocations(userId)).thenReturn(favorites);

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}/favorites", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]").value("loc1"))
                .andExpect(jsonPath("$[1]").value("loc2"))
                .andExpect(jsonPath("$[2]").value("loc3"));
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {
        // Arrange - Invalid request with empty username and invalid email
        UserRequest invalidRequest = UserRequest.builder()
                .username("")  // Empty username
                .email("invalid-email")  // Invalid email format
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}