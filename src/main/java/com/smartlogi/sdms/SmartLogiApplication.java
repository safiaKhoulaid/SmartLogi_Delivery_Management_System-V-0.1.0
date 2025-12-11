package com.smartlogi.sdms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@Slf4j
public class SmartLogiApplication {

    public static void main(String[] args) {
        log.info("Starting SmartLogiApplication");
        String profile = System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "dev");
        log.info("SPRING_PROFILES_ACTIVE = {}", profile);
        System.setProperty("spring.profiles.active", profile);
        log.info("welcome to your home");
        SpringApplication.run(SmartLogiApplication.class, args);
    }
}
