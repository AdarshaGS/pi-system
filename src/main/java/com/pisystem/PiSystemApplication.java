package com.pisystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.pisystem")
@EntityScan(basePackages = "com.pisystem")
@EnableJpaRepositories(basePackages = "com.pisystem")
@EnableScheduling
@org.springframework.boot.context.properties.ConfigurationPropertiesScan("com.pisystem")
public class PiSystemApplication {

        public static void main(String[] args) {
                SpringApplication.run(PiSystemApplication.class, args);
        }

}
