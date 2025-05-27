package com.dyslexia.dyslexia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String location = "file:///" + uploadDir + "/";
        registry.addResourceHandler("/pageImage/**").addResourceLocations(location);
    }
}
