package com.smartlogi.sdms.application.dto.auth;


import com.smartlogi.sdms.domain.model.enums.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthentificationResponse {

    private String massage;
    private String firstName;
    private String lastName;
    private String role ;
    private String email;
    private String token;
    private String refreshToken;

}
