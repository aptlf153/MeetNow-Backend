package com.example.meetnow.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface OAuthService {
    ResponseEntity<Void> login(String code, HttpServletResponse response);
}