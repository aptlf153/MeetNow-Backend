package com.example.meetnow.service.auth;

import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class GoogleOAuthService implements GoogleOAuthInterface {

    @Value("${googleCLIENTID}")
    private String CLIENT_ID;

    @Value("${googleCLIENTSECRET}")
    private String CLIENT_SECRET;

    @Value("${googleREDIRECTURI}")
    private String REDIRECT_URI;

    private final RegisterUserService registerUser;
    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final TokenService tokenService;

    public GoogleOAuthService(RegisterUserService registerUser,
                              JdbcTemplate jdbcTemplate,
                              JwtUtil jwtUtil,
                              CookieUtil cookieUtil,
                              TokenService tokenService) {
        this.registerUser = registerUser;
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.tokenService = tokenService;
    }

    @Override
    public void login(String code, HttpServletResponse responseToken) {
        try {
            // 1. 구글 토큰 요청
            RestTemplate rt = new RestTemplate();
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", CLIENT_ID);
            formData.add("client_secret", CLIENT_SECRET);
            formData.add("redirect_uri", REDIRECT_URI);
            formData.add("code", code);

            ResponseEntity<String> tokenResponse = rt.postForEntity(
                    "https://oauth2.googleapis.com/token", formData, String.class
            );

            String accessToken = new ObjectMapper().readTree(tokenResponse.getBody())
                    .get("access_token").asText();

            // 2. 사용자 정보 요청
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> userInfoResponse = rt.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode userInfo = new ObjectMapper().readTree(userInfoResponse.getBody());
            String id = userInfo.get("sub").asText();
            String email = userInfo.get("email").asText();

            // 3. 닉네임 생성
            String prefix = "구글회원";
            int count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user WHERE nickname LIKE ?", Integer.class, prefix + "%"
            );
            int suffix = count + 1;
            String finalNickname;
            while (true) {
                String temp = prefix + suffix;
                int exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM user WHERE nickname = ?", Integer.class, temp
                );
                if (exists == 0) {
                    finalNickname = temp;
                    break;
                }
                suffix++;
            }

            // 4. 최초 로그인 시 회원 등록
            if (!registerUser.idCheck(id)) {
                String password = UUID.randomUUID().toString();
                jdbcTemplate.update(
                        "INSERT INTO user (name, nickname, gender, userid, password, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        "알수없음", finalNickname, "other", id, password, "00000000000", email
                );
            }

            // 5. JWT 토큰 발급
            String accessTokenJwt = jwtUtil.generateAccessToken(id);
            String refreshToken = jwtUtil.generateRefreshToken(id);

            // 6. 기존 토큰 삭제 후 새 토큰 저장
            tokenService.deleteTokenByUserid(id);
            tokenService.saveRefreshToken(id, refreshToken);

            // 7. 쿠키 저장
            cookieUtil.addCookies(responseToken, accessTokenJwt, refreshToken);

            // 8. 리다이렉트
            responseToken.sendRedirect("https://meetnow-app.site/MeetingsPage");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                responseToken.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
