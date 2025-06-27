package com.example.meetnow.repository.user;

import com.example.meetnow.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsersRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE " +
           "(u.userid LIKE %:keyword% OR u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    List<User> searchUsersByUseridOrEmail(@Param("keyword") String keyword);
}
