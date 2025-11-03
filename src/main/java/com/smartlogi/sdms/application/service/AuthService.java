package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.dto.AuthResponse;
import com.smartlogi.sdms.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
}
