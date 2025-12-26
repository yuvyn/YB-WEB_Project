package com.example.demo.supportchat.service;

import com.example.demo.supportchat.domain.*;
import com.example.demo.supportchat.repository.ChatMessageRepository;
import com.example.demo.supportchat.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Service
@Transactional
public class SupportChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public SupportChatService(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo, SimpMessagingTemplate messagingTemplate) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.messagingTemplate = messagingTemplate;
    }

    // 유저: 상담 요청(중복 방지: WAITING/ACTIVE 있으면 재사용)
    public ChatSession request(Long userMemberId) {
        return sessionRepo.findFirstByUserMemberIdAndStatusInOrderByCreatedAtDesc(
                userMemberId, List.of(ChatStatus.WAITING, ChatStatus.ACTIVE)
        ).orElseGet(() -> sessionRepo.save(ChatSession.waiting(userMemberId)));
    }

    // 어드민: 다음 요청 수락 (동시 상담 1개 보장)
    public ChatSession acceptNext(Long adminMemberId) {
        if (sessionRepo.existsByStatus(ChatStatus.ACTIVE)) {
            throw new IllegalStateException("이미 상담 중인 세션이 있습니다. 종료 후 수락하세요.");
        }

        ChatSession next = sessionRepo.findByStatusOrderByCreatedAtAsc(ChatStatus.WAITING)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("대기중 상담 요청이 없습니다."));

        next.accept(adminMemberId);
        sessionRepo.save(next);

        messageRepo.save(ChatMessage.of(next.getId(), SenderRole.SYSTEM, null, "상담원이 입장했습니다."));
        return next;
    }
    
 // ✅ 어드민: 특정 세션 수락(수락 버튼용)
    public ChatSession accept(Long sessionId, Long adminMemberId) {
        if (sessionRepo.existsByStatus(ChatStatus.ACTIVE)) {
            throw new IllegalStateException("이미 상담 중인 세션이 있습니다. 종료 후 수락하세요.");
        }

        ChatSession s = getSession(sessionId);

        if (s.getStatus() != ChatStatus.WAITING) {
            throw new IllegalStateException("대기중(WAITING) 상담만 수락할 수 있습니다.");
        }

        s.accept(adminMemberId);
        sessionRepo.save(s);

        messageRepo.save(ChatMessage.of(s.getId(), SenderRole.SYSTEM, null, "상담원이 입장했습니다."));
        return s;
    }

    public void close(Long sessionId, Long adminMemberId) {
        ChatSession s = sessionRepo.findById(sessionId).orElseThrow();
        if (s.getStatus() != ChatStatus.ACTIVE) return;

        if (s.getAdminMemberId() == null || !s.getAdminMemberId().equals(adminMemberId)) {
            throw new SecurityException("담당 상담원이 아닙니다.");
        }

        s.close();
        sessionRepo.save(s);

        // ✅ 종료 시스템 메시지 저장
        ChatMessage closedMsg = messageRepo.save(
                ChatMessage.of(sessionId, SenderRole.SYSTEM, null, "상담이 종료되었습니다.")
        );

        // ✅ 실시간 전송 (이게 핵심)
        messagingTemplate.convertAndSend("/topic/support/" + sessionId, closedMsg);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> history(Long sessionId) {
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    // 메시지 저장(권한 체크 포함)
    public ChatMessage saveMessage(Long sessionId, SenderRole role, Long senderMemberId, String text) {
        ChatSession s = sessionRepo.findById(sessionId).orElseThrow();

        if (s.getStatus() != ChatStatus.ACTIVE) {
            throw new IllegalStateException("상담이 아직 시작되지 않았습니다.");
        }

        if (role == SenderRole.USER && !s.getUserMemberId().equals(senderMemberId)) {
            throw new SecurityException("본인 상담이 아닙니다.");
        }
        if (role == SenderRole.ADMIN && !s.getAdminMemberId().equals(senderMemberId)) {
            throw new SecurityException("담당 상담원이 아닙니다.");
        }

        ChatMessage saved = messageRepo.save(ChatMessage.of(sessionId, role, senderMemberId, text));

        // ✅ 실시간 전송(유저/어드민 모두 같은 세션 토픽을 구독)
        messagingTemplate.convertAndSend("/topic/support/" + sessionId, saved);

        return saved;
    }
    
    @Transactional(readOnly = true)
    public ChatSession getSession(Long sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담 세션입니다."));
    }
    
    @Transactional(readOnly = true)
    public List<ChatSession> listWaiting() {
        return sessionRepo.findByStatusOrderByCreatedAtAsc(ChatStatus.WAITING);
    }
}