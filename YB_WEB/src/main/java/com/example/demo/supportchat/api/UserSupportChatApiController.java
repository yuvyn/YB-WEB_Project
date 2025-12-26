package com.example.demo.supportchat.api;

import com.example.demo.domain.Member;
import com.example.demo.supportchat.domain.ChatMessage;
import com.example.demo.supportchat.domain.ChatSession;
import com.example.demo.supportchat.domain.ChatStatus;
import com.example.demo.supportchat.service.SupportChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/support")
public class UserSupportChatApiController {

    private final SupportChatService chatService;

    public UserSupportChatApiController(SupportChatService chatService) {
        this.chatService = chatService;
    }

    private Member requireLogin(HttpSession session){
        Member m = (Member) session.getAttribute("loginMember");
        if(m == null) throw new IllegalStateException("로그인이 필요합니다.");
        return m;
    }

    @PostMapping("/request")
    public Long request(HttpSession session){
        Member user = requireLogin(session);
        ChatSession s = chatService.request(user.getIdx());
        return s.getId();
    }

    @GetMapping("/status/{sessionId}")
    public String status(@PathVariable("sessionId") Long sessionId, HttpSession session){
        Member user = requireLogin(session);
        ChatSession s = chatService.getSession(sessionId);

        if (!s.getUserMemberId().equals(user.getIdx())) {
            throw new SecurityException("본인 상담이 아닙니다.");
        }
        return s.getStatus().name(); // WAITING / ACTIVE / CLOSED
    }

    @GetMapping("/history/{sessionId}")
    public List<ChatMessage> history(@PathVariable("sessionId") Long sessionId, HttpSession session){
        Member user = requireLogin(session);
        ChatSession s = chatService.getSession(sessionId);

        if (!s.getUserMemberId().equals(user.getIdx())) {
            throw new SecurityException("본인 상담이 아닙니다.");
        }
        return chatService.history(sessionId);
    }
}