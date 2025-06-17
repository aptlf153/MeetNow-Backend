package com.example.meetnow.dto; // 실제 패키지 경로로 수정해주세요.

public class MeetingWithNicknameDto {
    private Long id;
    private String title;
    private boolean closed;
    private String userNickname; // 사용자의 닉네임

    // 생성자
    public MeetingWithNicknameDto(Long id, String title, boolean closed, String userNickname) {
        this.id = id;
        this.title = title;
        this.closed = closed;
        this.userNickname = userNickname;
    }

    // Getter 메소드들
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isClosed() { return closed; }
    public String getUserNickname() { return userNickname; }
}
