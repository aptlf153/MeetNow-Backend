package com.example.meetnow.service.auth;

import jakarta.servlet.http.HttpServletResponse;

public interface GoogleOAuthInterface {
    void login(String code, HttpServletResponse response);
}
