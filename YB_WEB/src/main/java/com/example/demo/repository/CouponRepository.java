package com.example.demo.repository;

import com.example.demo.domain.Coupon;
import com.example.demo.domain.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findByMemberIdAndStatus(Long memberId, CouponStatus status);

    List<Coupon> findByMemberIdAndStatusIn(Long memberId, List<CouponStatus> statuses);

    Optional<Coupon> findByMemberIdAndCode(Long memberId, String code);
}