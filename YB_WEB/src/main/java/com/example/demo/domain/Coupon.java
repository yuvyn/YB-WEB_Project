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