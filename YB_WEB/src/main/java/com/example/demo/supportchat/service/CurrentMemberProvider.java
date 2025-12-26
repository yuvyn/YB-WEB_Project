package com.example.demo.supportchat.service;

import com.example.demo.domain.Member;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class CurrentMemberProvider {

    public Member get(HttpSession session) {
        Object obj = session.getAttribute("loginMember"); // ✅ 너 프로젝트에서 쓰는 키로 맞춰
        if (obj == null) throw new IllegalStateException("로그인이 필요합니다.");
        return (Member) obj;
    }

    public void requireAdmin(Member m) {
        if (!"ADMIN".equalsIgnoreCase(m.getRole())) { // role 필드명/Getter 맞춰
            throw new SecurityException("관리자만 가능합니다.");
        }
    }
}