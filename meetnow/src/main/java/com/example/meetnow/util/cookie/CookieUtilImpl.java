package com.example.meetnow.util.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtilImpl implements CookieUtil {

    @Override
    public void addCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(3600)
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(604800) // 7일
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        
        
    }

    @Override
    public void deleteCookies(HttpServletResponse response, String... names) {
        for (String name : names) {
            ResponseCookie cookie = ResponseCookie.from(name, null)
                    .path("/")
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(0)
                    .sameSite("None")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }

    @Override
    public String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
