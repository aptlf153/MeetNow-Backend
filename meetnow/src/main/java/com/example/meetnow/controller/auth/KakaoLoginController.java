package com.example.meetnow.controller.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.meetnow.service.auth.KakaoOAuthService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class KakaoLoginController {

    private static final Logger logger = LoggerFactory.getLogger(KakaoLoginController.class);

    private final KakaoOAuthService kakaoOAuthService;

    public KakaoLoginController(KakaoOAuthService kakaoOAuthService) {
        this.kakaoOAuthService = kakaoOAuthService;
    }

    @GetMapping("/api/auth/kakao/callback")
    public void kakaocallback(@RequestParam("code") String code, HttpServletResponse response) {
        logger.info("[DEBUG] KakaoOAuthService.login 시작");
        kakaoOAuthService.login(code, response);
    }
}
