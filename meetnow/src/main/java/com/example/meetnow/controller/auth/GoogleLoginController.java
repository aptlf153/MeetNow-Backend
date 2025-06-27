package com.example.meetnow.controller.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.auth.GoogleOAuthInterface;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class GoogleLoginController {

    private final GoogleOAuthInterface googleOAuthService;

    public GoogleLoginController(GoogleOAuthInterface googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    @GetMapping("/api/auth/google/callback")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) {
        googleOAuthService.login(code, response);
    }
}
