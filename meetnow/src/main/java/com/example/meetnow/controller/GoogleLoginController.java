package com.example.meetnow.controller;

import com.example.meetnow.repository.CookieUtil;
import com.example.meetnow.service.RegisterUser;
import com.example.meetnow.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.UUID;

@Controller
public class GoogleLoginController {

    @Value("${googleCLIENTID}")
    private String CLIENT_ID;

    @Value("${googleCLIENTSECRET}")
    private String CLIENT_SECRET;

    @Value("${googleREDIRECTURI}")
    private String REDIRECT_URI;

    @Autowired
    private RegisterUser registerUser;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

    @GetMapping("/auth/google/callback")
    public ResponseEntity<Void> googlecallback(@RequestParam("code") String code, HttpServletResponse responseToken) {

        RestTemplate rt = new RestTemplate();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", CLIENT_ID);
        formData.add("client_secret", CLIENT_SECRET);
        formData.add("redirect_uri", REDIRECT_URI);
        formData.add("code", code);

        // HTTP 요청
        ResponseEntity<String> response = rt.postForEntity(
                "https://oauth2.googleapis.com/token",
                formData,
                String.class
        );

        String responseBody = response.getBody();

        System.out.println("액세스 토큰 응답: " + responseBody);

        // JSON 파싱 (Jackson 라이브러리 사용)
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String accessToken = jsonNode.get("access_token").asText();
            System.out.println("액세스 토큰: " + accessToken);

            // 사용자 정보 요청
            String apiUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken); // 액세스 토큰 설정

            HttpEntity request = new HttpEntity<>(headers);

            ResponseEntity<String> userInfoResponse = rt.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            String userInfoBody = userInfoResponse.getBody();

            // 사용자 정보 JSON 파싱
            JsonNode userInfoJson = objectMapper.readTree(userInfoBody);

            String id = userInfoJson.get("sub").asText();
            String name = userInfoJson.get("name").asText();
            String email = userInfoJson.get("email").asText();

            boolean idCheck = registerUser.idCheck(id.toString());

            if (!idCheck) {
                String Password = UUID.randomUUID().toString();
                String sql = "INSERT INTO user (name,nickname,gender,userid,password,phone,email) VALUES (?,?,?,?,?,?,?)";
                jdbcTemplate.update(sql, name + "_google", email + "_google", "other", id + "_google", Password, "000000000000", email + "_google");
            }

            String accessToken1 = jwtUtil.generateAccessToken(id.toString());
            String refreshToken = jwtUtil.generateRefreshToken(id.toString());

            // 현재 시간
            Timestamp createdDate = new Timestamp(System.currentTimeMillis());
            // 만료 시간 (7일 후)
            Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + 604800000); // 7일 후

            String sql = "INSERT INTO refresh_tokens (userid, refreshtoken,expiration_date,created_date) VALUES (?,?,?,?)";
            jdbcTemplate.update(sql, id.toString(), refreshToken, createdDate, expirationDate);

            // 쿠키 만들기 컴포넌트
            cookieUtil.addCookies(responseToken, accessToken1, refreshToken);

            // 리다이렉션 URL 설정
            String redirectUrl = "http://localhost:5173/MeetingsPage";

            // HTTP 헤더 생성 및 리다이렉션 URL 설정
            HttpHeaders headers1 = new HttpHeaders();
            headers1.add("Location", redirectUrl);

            // ResponseEntity 반환
            return new ResponseEntity<>(headers1, HttpStatus.FOUND);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 오류 발생 시 INTERNAL_SERVER_ERROR 반환
        }
    }
}
