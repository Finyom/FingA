package com.example.finga.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/uploads/products/**")
                .addResourceLocations("file:./uploads/products/");

        registry.addResourceHandler("/api/uploads/shops/**")
                .addResourceLocations("file:./uploads/shops/");
    }
}