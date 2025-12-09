package com.example.demo.controller;

import com.example.demo.domain.Coupon;
import com.example.demo.service.CouponService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.domain.Member;
import java.util.List;

@Controller
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // 로그인 회원 ID 가져오는 헬퍼 (세션키는 프로젝트에 맞게 수정)
    private Long getLoginMemberId(HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember"); // 세션 키 이름은 네 로그인 코드에 맞게
        if (member == null) {
            return null;
        }
        return member.getIdx();   // ★ 여기!
    }

    @GetMapping
    public String couponPage(HttpSession session, Model model,
                             @ModelAttribute("message") String message) {

        Long memberId = getLoginMemberId(session);
        if (memberId == null) {
            return "redirect:/login";
        }

        List<Coupon> available = couponService.getAvailableCoupons(memberId);
        List<Coupon> history = couponService.getHistoryCoupons(memberId);

        model.addAttribute("availableCoupons", available);
        model.addAttribute("historyCoupons", history);
        model.addAttribute("message", message);

        return "event/coupon"; // templates/coupon/coupon.html
    }

    @PostMapping("/register")
    public String registerCoupon(@RequestParam("code") String code,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Long memberId = getLoginMemberId(session);
        if (memberId == null) {
            return "redirect:/login";
        }

        String msg = couponService.registerCoupon(memberId, code);
        redirectAttributes.addFlashAttribute("message", msg);
        return "redirect:/coupon";
    }

    @PostMapping("/use/{couponId}")
    public String useCoupon(@PathVariable("couponId") Long couponId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        Long memberId = getLoginMemberId(session);
        if (memberId == null) {
            return "redirect:/login";
        }

        String msg = couponService.useCoupon(memberId, couponId);
        redirectAttributes.addFlashAttribute("message", msg);
        return "redirect:/coupon";
    }
}