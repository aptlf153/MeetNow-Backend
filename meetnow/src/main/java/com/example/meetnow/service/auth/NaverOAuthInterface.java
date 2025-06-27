package com.example.meetnow.service.auth;

import jakarta.servlet.http.HttpServletResponse;

public interface NaverOAuthInterface {
    void login(String code, HttpServletResponse response);
}
