package com.example.meetnow.controller.auth;

import com.example.meetnow.dto.common.DTO;
import com.example.meetnow.service.auth.RegisterUserService;
import org.springframework.web.bind.annotation.*;

@RestController
public class SingupController {

    private final RegisterUserService registerUserService;

    public SingupController(RegisterUserService registerUserService) {
        this.registerUserService = registerUserService;
    }

    // 회원가입 처리
    @PostMapping("/api/users/signup")
    public String signup(@RequestBody DTO data) {
        registerUserService.register(data);
        return data.toString();
    }

    // 중복 체크
    @PostMapping("/api/users/signupcheck")
    public int signupCheck(@RequestBody DTO data) {
        return registerUserService.checkDuplicate(data);
    }
}
