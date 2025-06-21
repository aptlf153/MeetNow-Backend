package com.example.meetnow.controller.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.Entity.RefreshToken;
import com.example.meetnow.response.JwtResponse;
import com.example.meetnow.service.auth.TokenService;
import com.example.meetnow.util.jwt.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class TokenController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    // 토큰 검증
    @PostMapping("/api/token/check")
    public ResponseEntity<Boolean> tokenCheck(HttpServletRequest request) {
        String accessToken = extractCookie(request, "accessToken");
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        String userId = jwtUtil.extractUsername(accessToken);
        boolean isValid = jwtUtil.validateToken(accessToken, userId);

        return ResponseEntity.ok(isValid);
    }

    // 토큰 재발급
    @PostMapping("/api/token/refresh")
    public ResponseEntity<JwtResponse> getNewToken(HttpServletResponse response, HttpServletRequest request) {
        String refreshToken = extractCookie(request, "refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userId = jwtUtil.extractUsername(refreshToken);
        Optional<RefreshToken> tokenOptional = tokenService.findTokensByUserid(userId);

        String storedToken = tokenOptional.map(RefreshToken::getRefreshtoken).orElse(null);
        if (storedToken == null || !jwtUtil.validateToken(storedToken, userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 새 Access Token 발급 및 쿠키 설정
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(600)
                .sameSite("Lax")
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        return ResponseEntity.ok(new JwtResponse(newAccessToken));
    }

    // 쿠키 추출 공통 메서드
    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
