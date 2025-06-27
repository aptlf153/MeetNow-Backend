package com.example.meetnow.service.auth;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.meetnow.controller.auth.KakaoLoginController;
import com.example.meetnow.dto.auth.KakaoTokenDto;
import com.example.meetnow.dto.auth.KakaoUserInfoDto;
import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class KakaoOAuthService {

    @Value("${kakao.api.key}")
    private String key;

    @Value("${kakao.callback.url}")
    private String callback;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RegisterUserService registerUser;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private TokenService tokenService;

    private static final Logger logger = LoggerFactory.getLogger(KakaoLoginController.class);
    
    public void login(String code, HttpServletResponse response) {
    	logger.info("[DEBUG] KakaoOAuthService.login 호출, code: " + code);
        try {
            // === 1. 토큰 요청 ===
            RestTemplate rt = new RestTemplate();

            //http 요청 만들기
            HttpHeaders headers = new HttpHeaders();
            
            //
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            //파라미터 키와 벨류로 담기
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", key);
            params.add("redirect_uri", callback);
            params.add("code", code);

            //파라미터와 헤더를 전송하고 그걸 쿼리 형식으로 받는 코드 작성
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

            //post로 보내기
            ResponseEntity<String> tokenResponse = rt.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            KakaoTokenDto tokenDto = objectMapper.readValue(tokenResponse.getBody(), KakaoTokenDto.class);

            // === 2. 사용자 정보 요청 ===
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(tokenDto.getAccessToken());

            HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<String> userInfoResponse = rt.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    userInfoRequest,
                    String.class
            );

            KakaoUserInfoDto userInfo = objectMapper.readValue(userInfoResponse.getBody(), KakaoUserInfoDto.class);

            // === 3. DB 저장 및 사용자 처리 ===
            String userId = userInfo.getId().toString();

            System.out.println(userId);
            
            
            if (!registerUser.idCheck(userId)) {
                // 닉네임 생성
                String prefix = "카카오회원";
                String countSql = "SELECT COUNT(*) FROM user WHERE nickname LIKE ?";
                int count = jdbcTemplate.queryForObject(countSql, Integer.class, prefix + "%");

                int suffix = count + 1;
                String finalNickname;

                while (true) {
                    finalNickname = prefix + suffix;
                    int exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE nickname = ?", Integer.class, finalNickname);
                    if (exists == 0) break;
                    suffix++;
                }

                String email = userInfo.getKakaoAccount().getEmail();
                String password = UUID.randomUUID().toString();

                String insertSql = "INSERT INTO user (name, nickname, gender, userid, password, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, "알수없음", finalNickname, "other", userId, password, "000000000000", email);
            }

            // === 4. 토큰 생성 및 저장 ===
            String accessToken = jwtUtil.generateAccessToken(userId);
            String refreshToken = jwtUtil.generateRefreshToken(userId);

            System.out.println("[DEBUG] accessToken: " + accessToken);
            System.out.println("[DEBUG] refreshToken: " + refreshToken);

            tokenService.deleteTokenByUserid(userId);
            tokenService.saveRefreshToken(userId, refreshToken);

            cookieUtil.addCookies(response, accessToken, refreshToken);

            System.out.println("[DEBUG] 쿠키 추가 완료, 리다이렉트 처리");

            // === 5. 리다이렉트 ===
            response.sendRedirect("https://meetnow-app.site/MeetingsPage");

        } catch (Exception e) {
            e.printStackTrace();
            // 에러시 적절한 에러 처리 혹은 리다이렉트 필요
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
