package com.capstone.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private AddressDto address;
    private String locationId;
    private List<String> favoriteLocationIds;
}
