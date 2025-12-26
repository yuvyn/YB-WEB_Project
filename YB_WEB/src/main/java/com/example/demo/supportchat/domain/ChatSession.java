package com.example.demo.supportchat.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_session")
public class ChatSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long userMemberId;

    private Long adminMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ChatStatus status = ChatStatus.WAITING;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime closedAt;

    public static ChatSession waiting(Long userMemberId){
        ChatSession s = new ChatSession();
        s.userMemberId = userMemberId;
        s.status = ChatStatus.WAITING;
        s.createdAt = LocalDateTime.now();
        return s;
    }

    public void accept(Long adminMemberId){
        if(this.status != ChatStatus.WAITING) throw new IllegalStateException("이미 수락된 요청입니다.");
        this.adminMemberId = adminMemberId;
        this.status = ChatStatus.ACTIVE;
    }

    public void close(){
        this.status = ChatStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public Long getId(){ return id; }
    public Long getUserMemberId(){ return userMemberId; }
    public Long getAdminMemberId(){ return adminMemberId; }
    public ChatStatus getStatus(){ return status; }
}