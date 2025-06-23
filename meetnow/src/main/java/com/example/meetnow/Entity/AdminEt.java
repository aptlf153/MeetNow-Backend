package com.example.meetnow.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin") // 데이터베이스의 테이블 이름
public class AdminEt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long key;
    private String id;
    private String password; // 해시된 비밀번호
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
    
    
}
