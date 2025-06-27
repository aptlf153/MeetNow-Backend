package com.example.meetnow.service.auth;

import com.example.meetnow.Entity.RefreshToken;
import com.example.meetnow.repository.auth.TokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * 특정 유저의 리프레시 토큰 조회
     */
    public Optional<RefreshToken> findTokensByUserid(String userid) {
        return tokenRepository.findByUserid(userid);
    }

    /**
     * 특정 유저의 기존 토큰 제거 (있으면 삭제)
     */
    @Transactional
    public void deleteTokenByUserid(String userid) {
    	tokenRepository.findAllByUserid(userid).forEach(tokenRepository::delete);
    }

    /**
     * 새 리프레시 토큰 저장
     */
    @Transactional
    public void saveRefreshToken(String userid, String refreshToken) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L); // 7일

        RefreshToken token = new RefreshToken();
        token.setUserid(userid);
        token.setRefreshtoken(refreshToken);
        token.setCreated_date(now);
        token.setExpiration_date(expiry);

        tokenRepository.save(token);
    }
    
    /**
     * 토큰 객체 직접 삭제 (Optional로 조회한 후 바로 전달하는 경우)
     */
    @Transactional
    public void deleteRefreshToken(RefreshToken token) {
        tokenRepository.delete(token);
    }
}
