package com.example.finga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${shop.upload-dir:uploads/shops}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        Path uploadPath = Paths
                .get(uploadDir)
                .toAbsolutePath()
                .normalize();

        System.out.println(
                "Shop upload directory: " + uploadPath
        );

        registry
                .addResourceHandler("/uploads/shops/**")
                .addResourceLocations(
                        uploadPath.toUri().toString()
                );

        registry
                .addResourceHandler("/uploads/products/**")
                .addResourceLocations(
                        Paths.get("uploads/products")
                                .toAbsolutePath()
                                .normalize()
                                .toUri()
                                .toString()
                );
    }
}
