package com.example.meetnow.repository.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.meetnow.Entity.RefreshToken;

public interface TokenRepository extends JpaRepository<RefreshToken, Long> {
	
	Optional<RefreshToken> findByUserid(String userid);

}
