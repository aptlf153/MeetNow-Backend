package com.example.meetnow.service.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class AdminLoginService {

    private final JdbcTemplate jdbcTemplate;

    public AdminLoginService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean verifyLogin(String userid, String rawPassword) {
        String sql = "SELECT password FROM admin WHERE id = ?";
        try {
            List<String> storedHashList = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("password"), userid);
            if (storedHashList.isEmpty()) return false;

            String storedHash = storedHashList.get(0);
            String inputHash = sha512(rawPassword);

            return storedHash.equalsIgnoreCase(inputHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String sha512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 해시 생성 실패", e);
        }
    }
}
