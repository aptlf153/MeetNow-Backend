package com.example.meetnow.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.meetnow.dto.DTO;

@Service
public class hashpasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hashPassword(DTO dto) {
        return encoder.encode(dto.getPassword());
    }

}