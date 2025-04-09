package com.example.meetnow.controller;

import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.example.meetnow.dto.KakaoTokenDto;
import com.example.meetnow.dto.KakaoUserInfoDto;
import com.example.meetnow.repository.CookieUtil;
import com.example.meetnow.service.RegisterUser;
import com.example.meetnow.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class KakaoLoginController {
	
	    @Value("${kakao.api.key}")
	    private String key;
	
	    @Value("${kakao.callback.url}")
	    private String callback;
		
		@Autowired
		private JdbcTemplate jdbcTemplate; //DB와 상호작용하기
		
		@Autowired
		private RegisterUser registerUser;
		
		@Autowired
		private JwtUtil jwtUtil;
	
	    @Autowired
	    private CookieUtil cookieUtil;		
		
		@GetMapping("/auth/kakao/callback")
		public ResponseEntity<Void> kakaocallback(String code,HttpServletResponse tokenResponse) 
		{
			
			RestTemplate rt = new RestTemplate();	
			
			//Http Header 오브젝트 생성
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
			
			//HttpBody 오젝트 생성
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("grant_type", "authorization_code");
			params.add("client_id", key);
			params.add("redirect_uri", callback);
			params.add("code", code);
			
			
			//Http Header와 Body를 한 오브젝트에 담기 (왜 HttpEntity에 담냐면 exchange가 HttpEntity를 뭔함)
			HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
					new HttpEntity<>(params, headers);
			
			//Http 요청하기 POST방식으로 - 응답은 response변수로 받고 String 제널릭으로
			ResponseEntity<String> response = rt.exchange(
					"https://kauth.kakao.com/oauth/token",
					HttpMethod.POST,
					kakaoTokenRequest,
					String.class
					);
			
			ObjectMapper objectMapper = new ObjectMapper();
			KakaoTokenDto oauthToken = null;
			
			try {
				oauthToken = objectMapper.readValue(response.getBody(), KakaoTokenDto.class);
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			//====================================================================
			
			RestTemplate rqsUserInfo = new RestTemplate();
			
			HttpHeaders rqsUserInfoHeader = new HttpHeaders();
			rqsUserInfoHeader.add("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
			rqsUserInfoHeader.add("Authorization", "Bearer " + oauthToken.getAccessToken());
			
			HttpEntity<?> kakaoProfileRequest = new HttpEntity<>(rqsUserInfoHeader);
			
			
			ResponseEntity<String> rqsUserInfoEntity = rqsUserInfo.exchange(
					"https://kapi.kakao.com/v2/user/me",
					HttpMethod.POST,
					kakaoProfileRequest,
					String.class
					);

			System.out.println(rqsUserInfoEntity);
			
			ObjectMapper objectMapperProfile = new ObjectMapper();
			KakaoUserInfoDto kakaoUserInfoDto = null;
			
			try {
				kakaoUserInfoDto = objectMapperProfile.readValue(rqsUserInfoEntity.getBody(), KakaoUserInfoDto.class);
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			boolean idCheck = registerUser.idCheck(kakaoUserInfoDto.getId().toString());
			
			
			if(!idCheck) 
			{
				
	            String sql = "INSERT INTO user (name,nickname,gender,userid,password,phone,email) VALUES (?,?,?,?,?,?,?)";
	            
	            String kakaonickname = kakaoUserInfoDto.getProperties().getNickname();
	            String kakaoemail = kakaoUserInfoDto.getKakaoAccount().getEmail()+"_Kakao";
	            String kakaoid = kakaoUserInfoDto.getId()+"_Kakao";
	            String Password = UUID.randomUUID().toString();
	            
	            jdbcTemplate.update(sql, kakaonickname,kakaoemail,"other",kakaoid,Password,"000000000000",kakaoemail);
			}
			
				
            String accessToken = jwtUtil.generateAccessToken(kakaoUserInfoDto.getId().toString());
            String refreshToken = jwtUtil.generateRefreshToken(kakaoUserInfoDto.getId().toString());

            // 현재 시간
            Timestamp createdDate = new Timestamp(System.currentTimeMillis());
            // 만료 시간 (7일 후)
            Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + 604800000); // 7일 후

            String sql = "INSERT INTO refresh_tokens (userid, refreshtoken,expiration_date,created_date) VALUES (?,?,?,?)";
            jdbcTemplate.update(sql, kakaoUserInfoDto.getId().toString(), refreshToken, createdDate, expirationDate);

            //쿠키 만들기 컴포넌트
            cookieUtil.addCookies(tokenResponse, accessToken, refreshToken);         

	        // 리다이렉션 URL 설정
	        String redirectUrl = "http://localhost:5173/MeetingsPage";

	        // HTTP 헤더 생성 및 리다이렉션 URL 설정
	        HttpHeaders headers1 = new HttpHeaders();
	        headers1.add("Location", redirectUrl);

	        // ResponseEntity 반환
	        return new ResponseEntity<>(headers1, HttpStatus.FOUND);
		}
    }
