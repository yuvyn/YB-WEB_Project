package com.example.demo.domain;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "board_post")
public class BoardPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false, length = 20)
    private BoardType boardType;   // NOTICE / UPDATE / FREE / QNA

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(length = 50)
    private String writer;

    @Column(name = "member_id")
    private Long memberId;
    
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "notice_pin", nullable = false)
    private boolean noticePin = false;  // 공지 상단 고정 여부

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "qna_category", length = 20)
    private String qnaCategory;
    
    @Column(name = "qna_status", length = 20)
    private String qnaStatus = "RECEIVED";

    @Column(name = "secret", nullable = false)
    private boolean secret = false;   // 🔹 비밀글 여부
    
    // === 생성자 ===
    public BoardPost() {}

    public BoardPost(BoardType boardType, String title, String content, String writer, Long memberId) {
        this.boardType = boardType;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.memberId = memberId;
    }

    // === getter/setter ===

    public Long getId() { return id; }

    public BoardType getBoardType() { return boardType; }
    public void setBoardType(BoardType boardType) { this.boardType = boardType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }
    
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public int getViewCount() { return viewCount; }
    public void increaseViewCount() { this.viewCount++; }

    public boolean isNoticePin() { return noticePin; }
    public void setNoticePin(boolean noticePin) { this.noticePin = noticePin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 🔹 NEW 표시용 (DB 컬럼 아님)
    @Transient
    public boolean getIsNew() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusDays(3));
    }
    
    public String getQnaCategory() {
        return qnaCategory;
    }

    public void setQnaCategory(String qnaCategory) {
        this.qnaCategory = qnaCategory;
    }

    // 🔹 화면에서 쓸 한글 라벨
    @Transient
    public String getQnaCategoryLabel() {
        if (qnaCategory == null) return "기타 문의";
        return switch (qnaCategory) {
            case "ACCOUNT" -> "계정/로그인";
            case "PAY"     -> "결제/캐시";
            case "BUG"     -> "게임 오류/버그";
            case "SUGGEST" -> "건의/피드백";
            case "ETC"     -> "기타 문의";
            default        -> "기타 문의";
        };
    }
    
    public String getQnaStatus() {
        return qnaStatus;
    }

    public void setQnaStatus(String qnaStatus) {
        this.qnaStatus = qnaStatus;
    }

    @Transient
    public String getQnaStatusLabel() {
        if (qnaStatus == null) return "접수";
        return switch (qnaStatus) {
            case "RECEIVED"    -> "접수";
            case "IN_PROGRESS" -> "처리중";
            case "DONE"        -> "답변완료";
            default            -> "접수";
        };
    }
}