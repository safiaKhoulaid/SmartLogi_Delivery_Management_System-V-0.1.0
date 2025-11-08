package com.smartlogi.sdms.presentation.controller.auth;

import lombok.Data;

@Data
public class AuthentificationRequest {
    private String email;
    private String password;

}
