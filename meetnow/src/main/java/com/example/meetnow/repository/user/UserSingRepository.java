package com.example.meetnow.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.meetnow.Entity.Singuser;
import com.example.meetnow.Entity.User;

public interface UserSingRepository extends JpaRepository<Singuser, Long> {
	Singuser findByUserid(String userid); // 사용자 아이디로 조회
}