package com.example.meetnow.controller.auth;

import com.example.meetnow.service.auth.RegisterUserService;
import com.example.meetnow.util.cookie.CookieUtil;
import com.example.meetnow.util.jwt.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

import java.sql.Timestamp;
import java.util.UUID;

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

@Controller
public class NaverController {

    @Value("${naver.client.id}")
    private String CLIENT_ID;

    @Value("${naver.client.secret}")
    private String CLIENT_SECRET;

    @Value("${naver.redirect.uri}")
    private String REDIRECT_URI;

    @Autowired
    private RegisterUserService registerUser;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CookieUtil cookieUtil;
    
    @GetMapping("/auth/naver/callback")
    public  ResponseEntity<Void> navercallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse responseToken) {

        System.out.println("code: " + code);
        System.out.println("state: " + state);

        RestTemplate rt = new RestTemplate();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", CLIENT_ID);
        formData.add("client_secret", CLIENT_SECRET);
        formData.add("redirect_uri", REDIRECT_URI);
        formData.add("code", code);
        formData.add("state", state);

        // HTTP 요청
        ResponseEntity<String> response = rt.postForEntity(
                "https://nid.naver.com/oauth2.0/token",
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
            String apiUrl = "https://openapi.naver.com/v1/nid/me";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken); // 액세스 토큰 설정

            HttpEntity request = new HttpEntity<>(headers); //HTTP 요청을 위하 헤더

            ResponseEntity<String> userInfoResponse = rt.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            String userInfoBody = userInfoResponse.getBody();

            // 사용자 정보 JSON 파싱
            try {
                JsonNode userInfoJson = objectMapper.readTree(userInfoBody);
                JsonNode responseNode = userInfoJson.get("response");

                String id = responseNode.get("id").asText();
                
                
             //닉네임 임의 부여
             String prefix = "네이버회원";

             // 1. 대략적인 시작점 구하기 (이 자체는 매우 빠름)
             String countSql = "SELECT COUNT(*) FROM user WHERE nickname LIKE ?";
             int count = jdbcTemplate.queryForObject(countSql, Integer.class, prefix + "%");

             // 2. 예상되는 숫자부터 중복 체크하면서 증가
             int suffix = count + 1;
             String finalNickname;

             while (true) {
                 String tempNickname = prefix + suffix;

                 String checkSql = "SELECT COUNT(*) FROM user WHERE nickname = ?";
                 int exists = jdbcTemplate.queryForObject(checkSql, Integer.class, tempNickname);

                 if (exists == 0) {
                     finalNickname = tempNickname;
                     break;
                 }

                 suffix++;
             }
                
                
                String email = responseNode.get("email").asText();
                String name = responseNode.get("name").asText();
                
    			boolean idCheck = registerUser.idCheck(id.toString());
    			
    			if(!idCheck) 
    			{
    				String Password = UUID.randomUUID().toString();
    	            String sql = "INSERT INTO user (name,nickname,gender,userid,password,phone,email) VALUES (?,?,?,?,?,?,?)";
    	            jdbcTemplate.update(sql, "알수없음",finalNickname,"other",id,Password,"000000000000",email);
    			}
    			
    				
                String accessToken1 = jwtUtil.generateAccessToken(id.toString());
                String refreshToken = jwtUtil.generateRefreshToken(id.toString());

                // 현재 시간
                Timestamp createdDate = new Timestamp(System.currentTimeMillis());
                // 만료 시간 (7일 후)
                Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + 604800000); // 7일 후

                String sql = "INSERT INTO refresh_tokens (userid, refreshtoken,expiration_date,created_date) VALUES (?,?,?,?)";
                jdbcTemplate.update(sql, id.toString(), refreshToken, createdDate, expirationDate);

                //쿠키 만들기 컴포넌트
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
                return new ResponseEntity<>(HttpStatus.FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.FOUND);
        }
    }
}
