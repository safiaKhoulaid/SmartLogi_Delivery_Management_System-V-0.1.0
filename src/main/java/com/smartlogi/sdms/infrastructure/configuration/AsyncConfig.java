package com.smartlogi.sdms.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync // 👈 METTEZ L'ANNOTATION ICI
public class AsyncConfig {
    // Cette classe peut rester vide.
    // Son seul but est d'activer @EnableAsync
    // de manière isolée pour ne pas perturber Springdoc.
}