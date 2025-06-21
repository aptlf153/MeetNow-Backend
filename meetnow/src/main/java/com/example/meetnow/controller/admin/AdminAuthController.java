package com.example.meetnow.controller.admin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List; // List 임포트 추가

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;
import com.example.meetnow.dto.common.DTO;

import jakarta.servlet.http.HttpServletResponse; // HttpServletResponse 임포트

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // React 앱의 URL
public class AdminAuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

    // --- SHA-512 해시 함수 ---
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
            // SHA-512 알고리즘을 찾을 수 없는 경우 (거의 발생하지 않음)
            throw new RuntimeException("SHA-512 해시 생성 실패", e);
        }
    }

    // --- 로그인 검증 메서드 ---
    private boolean verifyLogin(String userid, String rawPassword) {
        String sql = "SELECT password FROM admin WHERE id = ?";
        try {
            // jdbcTemplate.query를 사용하여 결과를 List<String>으로 받음
            // deprecated된 Object[] 대신 가변인자(userid) 사용
            List<String> storedHashList = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("password"), userid);

            // 결과 리스트가 비어있으면 해당 userid가 없는 것
            if (storedHashList.isEmpty()) {
                return false;
            }

            // 결과가 있다면 첫 번째(유일한) 해시 값을 가져옴
            String storedHash = storedHashList.get(0);

            // 입력된 비밀번호를 SHA-512로 해싱
            String inputHash = sha512(rawPassword);

            // 저장된 해시와 입력 해시를 대소문자 구분 없이 비교
            return storedHash.equalsIgnoreCase(inputHash);

        } catch (Exception e) {
            // SQL 실행 오류 등 예외 발생 시 로그인 실패 처리
            // 실제 운영 환경에서는 로깅 등을 통해 예외를 기록하는 것이 좋습니다.
            e.printStackTrace(); // 개발 중 디버깅을 위해 예외 출력
            return false;
        }
    }

    // --- 로그인 API 엔드포인트 ---
    @PostMapping("/api/admin/login")
    public ResponseEntity<Boolean> login(@RequestBody DTO data, HttpServletResponse response) {
        // verifyLogin 메서드를 호출하여 로그인 유효성 검사
        boolean success = verifyLogin(data.getUserid(), data.getPassword());
        if (success) {
            // 로그인 성공 시 JWT 액세스 토큰 및 리프레시 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(data.getUserid());
            String refreshToken = jwtUtil.generateRefreshToken(data.getUserid());

            // 리프레시 토큰 만료 시간 설정 (예: 7일 후)
            Timestamp createdDate = new Timestamp(System.currentTimeMillis());
            Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + 604800000L); // 7일 (밀리초)

            // 해당 userid의 기존 리프레시 토큰 삭제
            String deleteSql = "DELETE FROM refresh_tokens WHERE userid = ?";
            jdbcTemplate.update(deleteSql, data.getUserid());

            // 새 리프레시 토큰을 데이터베이스에 저장
            String insertSql = "INSERT INTO refresh_tokens (userid, refreshtoken, expiration_date, created_date) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, data.getUserid(), refreshToken, expirationDate, createdDate);

            // 생성된 토큰들을 HTTP 응답 쿠키에 추가
            cookieUtil.addCookies(response, accessToken, refreshToken);

            // 클라이언트에 로그인 성공 응답 (true 반환)
            return ResponseEntity.ok(true);
        } else {
            // 로그인 실패 시 401 Unauthorized 상태 코드 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false); // 실패 시 body에 false를 명시적으로 반환
        }
    }
}
