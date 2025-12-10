package com.example.demo.controller;

import com.example.demo.domain.Member;
import com.example.demo.domain.BoardPost;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.BoardPostRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mypage")
public class MyPageController {

    private final MemberRepository memberRepository;
    private final BoardPostRepository boardPostRepository;

    public MyPageController(MemberRepository memberRepository,
                            BoardPostRepository boardPostRepository) {
        this.memberRepository = memberRepository;
        this.boardPostRepository = boardPostRepository;
    }

    // 🔹 세션에서 로그인 회원 꺼내기
    private Member getLoginMember(HttpSession session) {
        return (Member) session.getAttribute("loginMember");
    }

    // 🔹 마이페이지 메인 화면
    @GetMapping
    public String mypage(Model model,
                         HttpSession session,
                         @RequestParam(value = "tab", required = false) String tab,
                         RedirectAttributes redirectAttributes) {

        Member loginMember = getLoginMember(session);
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("globalMsg", "로그인 후 이용 가능합니다.");
            return "redirect:/login";
        }

        Member member = memberRepository.findById(loginMember.getIdx())
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        // 내가 작성한 게시글 목록 (최근 20개 예시)
        List<BoardPost> myPosts =
                boardPostRepository.findTop20ByMemberIdOrderByCreatedAtDesc(member.getIdx());

        model.addAttribute("member", member);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("myComments", java.util.Collections.emptyList());

        // 🔹 기본 탭은 profile, 쿼리 파라미터로 들어오면 그걸 사용
        String activeTab = (tab != null && !tab.isBlank()) ? tab : "profile";
        model.addAttribute("activeTab", activeTab);

        return "login/mypage";   // templates/mypage.html
    }

    // 🔹 프로필 / 정보 수정
    @PostMapping("/profile")
    public String updateProfile(@RequestParam("nickname") String nickname,
                                @RequestParam(value = "name", required = false) String name,
                                @RequestParam("email") String email,
                                @RequestParam(value = "phone", required = false) String phone,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Member loginMember = getLoginMember(session);
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("globalMsg", "로그인 후 이용 가능합니다.");
            return "redirect:/login";
        }

        Member member = memberRepository.findById(loginMember.getIdx())
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        member.setNickname(nickname);
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);

        memberRepository.save(member);

        // 헤더에서 쓰는 세션 값도 업데이트
        session.setAttribute("loginMember", member);

        redirectAttributes.addFlashAttribute("globalMsg", "회원 정보가 수정되었습니다.");
        return "redirect:/mypage";
    }

 // 🔹 비밀번호 강도 체크 (영문 + 숫자 + 특수문자, 8~20자)
    private boolean isStrongPassword(String password) {
        if (password == null) return false;
        // 최소 8~20자, 영문/숫자/특수문자 각각 1개 이상
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,20}$";
        return password.matches(pattern);
    }

    // 🔹 비밀번호 변경
    @PostMapping("/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Member loginMember = getLoginMember(session);
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("globalMsg", "로그인 후 이용 가능합니다.");
            return "redirect:/login";
        }

        Member member = memberRepository.findById(loginMember.getIdx())
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        // ❌ 기존 비밀번호 불일치
        if (!member.getPassword().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "현재 비밀번호가 올바르지 않습니다.");
            return "redirect:/mypage?tab=password";
        }

        // ❌ 새 비밀번호 & 확인 불일치
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "새 비밀번호와 확인이 일치하지 않습니다.");
            return "redirect:/mypage?tab=password";
        }

        // 🔥 변경 성공 → 로그아웃 후 로그인 페이지로 + 모달 표시
        member.setPassword(newPassword);
        memberRepository.save(member);

        // 🔥 변경 완료 플래그 + 모달에서 띄울 메시지
        redirectAttributes.addFlashAttribute("passwordChanged", true);
        redirectAttributes.addFlashAttribute("passwordChangeMsg", 
                "비밀번호가 정상적으로 변경되었습니다. 재 로그인해주세요.");
        
        session.invalidate();
        return "redirect:/login";
    }

 // 🔹 회원 탈퇴
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("password") String password,
                           @RequestParam(name = "agree", required = false) Boolean agree,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        Member loginMember = getLoginMember(session);
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("globalMsg", "로그인 후 이용 가능합니다.");
            return "redirect:/login";
        }

        Member member = memberRepository.findById(loginMember.getIdx())
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        // ✅ 동의 체크 안 했을 때 → 탈퇴 탭 유지 + 에러 메시지
        if (agree == null || !agree) {
            redirectAttributes.addFlashAttribute("withdrawError", "탈퇴 안내 및 동의 항목을 체크해 주세요.");
            return "redirect:/mypage?tab=withdraw";
        }

        // ✅ 비밀번호 틀렸을 때 → 탈퇴 탭 유지 + 에러 메시지
        if (!member.getPassword().equals(password)) {
            redirectAttributes.addFlashAttribute("withdrawError", "비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage?tab=withdraw";
        }

        // ✅ 실제 탈퇴 처리
        memberRepository.delete(member);

        session.invalidate();
        redirectAttributes.addFlashAttribute("withdrawSuccess", true);
        redirectAttributes.addFlashAttribute("withdrawMsg", "회원탈퇴가 정상적으로 처리되었습니다.");

        return "redirect:/";
    }
}