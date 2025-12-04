package com.example.demo.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "event_post")
public class EventPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;    // 상세 설명

    @Column(name = "thumbnail_url", length = 300)
    private String thumbnailUrl;

    @Column(nullable = false, length = 20)
    private String status;     // OPEN / UPCOMING / CLOSED

    @Column(nullable = false, length = 20)
    private String category;   // WEB / INGAME / PC / COMMUNITY 등

    @Column(length = 50)
    private String platform;   // "PC / 모바일" 등

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected EventPost() {}

    public EventPost(String title, String content, String thumbnailUrl,
                     String status, String category, String platform,
                     LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.category = category;
        this.platform = platform;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // getter/setter

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getViewCount() { return viewCount; }
    public void increaseViewCount() { this.viewCount++; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Transient
    public String getPeriodText() {
        if (startDate == null && endDate == null) return "-";

        if (startDate != null && endDate != null) {
            // 2025-12-01 ~ 2025-12-31 형태
            return startDate.toString() + " ~ " + endDate.toString();
        }

        if (startDate != null) {
            return startDate.toString() + " ~";
        }

        return "~ " + endDate.toString();
    }

    @Transient
    public String getDDayLabel() {
        if (endDate == null) return "";

        java.time.LocalDate today = java.time.LocalDate.now();
        long diff = java.time.temporal.ChronoUnit.DAYS.between(today, endDate);

        if (diff < 0) {
            return "종료";
        } else if (diff == 0) {
            return "D-DAY";
        } else {
            return "D-" + diff;
        }
    }
}