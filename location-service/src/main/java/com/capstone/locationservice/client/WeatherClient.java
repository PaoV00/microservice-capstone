package com.capstone.locationservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
public class WeatherClient {

    private final WebClient webClient;

}
