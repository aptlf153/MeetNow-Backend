package com.example.meetnow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //@Scheduled를 실행시켜주는 어노테이션 즉, 스케줄러 실행기
public class MeetnowApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetnowApplication.class, args);
	}

}
