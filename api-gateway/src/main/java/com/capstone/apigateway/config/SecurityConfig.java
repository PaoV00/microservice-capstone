//package com.capstone.apigateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfigurationSource;
////import org.springframework.security.
////import org.springframework.security.config.Customizer;
////import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
////import org.springframework.security.config.web.server.ServerHttpSecurity;
////import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
////@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    private final String[] freeResourceUrls = { "/aggregate/**", "/eureka/**"};
////    @Bean
////    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
////        http
////                .csrf(csrf -> csrf.disable())
////                .authorizeExchange(exchanges -> exchanges
////                        .pathMatchers("/eureka/**").permitAll()
////                        .anyExchange().authenticated()
////                )
////                .authorizeExchange(exchanges -> exchanges  // FOR NOW, REMOVE AFTER TESTING
////                        .pathMatchers("/**").permitAll()
////                        .anyExchange().authenticated()
////                )
////                .oauth2ResourceServer(oauth2 -> oauth2
////                        .jwt(Customizer.withDefaults())
////                );
////
////        return http.build();
////    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http.authorizeHttpRequests(authorize -> authorize
//                .requestMatchers(freeResourceUrls)
//                .permitAll()
//                .anyRequest().authenticated())
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                build();
//    }
//
//    private CorsConfigurationSource corsConfigurationSource() {
//    }
//}
