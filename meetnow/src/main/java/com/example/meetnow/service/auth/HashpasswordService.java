package com.example.meetnow.service.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.meetnow.dto.common.DTO;

@Service
public class HashpasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hashPassword(DTO dto) {
        return encoder.encode(dto.getPassword());
    }

}