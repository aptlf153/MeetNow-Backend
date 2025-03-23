package com.example.meetnow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetnow.service.UserService;
//import com.example.meetnow.service.UserService;
import com.example.meetnow.service.hashpasswordService;
import com.example.meetnow.dto.*;


@RestController
@CrossOrigin(origins = "http://localhost:5173") // React 앱의 URL
public class Controller {

	@Autowired
	private JdbcTemplate jdbcTemplate; //DB와 상호작용하기
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/")
	@ResponseBody
	public String HellowAPI() {
		return "HellowAPI";
	}
	
	@Autowired
	private hashpasswordService hashpasswordService;
	
    @PostMapping("/api/users/login")
    public boolean login(@RequestBody DTO data) {
        boolean success = userService.login(data.getUserid(), data.getPassword());
        return success;
    }
	
    @PostMapping("/api/users/signup")
    public String hello(@RequestBody DTO data) {
        // 받은 메시지를 로그에 출력
        System.out.println(data.toString());
        	
        String hashPassword = hashpasswordService.hashPassword(data);
        // 데이터베이스에 업데이트
        String sql = "INSERT INTO test_table (name,nickname,gender,userid,password,phone,email) VALUES (?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, data.getName(),data.getNickname(),data.getGender(),data.getUserid(),hashPassword,data.getPhone(),data.getEmail());
        
        return data.toString(); // 클라이언트에 응답
    }
    
    
}