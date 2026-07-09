package com.pipes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticPreviewConfig implements WebMvcConfigurer {

    @Value("${pipes.preview.root:./pipes-previews}")
    private String previewsRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(previewsRoot).toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler("/previews/**")
                .addResourceLocations(location);
    }
}