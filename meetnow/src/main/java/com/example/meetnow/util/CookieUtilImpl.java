package com.example.meetnow.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import com.example.meetnow.repository.CookieUtil;

@Component
public class CookieUtilImpl implements CookieUtil {

    @Override
    public void addCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true) // 클라이언트에서 접근 불가
                .path("/")
                .maxAge(60 * 10) // 10분
                .sameSite("Lax") // 같은 도메인에서만
                .secure(false) // 개발 환경에서는 false로 설정
                .domain("localhost") // 로컬 환경에서만 유효
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // 클라이언트에서 접근 불가
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                .sameSite("Lax") // 같은 도메인에서만
                .secure(false) // 개발 환경에서는 false로 설정
                .domain("localhost") // 로컬 환경에서만 유효
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}
