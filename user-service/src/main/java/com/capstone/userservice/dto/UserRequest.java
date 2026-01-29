package com.capstone.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String firstName;
    private String lastName;

    @NotBlank
    @Email(message = "Please provide a valid email address")
    private String email;
    private String locationId;
    private List<String> favoriteLocationIds;
}
