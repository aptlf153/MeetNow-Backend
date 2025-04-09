package com.example.meetnow.repository;

import jakarta.servlet.http.HttpServletResponse;

public interface CookieUtil {
    void addCookies(HttpServletResponse response, String accessToken, String refreshToken);
}

