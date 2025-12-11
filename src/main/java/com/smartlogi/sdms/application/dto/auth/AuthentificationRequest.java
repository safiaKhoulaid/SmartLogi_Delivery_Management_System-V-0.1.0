package com.smartlogi.sdms.application.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class AuthentificationRequest {
    private String email;
    private String password;

}
