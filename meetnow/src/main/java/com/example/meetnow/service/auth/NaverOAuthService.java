package com.example.meetnow.service.auth;

import com.example.meetnow.dto.auth.NaverTokenDto;
import com.example.meetnow.dto.auth.NaverUserInfoDto;
import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;
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
public class NaverOAuthService implements NaverOAuthInterface {

    @Value("${naver.client.id}")
    private String CLIENT_ID;

    @Value("${naver.client.secret}")
    private String CLIENT_SECRET;

    @Value("${naver.redirect.uri}")
    private String REDIRECT_URI;

    private final RegisterUserService registerUser;
    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final TokenService tokenService;
    private final RestTemplate restTemplate;

    public NaverOAuthService(RegisterUserService registerUser,
                             JdbcTemplate jdbcTemplate,
                             JwtUtil jwtUtil,
                             CookieUtil cookieUtil,
                             TokenService tokenService,
                             RestTemplate restTemplate) {
        this.registerUser = registerUser;
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.tokenService = tokenService;
        this.restTemplate = restTemplate;
    }

    @Override
    public void login(String code, HttpServletResponse response) {
        try {
            // 1. 네이버 토큰 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", CLIENT_ID);
            params.add("client_secret", CLIENT_SECRET);
            params.add("redirect_uri", REDIRECT_URI);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    "https://nid.naver.com/oauth2.0/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            NaverTokenDto tokenDto = objectMapper.readValue(tokenResponse.getBody(), NaverTokenDto.class);

            // 2. 사용자 정보 요청
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(tokenDto.getAccessToken());

            HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    userInfoRequest,
                    String.class
            );

            NaverUserInfoDto userInfo = objectMapper.readValue(userInfoResponse.getBody(), NaverUserInfoDto.class);

            String userId = userInfo.getResponse().getId();
            String email = userInfo.getResponse().getEmail();

            // 3. 회원 등록
            if (!registerUser.idCheck(userId)) {
                String prefix = "네이버회원";
                int count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM user WHERE nickname LIKE ?", Integer.class, prefix + "%");
                int suffix = count + 1;
                String finalNickname;
                while (true) {
                    finalNickname = prefix + suffix;
                    int exists = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM user WHERE nickname = ?", Integer.class, finalNickname);
                    if (exists == 0) break;
                    suffix++;
                }

                String password = UUID.randomUUID().toString();
                jdbcTemplate.update(
                        "INSERT INTO user (name, nickname, gender, userid, password, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        "알수없음", finalNickname, "other", userId, password, "000000000000", email
                );
            }

            // 4. JWT 토큰 생성 및 저장
            String accessToken = jwtUtil.generateAccessToken(userId);
            String refreshToken = jwtUtil.generateRefreshToken(userId);

            tokenService.deleteTokenByUserid(userId);
            tokenService.saveRefreshToken(userId, refreshToken);

            // 5. 쿠키 저장
            cookieUtil.addCookies(response, accessToken, refreshToken);

            // 6. 리다이렉트
            response.sendRedirect("https://meetnow-app.site/MeetingsPage");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
