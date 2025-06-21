package com.example.meetnow.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.auth.TokenService;
import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class LogoutController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

    @PostMapping("/api/users/logout")
    public void logout(HttpServletResponse response, HttpServletRequest request) {
        String refreshToken = cookieUtil.extractRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            try {
                String userid = jwtUtil.extractUsername(refreshToken);
                tokenService.findTokensByUserid(userid)
                        .ifPresent(token -> tokenService.deleteRefreshToken(token));
            } catch (Exception e) {
                // 무시하고 쿠키만 삭제
            }
        }

        cookieUtil.deleteCookies(response, "accessToken", "refreshToken");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
