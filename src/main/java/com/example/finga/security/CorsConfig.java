
package com.example.finga.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from your deployed Angular frontend.
        config.setAllowedOrigins(List.of(
                "https://finga-frontend-finga-frontend.vercel.app",
                "https://finga-frontend.vercel.app/"
        ));

        // Allow the HTTP methods used by the REST API.
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allow headers such as Content-Type and Authorization.
        config.setAllowedHeaders(List.of("*"));

        // JWT in the Authorization header does not require cookie credentials.
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
