package com.example.demo.service;

import com.example.demo.domain.Coupon;
import com.example.demo.domain.CouponMaster;
import com.example.demo.domain.CouponStatus;
import com.example.demo.repository.CouponMasterRepository;
import com.example.demo.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMasterRepository couponMasterRepository;

    public CouponService(CouponRepository couponRepository,  CouponMasterRepository couponMasterRepository) {
        this.couponRepository = couponRepository;
        this.couponMasterRepository = couponMasterRepository;
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

        // 공백 방지
        if (code == null || code.trim().isEmpty()) {
            return "쿠폰 코드를 입력해주세요.";
        }
        String trimmedCode = code.trim();

        // 1) 쿠폰 마스터에서 코드 유효성 검증
        CouponMaster master = couponMasterRepository.findByCode(trimmedCode)
                .orElse(null);

        if (master == null || Boolean.FALSE.equals(master.getEnabled())) {
            return "유효하지 않은 쿠폰입니다.";
        }

        // 만료일 체크 (coupon_master 기준)
        if (master.getExpiredAt() != null &&
                master.getExpiredAt().isBefore(LocalDateTime.now())) {
            return "만료된 쿠폰입니다.";
        }

        // 2) 이미 이 회원이 같은 코드로 등록한 쿠폰인지 체크
        if (couponRepository.findByMemberIdAndCode(memberId, trimmedCode).isPresent()) {
            return "이미 등록된 쿠폰입니다.";
        }

        // 3) 유효하면 실제 coupon 레코드 생성
        Coupon coupon = new Coupon();
        coupon.setMemberId(memberId);
        coupon.setCode(master.getCode());
        coupon.setName(master.getName()); // 이름은 마스터에서 가져오기
        coupon.setStatus(CouponStatus.AVAILABLE);
        coupon.setIssuedAt(LocalDateTime.now());
        // 필요하면 마스터 만료일을 복사하거나, 개별 만료일을 별도로 관리해도 됨
        coupon.setExpiredAt(master.getExpiredAt());

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