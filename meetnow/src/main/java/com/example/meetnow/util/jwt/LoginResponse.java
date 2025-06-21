package com.example.meetnow.util.jwt;

public class LoginResponse {
    private String accessToken;  // 액세스 토큰
    private String refreshToken;  // 리프레시 토큰

    // 생성자
    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getter 메서드
    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}