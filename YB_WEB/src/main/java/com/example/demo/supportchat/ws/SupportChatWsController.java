package com.example.demo.supportchat.ws;

import com.example.demo.domain.Member;
import com.example.demo.supportchat.domain.ChatMessage;
import com.example.demo.supportchat.domain.SenderRole;
import com.example.demo.supportchat.service.SupportChatService;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SupportChatWsController {

    private final SupportChatService chatService;
    private final SimpMessagingTemplate messaging;

    public SupportChatWsController(SupportChatService chatService, SimpMessagingTemplate messaging) {
        this.chatService = chatService;
        this.messaging = messaging;
    }

    @MessageMapping("/support/{sessionId}/send")
    public void send(@DestinationVariable("sessionId") Long sessionId,
                     @Payload ChatSendRequest req,
                     SimpMessageHeaderAccessor accessor) {

        Object s = accessor.getSessionAttributes().get("HTTP_SESSION");
        if (!(s instanceof HttpSession httpSession)) {
            throw new IllegalStateException("세션이 없습니다.");
        }

        Member me = (Member) httpSession.getAttribute("loginMember");
        if (me == null) throw new IllegalStateException("로그인이 필요합니다.");

        SenderRole role = "ADMIN".equalsIgnoreCase(me.getRole()) ? SenderRole.ADMIN : SenderRole.USER;

        ChatMessage saved = chatService.saveMessage(sessionId, role, me.getIdx(), req.getText());

        messaging.convertAndSend("/topic/support/" + sessionId,
                ChatSendResponse.from(saved, me.getNickname()));
    }

    public static class ChatSendRequest {
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class ChatSendResponse {
        public Long sessionId;
        public String senderRole;
        public Long senderMemberId;
        public String senderName;
        public String message;
        public String createdAt;

        public static ChatSendResponse from(ChatMessage m, String senderName) {
            ChatSendResponse r = new ChatSendResponse();
            r.sessionId = m.getSessionId();
            r.senderRole = m.getSenderRole().name();
            r.senderMemberId = m.getSenderMemberId();
            r.senderName = senderName;
            r.message = m.getMessage();
            r.createdAt = m.getCreatedAt().toString();
            return r;
        }
    }
}