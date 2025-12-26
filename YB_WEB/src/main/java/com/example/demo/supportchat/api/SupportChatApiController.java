package com.example.demo.supportchat.api;

import com.example.demo.domain.Member;
import com.example.demo.supportchat.domain.ChatSession;
import com.example.demo.supportchat.domain.ChatMessage;
import com.example.demo.supportchat.service.CurrentMemberProvider;
import com.example.demo.supportchat.service.SupportChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportChatApiController {

    private final SupportChatService chatService;
    private final CurrentMemberProvider current;

    public SupportChatApiController(SupportChatService chatService, CurrentMemberProvider current) {
        this.chatService = chatService;
        this.current = current;
    }

    // 유저: 상담 요청
    @PostMapping("/request")
    public ResponseEntity<Long> request(HttpSession session) {
        Member me = current.get(session);
        ChatSession s = chatService.request(me.getIdx()); // ✅ Member PK getter 맞춰
        return ResponseEntity.ok(s.getId());
    }

    // 대화 이력
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> history(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.history(sessionId));
    }
}