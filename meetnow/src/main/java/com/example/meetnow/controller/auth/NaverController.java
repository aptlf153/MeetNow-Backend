package com.example.meetnow.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.auth.NaverOAuthService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class NaverController {

    private final NaverOAuthService naverOAuthService;

    public NaverController(NaverOAuthService naverOAuthService) {
        this.naverOAuthService = naverOAuthService;
    }

    @GetMapping("/auth/naver/callback")
    public ResponseEntity<Void> naverCallback(@RequestParam("code") String code, HttpServletResponse response) {
        return naverOAuthService.login(code, response);
    }
}
