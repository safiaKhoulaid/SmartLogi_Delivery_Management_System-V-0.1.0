package com.smartlogi.sdms.application.dto.auth;

import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private Adresse adresse;

    private Telephone telephone;

    private Role role = Role.USER;
}
