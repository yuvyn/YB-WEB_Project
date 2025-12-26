package com.example.demo.supportchat.repository;

import com.example.demo.supportchat.domain.ChatSession;
import com.example.demo.supportchat.domain.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    boolean existsByStatus(ChatStatus status);
    List<ChatSession> findByStatusOrderByCreatedAtAsc(ChatStatus status);
    Optional<ChatSession> findFirstByUserMemberIdAndStatusInOrderByCreatedAtDesc(Long userId, List<ChatStatus> statuses);
}