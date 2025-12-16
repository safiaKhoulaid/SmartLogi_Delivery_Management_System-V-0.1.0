package com.smartlogi.sdms.application.dto.auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class RegisterResponse {

    private String message;
    private String email ;
}
