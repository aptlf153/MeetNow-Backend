package com.example.meetnow.controller.auth;

import com.example.meetnow.service.auth.KakaoOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KakaoLoginController {

    @Autowired
    private KakaoOAuthService kakaoOAuthService;

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<Void> kakaocallback(String code, HttpServletResponse response) {
        return kakaoOAuthService.login(code, response);
    }
}
