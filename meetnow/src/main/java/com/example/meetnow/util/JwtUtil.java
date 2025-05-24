package com.example.meetnow.util;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    
	// 엑세스 토큰
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username) //토큰 주인 아이디
                .setIssuedAt(new Date(System.currentTimeMillis())) // 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + 600000)) // 토큰 유지 시간 600000
                .signWith(key) //서명할 키
                .compact();
    }
    
    //리프레시 토큰
    public String generateRefreshToken(String username) 
    {
    	return Jwts.builder()
    			.setSubject(username)
    			.setIssuedAt(new Date(System.currentTimeMillis()))
    			.setExpiration(new Date(System.currentTimeMillis() + 604800000)) //604800000
    			.signWith(key)
    			.compact();
    }    

    // JWT에서 사용자 이름 추출
    public String extractUsername(String token) {
    	
    	System.out.println(token);
    	
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            return username;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("JWT token is malformed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Token parsing failed: " + e.getMessage());
        }
        return null; // 오류 발생 시 null 반환
    }

    // JWT 검증
    public Boolean validateToken(String token, String userid) {
    	
    	
    	// extractUsername :토큰에서 유저의 아이디를 추출
        String extractedUsername = extractUsername(token); 
        
        // extractedUsername.equals : 이 아이디가 토큰을 가지고 있는 확인
        // isTokenExpired // 토큰이 만료 되었는지 확인 후 bool 반환
        return (extractedUsername.equals(userid) && !isTokenExpired(token));
    }

    // 만료 확인
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 만료 시간 추출
    private Date extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
 
    
}
