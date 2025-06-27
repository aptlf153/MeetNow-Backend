package com.example.meetnow.controller.auth;

import com.example.meetnow.service.auth.NaverOAuthInterface;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NaverController {

    private final NaverOAuthInterface naverOAuthService;

    public NaverController(NaverOAuthInterface naverOAuthService) {
        this.naverOAuthService = naverOAuthService;
    }

    @GetMapping("/api/auth/naver/callback")
    public void naverCallback(@RequestParam("code") String code, HttpServletResponse response) {
        naverOAuthService.login(code, response);
    }
}
