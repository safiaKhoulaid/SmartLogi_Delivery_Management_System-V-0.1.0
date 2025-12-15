package com.smartlogi.sdms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SmartLogiApplicationTest {

    @Test
    void contextLoads() {
        // Ce test est simple : si l'application ne parvient pas
        // à démarrer (à charger le contexte), ce test échouera.
    }
}