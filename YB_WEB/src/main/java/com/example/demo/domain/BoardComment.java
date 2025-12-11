package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_comment")
public class BoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 어떤 게시글의 댓글인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BoardPost post;

    // 🔹 작성자 정보
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "writer", length = 50)
    private String writer;

    @Lob
    @Column(nullable = false)
    private String content;

    // 🔹 상위 댓글 (null이면 일반 댓글, 있으면 대댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BoardComment parent;

    @Column(name = "secret", nullable = false)
    private boolean secret = false;  // 비밀댓글 여부
    
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;   // 🔹 삭제 여부(소프트 삭제용)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public BoardComment() {}

    public BoardComment(BoardPost post, Long memberId, String writer, String content,
                        BoardComment parent, boolean secret) {
        this.post = post;
        this.memberId = memberId;
        this.writer = writer;
        this.content = content;
        this.parent = parent;
        this.secret = secret;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getter/Setter =====

    public Long getId() { return id; }

    public BoardPost getPost() { return post; }

    public void setPost(BoardPost post) { this.post = post; }

    public Long getMemberId() { return memberId; }

    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getWriter() { return writer; }

    public void setWriter(String writer) { this.writer = writer; }

    public String getContent() { return content; }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public BoardComment getParent() { return parent; }

    public void setParent(BoardComment parent) { this.parent = parent; }

    public boolean isSecret() { return secret; }

    public void setSecret(boolean secret) { this.secret = secret; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}