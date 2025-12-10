package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_master")
public class CouponMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;          // 사용자가 입력하는 코드

    @Column(nullable = false)
    private String name;          // 화면에 보여줄 이름

    @Column
    private String description;   // 설명(선택)

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected CouponMaster() {}

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public Boolean getEnabled() { return enabled; }

    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}