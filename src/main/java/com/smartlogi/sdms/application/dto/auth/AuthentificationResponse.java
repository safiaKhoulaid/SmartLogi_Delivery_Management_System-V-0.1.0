package com.smartlogi.sdms.application.dto.auth;


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
    private String email;
    private String token;
    private String refreshToken;

}
