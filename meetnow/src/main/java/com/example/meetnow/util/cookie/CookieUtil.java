package com.example.meetnow.util.cookie;

import jakarta.servlet.http.HttpServletResponse;

public interface CookieUtil {
    void addCookies(HttpServletResponse response, String accessToken, String refreshToken);
}

