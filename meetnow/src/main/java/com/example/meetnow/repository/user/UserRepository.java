package com.example.meetnow.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.meetnow.Entity.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserid(String userid);
    boolean existsByNickname(String nickname);
}
