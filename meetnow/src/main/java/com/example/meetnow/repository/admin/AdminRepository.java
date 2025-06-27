package com.example.meetnow.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.meetnow.Entity.AdminEt;

public interface AdminRepository extends JpaRepository<AdminEt, String> {

}