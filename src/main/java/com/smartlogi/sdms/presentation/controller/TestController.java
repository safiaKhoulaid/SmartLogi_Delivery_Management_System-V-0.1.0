package com.smartlogi.sdms.presentation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/testlog")
    public String testLog() {
        // C'est ce log que vous voulez voir dans Kibana
        log.info("CECI EST UN LOG DE TEST DEPUIS MON APPLICATION SPRING BOOT");
        log.warn("Ceci est un avertissement de test.");
        log.error("Ceci est une erreur de test.");
        return "Log de test envoyé !";
    }
}