package com.golf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for mobile app
                .cors(Customizer.withDefaults()) // Enable CORS
                .authorizeHttpRequests(auth -> auth
                        // Allow auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Allow Golf API endpoints
                        .requestMatchers("/api/golf-api/**").permitAll()
                        .requestMatchers("/api/courses/**").permitAll()

                        // Allow rounds endpoints
                        .requestMatchers("/api/rounds/**").permitAll()

                        // Allow health check
                        .requestMatchers("/api/health").permitAll()

                        .anyRequest().authenticated() // Secure everything else
                );

        return http.build();
    }
}