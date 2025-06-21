package com.example.meetnow.service.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.meetnow.Entity.RefreshToken;
import com.example.meetnow.repository.auth.TokenRepository;

import jakarta.transaction.Transactional;

@Service
public class TokenService {
    
    @Autowired
	private TokenRepository tokenrepository; 
    
	
	public Optional<RefreshToken> findTokensByUserid(String userid)
	{
		return tokenrepository.findByUserid(userid);
	}
	
	@Transactional
	public void deleteRefreshToken(RefreshToken refreshToken) {
	    tokenrepository.delete(refreshToken);
	}
	
}
