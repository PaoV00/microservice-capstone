package com.capstone.weatherservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Primary
    public WebClient weatherWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openweathermap.org")
                .build();
    }
}


