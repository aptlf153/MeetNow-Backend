package com.example.meetnow.dto;

public class DTO {
    
	private String name;
	private String nickname;
	private String gender;
	private String userid;
	private String password;
	private String phone;
	private String email;
    
    // 기본 생성자
    public DTO() {
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "DTO [name=" + name + ", nickname=" + nickname + ", gender=" + gender + ", userid=" + userid
				+ ", password=" + password + ", phone=" + phone + ", email=" + email + "]";
	}
	
}