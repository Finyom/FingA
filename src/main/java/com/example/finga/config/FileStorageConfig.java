
package com.example.finga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${product.upload-dir:uploads/products}")
    private String productUploadDir;

    @Value("${shop.upload-dir:uploads/shops}")
    private String shopUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path productPath = Paths.get(productUploadDir)
                .toAbsolutePath()
                .normalize();

        Path shopPath = Paths.get(shopUploadDir)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/api/uploads/products/**")
                .addResourceLocations(productPath.toUri().toString());

        registry.addResourceHandler("/api/uploads/shops/**")
                .addResourceLocations(shopPath.toUri().toString());
    }
}
