package com.example.meetnow.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.meetnow.Entity.User;
import com.example.meetnow.repository.user.UserIdRepository;
import com.example.meetnow.repository.user.UseremailRepository;
import com.example.meetnow.repository.user.UsernicknamelRepository;

@Service
public class RegisterUserService {
	
	    @Autowired
	    private UserIdRepository useridRepository;
	
	    @Autowired
	    private UseremailRepository useremailRepository;	  
	    
	    @Autowired
	    private  UsernicknamelRepository usernicknamelRepository;
	    
	    
	    // 회원가입 아이디 중복 확인
	    public Boolean idCheck(String userid) {
	    	
		    User id = useridRepository.findByUserid(userid);
		    
		    System.out.println(id);
		    
		    if(id != null) 
		    {
		    	return true;
		    }		    
		    
		    return false;
	    }
	    
	 // 회원가입 이메일 중복 확인
	    public Boolean emailCheck(String useremail) {
	    	
		    User email = useremailRepository.findByemail(useremail); // DB에서 사용자 조회
		    
		    
		    if(email != null) 
		    {
		    	return true;
		    }		    
		    
		    return false;
	    }
	    
	 // 회원가입 닉네임 중복 확인
	    public Boolean nicknameCheck(String nickname) {
	    	
		    User usernickname = usernicknamelRepository.findBynickname(nickname); // DB에서 사용자 조회
		    
		    if(usernickname != null) 
		    {
		    	return true;
		    }		    
		    
		    return false;
	    }
	    
}
