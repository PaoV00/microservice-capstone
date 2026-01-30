package com.capstone.userservice;

import com.capstone.userservice.model.User;
import com.capstone.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserServiceApplicationTests {

    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:5.7.17");

    @Autowired
    private UserRepository userRepository;

    @Test
    void canSaveUser() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getUserId());
        assertTrue(userRepository.existsById(savedUser.getUserId()));
    }

    @Test
    void canFindByUsername() {
        User user = User.builder()
                .username("uniqueuser")
                .password("password")
                .email("unique@example.com")
                .firstName("Unique")
                .lastName("User")
                .build();

        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("uniqueuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }
}