package com.example.meetnow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.meetnow.Entity.User;

public interface UsernicknamelRepository extends JpaRepository<User, Long> {
    User findBynickname(String nickname); // 사용자 이메일로 조회
}