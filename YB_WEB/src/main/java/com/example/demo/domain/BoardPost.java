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
    @Column(nullable = false)
    private String content;

    @Column(length = 50)
    private String writer;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "notice_pin", nullable = false)
    private boolean noticePin = false;  // 공지 상단 고정 여부

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // === 생성자 ===
    protected BoardPost() {}

    public BoardPost(BoardType boardType, String title, String content, String writer) {
        this.boardType = boardType;
        this.title = title;
        this.content = content;
        this.writer = writer;
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

    public int getViewCount() { return viewCount; }
    public void increaseViewCount() { this.viewCount++; }

    public boolean isNoticePin() { return noticePin; }
    public void setNoticePin(boolean noticePin) { this.noticePin = noticePin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

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
}