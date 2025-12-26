package com.example.demo.supportchat.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private SenderRole senderRole;

    private Long senderMemberId;

    @Column(nullable=false, length=2000)
    private String message;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static ChatMessage of(Long sessionId, SenderRole role, Long senderMemberId, String msg){
        ChatMessage m = new ChatMessage();
        m.sessionId = sessionId;
        m.senderRole = role;
        m.senderMemberId = senderMemberId;
        m.message = msg;
        m.createdAt = LocalDateTime.now();
        return m;
    }

    public Long getSessionId(){ return sessionId; }
    public SenderRole getSenderRole(){ return senderRole; }
    public Long getSenderMemberId(){ return senderMemberId; }
    public String getMessage(){ return message; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}