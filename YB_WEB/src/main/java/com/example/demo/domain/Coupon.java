package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "coupon")
public class Coupon {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    private CouponStatus status = CouponStatus.AVAILABLE;

    private LocalDateTime issuedAt = LocalDateTime.now();
    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    /* ======================
          🔥 핵심 메서드   
       ====================== */

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public CouponStatus getStatus() {
        return status;
    }
    public void setStatus(CouponStatus status) {
        this.status = status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // 쿠폰 사용
    public void use() {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    // 쿠폰 만료 처리
    public void expire() {
        this.status = CouponStatus.EXPIRED;
        this.usedAt = LocalDateTime.now();
    }

    // getter / setter …
}