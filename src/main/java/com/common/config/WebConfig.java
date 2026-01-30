package com.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String projectDir = System.getProperty("user.dir");
        String reportPath = "file:" + projectDir + File.separator + "build" + File.separator + "reports"
                + File.separator + "tests" + File.separator + "test" + File.separator;

        log.info("Configuring report resource handler at path: {}", reportPath);

        registry.addResourceHandler("/reports/**")
                .addResourceLocations(reportPath);
    }
}
