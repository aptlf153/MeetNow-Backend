package com.example.meetnow.controller;

import java.nio.file.AccessDeniedException;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice //전역으로 선언 어노테이션
public class GlobalExceptionHandler {
	
	//@ExceptionHandler 오류 정의 어노테이션
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body("요청이 잘못되었습니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleForbidden(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.SC_FORBIDDEN).body("접근 권한이 없습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle500(Exception ex) {
        ex.printStackTrace(); // 콘솔 로그 출력

        return ResponseEntity
                .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
}
