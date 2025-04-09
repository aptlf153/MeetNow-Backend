package com.example.meetnow.Entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens") // 데이터베이스의 테이블 이름

public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long id;
    private String userid;
    private String refreshtoken; 
    private Timestamp expiration_date; //만료시간
    private Timestamp created_date; // 토큰 시작 시간간
    
    public RefreshToken() {
        // 기본 생성자
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}


	public String getRefreshtoken() {
		return refreshtoken;
	}

	public void setRefreshtoken(String refreshtoken) {
		this.refreshtoken = refreshtoken;
	}

	public Timestamp getExpiration_date() {
		return expiration_date;
	}

	public void setExpiration_date(Timestamp expiration_date) {
		this.expiration_date = expiration_date;
	}

	public Timestamp getCreated_date() {
		return created_date;
	}

	public void setCreated_date(Timestamp created_date) {
		this.created_date = created_date;
	}
    
    
}
