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
@RequestMapping("/api/admin/support")
public class AdminSupportChatApiController {

    private final SupportChatService chatService;

    public AdminSupportChatApiController(SupportChatService chatService) {
        this.chatService = chatService;
    }

    private Member requireLogin(HttpSession session){
        Member m = (Member) session.getAttribute("loginMember");
        if(m == null) throw new IllegalStateException("로그인이 필요합니다.");
        return m;
    }
    private Member requireAdmin(HttpSession session){
        Member m = requireLogin(session);
        if(!"ADMIN".equalsIgnoreCase(m.getRole())) throw new IllegalStateException("어드민만 가능합니다.");
        return m;
    }

    // 대기 목록
    @GetMapping("/waiting")
    public List<ChatSession> waiting(HttpSession session){
        requireAdmin(session);
        return chatService.listWaiting();
    }

    // 다음 상담 수락(한 명씩)
    @PostMapping("/accept-next")
    public Long acceptNext(HttpSession session){
        Member admin = requireAdmin(session);
        ChatSession s = chatService.acceptNext(admin.getIdx());
        return s.getId();
    }

    // 특정 세션 수락
    @PostMapping("/{sessionId}/accept")
    public Long accept(@PathVariable("sessionId") Long sessionId, HttpSession session){
        Member admin = requireAdmin(session);
        ChatSession s = chatService.accept(sessionId, admin.getIdx());
        return s.getId();
    }

    // 히스토리
    @GetMapping("/history/{sessionId}")
    public List<ChatMessage> history(@PathVariable("sessionId") Long sessionId, HttpSession session){
        requireAdmin(session);
        return chatService.history(sessionId);
    }

    // 종료
    @PostMapping("/{sessionId}/close")
    public ResponseEntity<Void> close(@PathVariable("sessionId") Long sessionId, HttpSession session) {
        Member admin = requireAdmin(session);
        chatService.close(sessionId, admin.getIdx());
        return ResponseEntity.ok().build();
    }

    // 상태 확인(필요시)
    @GetMapping("/status/{sessionId}")
    public String status(@PathVariable("sessionId") Long sessionId, HttpSession session){
        requireAdmin(session);
        return chatService.getSession(sessionId).getStatus().name(); // WAITING/ACTIVE/CLOSED
    }
}