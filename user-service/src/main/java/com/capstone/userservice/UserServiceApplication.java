package com.capstone.userservice;

import com.capstone.userservice.model.User;
import com.capstone.userservice.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(UserService userService) {
        return args -> {

            User user1 = new User();
            user1.setFirstName("John");
            user1.setLastName("Smith");
            user1.setEmail("john.smith@gmail.com");
            user1.setUsername("john.smith");
            user1.setPassword("password");

            User user2 = new User();
        };
    }
}
