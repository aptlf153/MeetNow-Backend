package com.example.meetnow.controller.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.meetnow.service.auth.GoogleOAuthService;

@RestController
@RequestMapping("/auth/google")
public class GoogleLoginController {

    private final GoogleOAuthService googleOAuthService;

    public GoogleLoginController(GoogleOAuthService googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code, HttpServletResponse response) {
        return googleOAuthService.login(code, response);
    }
}
