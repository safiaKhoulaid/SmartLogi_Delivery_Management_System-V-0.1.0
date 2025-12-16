package com.smartlogi.sdms.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/home")
    public Map<String, Object> home(@AuthenticationPrincipal OAuth2User principal) {
        // Hna katarj3i les infos dyal user (smya, email, photo...)
        return principal.getAttributes();
    }

    @GetMapping("/")
    public String index() {
        return "Ahlan, hada howa l'accueil public.";
    }
}