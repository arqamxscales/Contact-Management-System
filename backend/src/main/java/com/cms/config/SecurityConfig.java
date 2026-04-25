package com.cms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 * Configures password encoding and authentication mechanisms.
 */
@Configuration
public class SecurityConfig {

    /**
     * Temporary permissive filter chain for development phase.
     * We'll tighten endpoint-level authorization in a later sprint.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Password encoder bean using BCrypt algorithm.
     * BCrypt is a strong, adaptive password hashing function suitable for storing passwords.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with default strength (12 rounds) provides good security without being too slow
        return new BCryptPasswordEncoder();
    }
}
