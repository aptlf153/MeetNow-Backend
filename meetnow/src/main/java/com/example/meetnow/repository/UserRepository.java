package com.example.meetnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.meetnow.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserid(String userid); // 사용자 아이디로 조회
}
