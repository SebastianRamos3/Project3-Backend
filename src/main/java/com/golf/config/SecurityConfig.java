package com.golf.config;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

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

                        // ✅ ADD THIS - Allow rounds endpoints
                        .requestMatchers("/api/rounds/**").permitAll()

                        // Allow health check
                        .requestMatchers("/api/health").permitAll()

                        // Allow OAuth2 endpoints
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        .anyRequest().authenticated() // Secure everything else
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler));

        return http.build();
    }
}