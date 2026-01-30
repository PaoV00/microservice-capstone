
package com.capstone.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Email(message = "Please provide a valid email address")
    @NotBlank
    private String email;

    @Embedded
    private Address address;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "location_id")
    private Set<String> favoriteLocationIds = new HashSet<>();

    public void updateFirstName(String newFirst) {
        if (newFirst != null && !newFirst.equals(this.firstName)) this.firstName = newFirst;
    }

    public void updateLastName(String newLast) {
        if (newLast != null && !newLast.equals(this.lastName)) this.lastName = newLast;
    }

    public void updateUsername(String newUsername) {
        if (newUsername != null && !newUsername.isBlank() && !newUsername.equals(this.username)) this.username = newUsername;
    }

    public void updateEmail(String newEmail) {
        if (newEmail != null && !newEmail.isBlank() && (this.email == null || !newEmail.equals(this.email))) {
            this.email = newEmail;
        }
    }

    public void updatePassword(String newPassword) {
        if (newPassword != null && !newPassword.isBlank() && !newPassword.equals(this.password)) this.password = newPassword;
    }
}
