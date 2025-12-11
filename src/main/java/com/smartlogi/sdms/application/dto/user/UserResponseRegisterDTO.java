package com.smartlogi.sdms.application.dto.user;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseRegisterDTO {

    String id;
    String nom;
    String prenom;
    String email;
    String password;
    String role;
    String token;

}
