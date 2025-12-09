package com.example.demo.service;

import com.example.demo.domain.Coupon;
import com.example.demo.domain.CouponStatus;
import com.example.demo.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<Coupon> getAvailableCoupons(Long memberId) {
        return couponRepository.findByMemberIdAndStatus(memberId, CouponStatus.AVAILABLE);
    }

    public List<Coupon> getHistoryCoupons(Long memberId) {
        return couponRepository.findByMemberIdAndStatusIn(
                memberId,
                List.of(CouponStatus.USED, CouponStatus.EXPIRED)
        );
    }

    /**
     * 쿠폰 등록 (코드 입력 시)
     */
    public String registerCoupon(Long memberId, String code) {

        // 이미 등록한 쿠폰인지 검사
        if (couponRepository.findByMemberIdAndCode(memberId, code).isPresent()) {
            return "이미 등록된 쿠폰입니다.";
        }

        // 여기서는 간단히 코드에 따라 쿠폰 이름/만료일을 하드코딩 예시
        // 실제로는 coupon_master에서 조회하는 구조로 변경 가능.
        Coupon coupon = new Coupon();
        coupon.setMemberId(memberId);
        coupon.setCode(code);
        coupon.setName("포트폴리오 쿠폰 이벤트"); // 임시 이름
        coupon.setStatus(CouponStatus.AVAILABLE);
        coupon.setIssuedAt(LocalDateTime.now());
        // coupon.setExpiredAt(LocalDateTime.now().plusDays(7)); // 필요하면

        couponRepository.save(coupon);
        return "쿠폰이 등록되었습니다.";
    }

    /**
     * 사용 버튼 클릭 시
     */
    public String useCoupon(Long memberId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        if (!coupon.getMemberId().equals(memberId)) {
            return "다른 사용자의 쿠폰입니다.";
        }

        if (coupon.getStatus() != CouponStatus.AVAILABLE) {
            return "이미 사용했거나 만료된 쿠폰입니다.";
        }

        // 만료일 체크 (있다면)
        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            coupon.expire();
            return "쿠폰 사용 기간이 만료되었습니다.";
        }

        coupon.use();
        return "쿠폰을 사용했습니다.";
    }
}