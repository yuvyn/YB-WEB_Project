package com.example.demo.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.domain.Member;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@Controller
public class LoginController {

    private final LoginService loginService;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final JdbcTemplate jdbcTemplate;

    public LoginController(LoginService loginService, MemberRepository memberRepository, EmailService emailService, JdbcTemplate jdbcTemplate) {
        this.loginService = loginService;
        this.memberRepository = memberRepository;
        this.emailService = emailService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // 로그인 화면
    @GetMapping("/login")
    public String loginForm(HttpServletRequest request, Model model) {

        // 1) 우선 "모달 띄우라"는 플래시가 있는지 확인
        Object flashFlag = model.asMap().get("showSecondModal");
        boolean showSecondModal = (flashFlag instanceof Boolean) && (Boolean) flashFlag;

        HttpSession session = request.getSession(false);

        // 🔹 플래시 플래그가 없으면 → 무조건 초기 화면 + 2차인증 상태 초기화
        if (!showSecondModal) {
            if (session != null) {
                // 이전에 남아 있던 tempMemberId 싹 제거 (새로 로그인 시작)
                session.removeAttribute("tempMemberId");
            }

            // 모달 관련 값들 기본값
            if (!model.containsAttribute("forceEmailTab")) {
                model.addAttribute("forceEmailTab", false);
            }
            if (!model.containsAttribute("emailSent")) {
                model.addAttribute("emailSent", false);
            }
            model.addAttribute("showSecondModal", false);

            // 🔹 비밀번호 찾기 모달도 기본값 세팅
            if (!model.containsAttribute("showPwModal")) {
                model.addAttribute("showPwModal", false);
            }
            
            return "login/login";
        }

        // 🔹 여기까지 왔다는 건 "반드시 모달을 띄우고 싶다"는 의미 (POST 이후 redirect)

        if (session == null || session.getAttribute("tempMemberId") == null) {
            // 플래그는 있는데 세션이 없으면 이상한 상태 → 그냥 초기화해서 로그인만 보여줌
            model.addAttribute("showSecondModal", false);
            return "login/login";
        }

        Long memberId = (Long) session.getAttribute("tempMemberId");
        Member member = memberRepository.findById(memberId).orElse(null);

        if (member == null) {
            // 회원도 없으면 초기화
            session.removeAttribute("tempMemberId");
            model.addAttribute("showSecondModal", false);
            return "login/login";
        }

        // 질문/이메일 세팅
        model.addAttribute("question", member.getVerifyQuestion());

        String realEmail = member.getEmail();
        String maskedEmail = realEmail;

        if (realEmail != null) {
            int atIndex = realEmail.indexOf("@");
            if (atIndex > 1) {
                String first = realEmail.substring(0, 1);
                String hidden = "*".repeat(atIndex - 1);
                String domain = realEmail.substring(atIndex);
                maskedEmail = first + hidden + domain;
            }
        }

        model.addAttribute("email", realEmail);
        model.addAttribute("maskedEmail", maskedEmail);

        // forceEmailTab / emailSent 가 플래시로 안 오면 기본값 세팅
        if (!model.containsAttribute("forceEmailTab")) {
            model.addAttribute("forceEmailTab", false);
        }
        if (!model.containsAttribute("emailSent")) {
            model.addAttribute("emailSent", false);
        }

        // 🔹 emailSent == true인데 remainSec 이 안 왔으면 기본 180초로 타이머 활성화
        boolean emailSent = false;
        Object emailSentObj = model.asMap().get("emailSent");
        if (emailSentObj instanceof Boolean && (Boolean) emailSentObj) {
            emailSent = true;
        }

        if (emailSent && !model.containsAttribute("remainSec")) {
            model.addAttribute("remainSec", 180);   // 3분
        }
        
        model.addAttribute("showSecondModal", true);

        return "login/login";
    }

	//  로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam("loginId") String loginId,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {

        Member member = loginService.login(loginId, password);

        if (member == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "아이디 또는 비밀번호가 일치하지 않아요.\n정확하게 입력해 주세요."
            );
            return "redirect:/login";
        }

        HttpSession session = request.getSession();

        // ✅ 2단계 인증 사용 여부 체크
        if (Boolean.TRUE.equals(member.getTwoFactorEnabled())
                && "QUESTION".equals(member.getTwoFactorType())) {

            session.setAttribute("tempMemberId", member.getIdx());

            // 🔥 다음 /login GET 에서만 모달 띄워라!
            redirectAttributes.addFlashAttribute("showSecondModal", true);

            return "redirect:/login";
        }

        // 2차 인증 안 쓰면 바로 로그인
        session.setAttribute("loginMember", member);
        return "redirect:/";
    }
    

    // 로그아웃
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
    
 // 회원가입 화면
    @GetMapping("/join")
    public String joinForm(Model model) {
        // 에러로 돌아올 때는 기존 값 그대로, 처음 들어오면 빈 값 세팅
        if (!model.containsAttribute("loginId")) {
            model.addAttribute("loginId", "");
            model.addAttribute("name", "");
            model.addAttribute("nickname", "");
            model.addAttribute("phone", "");
            model.addAttribute("email", "");
            model.addAttribute("birth", "");
            model.addAttribute("gender", "");
            model.addAttribute("verifyQuestion", "");
            model.addAttribute("verifyAnswer", "");
        }
        return "login/join";   // templates/login/join.html
    }

    // 회원가입 처리
    @PostMapping("/join")
    public String join(@RequestParam("loginId") String loginId,
                       @RequestParam("name") String name,
                       @RequestParam("nickname") String nickname,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam("email") String email,
                       @RequestParam(value = "birth", required = false) String birth,
                       @RequestParam(value = "gender", required = false) String gender,
                       @RequestParam("password") String password,
                       @RequestParam("passwordConfirm") String passwordConfirm,
                       @RequestParam(value = "verifyQuestion", required = false) String verifyQuestion,
                       @RequestParam(value = "verifyAnswer", required = false) String verifyAnswer,
                       Model model) {

        // 1) 비밀번호 확인
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            model.addAttribute("nickname", nickname);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("birth", birth);
            model.addAttribute("gender", gender);
            model.addAttribute("verifyQuestion", verifyQuestion);
            model.addAttribute("verifyAnswer", verifyAnswer);
            return "login/join";
        }

        try {
            // 2) 가입 로직 서비스에 위임
            loginService.join(loginId, name, nickname, phone, email, birth, gender, password, verifyQuestion, verifyAnswer);

        } catch (IllegalStateException e) {
            // 중복/검증 에러
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            model.addAttribute("nickname", nickname);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("birth", birth);
            model.addAttribute("gender", gender);
            model.addAttribute("verifyQuestion", verifyQuestion);
            model.addAttribute("verifyAnswer", verifyAnswer);
            return "login/join";
        }

        // 3) 가입 완료 → 로그인 페이지로
        return "redirect:/login";
    }

    // 2단계 질문 답변 확인
    @PostMapping("/login/second")
    public String secondStepVerify(@RequestParam("answer") String answer,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("tempMemberId") == null) {
            redirectAttributes.addFlashAttribute("error", "다시 로그인해 주세요.");
            return "redirect:/login";
        }

        Long memberId = (Long) session.getAttribute("tempMemberId");
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        if (!member.getVerifyAnswer().equals(answer)) {
            redirectAttributes.addFlashAttribute("error", "본인확인 답변이 일치하지 않습니다.");
            redirectAttributes.addFlashAttribute("showSecondModal", true); // 🔹 모달 다시 열기
            return "redirect:/login";
        }

        // 2차 인증 성공 → 실제 로그인 세션 완성
        session.removeAttribute("tempMemberId");
        session.setAttribute("loginMember", member);
        
        // 🔔 메인 화면에서 중앙 팝업으로 보여줄 메시지
        redirectAttributes.addFlashAttribute(
                "globalMsg",
                "2단계 본인 인증이 완료되었습니다. 안전하게 로그인되었어요."
        );

        // ✅ 성공할 때만 로그인 완료 후 메인으로
        return "redirect:/";
    }
    
    // 인증코드 이메일 발송
    @PostMapping("/email/send")
    public String sendEmailCode(@RequestParam("email") String email,
                                HttpServletRequest request,
                                RedirectAttributes ra) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("tempMemberId") == null) {
            ra.addFlashAttribute("error", "다시 로그인해 주세요.");
            return "redirect:/login";
        }

        Long memberId = (Long) session.getAttribute("tempMemberId");
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        String realEmail = member.getEmail();
        email = (email == null) ? null : email.trim();

        // ❌ 이메일이 다르면 에러 + 모달 유지
        if (email == null || email.isBlank() || !email.equalsIgnoreCase(realEmail)) {
            ra.addFlashAttribute("error", "이메일 정보가 올바르지 않습니다. 다시 시도해 주세요.");
            ra.addFlashAttribute("forceEmailTab", true);
            ra.addFlashAttribute("showSecondModal", true);   // 🔹 모달 다시 열기
            return "redirect:/login";
        }

        // ✅ 여기서부터는 검증 통과
        String code = emailService.generateCode();
        emailService.sendAuthMail(email, code);

        jdbcTemplate.update("""
            INSERT INTO email_auth(email, code, expire_time)
            VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 3 MINUTE))
            ON DUPLICATE KEY UPDATE code=?, expire_time=DATE_ADD(NOW(), INTERVAL 3 MINUTE)
        """, email, code, code);

        // 이메일 발송 완료 → 이메일 탭 + 모달 유지
        ra.addFlashAttribute("emailSent", true);
        ra.addFlashAttribute("forceEmailTab", true);
        ra.addFlashAttribute("showSecondModal", true);       // 🔹 모달 다시 열기

        return "redirect:/login";
    }
    
    @PostMapping("/email/verify")
    public String verifyEmailCode(@RequestParam("email") String email,
                                  @RequestParam("code") String code,
                                  RedirectAttributes ra,
                                  HttpServletRequest request) {

        List<Map<String,Object>> rows = jdbcTemplate.queryForList("""
            SELECT code, expire_time FROM email_auth
            WHERE email = ?
        """, email);

        // ① 인증번호 요청 안 됨
        if (rows.isEmpty()) {
            ra.addFlashAttribute("error", "인증번호를 먼저 요청해 주세요.");
            ra.addFlashAttribute("forceEmailTab", true);
            ra.addFlashAttribute("showSecondModal", true);
            return "redirect:/login";
        }

        Map<String,Object> row = rows.get(0);
        String savedCode = (String) row.get("code");
        java.time.LocalDateTime expireTime = (java.time.LocalDateTime) row.get("expire_time");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // ② 코드 불일치
        if (!savedCode.equals(code)) {
            ra.addFlashAttribute("error",
                    "인증번호가 일치하지 않습니다. 다시 인증번호를 요청해 주세요.");
            ra.addFlashAttribute("forceEmailTab", true);
            ra.addFlashAttribute("emailSent", false);
            ra.addFlashAttribute("showSecondModal", true);
            return "redirect:/login";
        }

        // ③ 시간 만료
        if (expireTime.isBefore(now)) {
            ra.addFlashAttribute("error",
                    "인증번호가 만료되었습니다. 다시 인증번호를 요청해 주세요.");
            ra.addFlashAttribute("forceEmailTab", true);
            ra.addFlashAttribute("emailSent", false);
            ra.addFlashAttribute("showSecondModal", true);
            return "redirect:/login";
        }

        // ④ 성공
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            ra.addFlashAttribute("error", "해당 이메일의 회원 정보를 찾을 수 없습니다.");
            ra.addFlashAttribute("forceEmailTab", true);
            ra.addFlashAttribute("emailSent", true);
            ra.addFlashAttribute("showSecondModal", true);
            return "redirect:/login";
        }

        HttpSession session = request.getSession();
        session.removeAttribute("tempMemberId");
        session.setAttribute("loginMember", member);
        
        // 🔔 메인 화면에 인증 완료 팝업 띄우기
        ra.addFlashAttribute(
                "globalMsg",
                "이메일 2단계 인증이 완료되었습니다. 안전하게 로그인되었어요."
        );

        // ✅ 여기서만 진짜 로그인 완료
        return "redirect:/";
    }
    
 // 🔹 비밀번호 찾기 (임시 비밀번호 발급)
    @PostMapping("/password/reset")
    public String resetPassword(@RequestParam("loginId") String loginId,
                                @RequestParam("email") String email,
                                RedirectAttributes ra) {

        // 양쪽 공백 제거
        String trimmedLoginId = loginId == null ? null : loginId.trim();
        String trimmedEmail   = email == null ? null : email.trim();

        if (trimmedLoginId == null || trimmedLoginId.isBlank()
                || trimmedEmail == null || trimmedEmail.isBlank()) {

            ra.addFlashAttribute("pwError", "아이디와 이메일을 모두 입력해 주세요.");
            ra.addFlashAttribute("showPwModal", true);   // 비밀번호 찾기 모달 다시 열기
            ra.addFlashAttribute("pwLoginId", loginId);
            ra.addFlashAttribute("pwEmail", email);
            return "redirect:/login";
        }

        boolean success = loginService.resetPasswordWithTemp(trimmedLoginId, trimmedEmail);

        if (!success) {
            // 아이디+이메일 일치하는 회원 없음
            ra.addFlashAttribute("pwError", "일치하는 계정을 찾을 수 없습니다.\n아이디와 이메일을 다시 확인해 주세요.");
            ra.addFlashAttribute("showPwModal", true);
            ra.addFlashAttribute("pwLoginId", loginId);
            ra.addFlashAttribute("pwEmail", email);
            return "redirect:/login";
        }

        // 성공
        ra.addFlashAttribute("pwMsg",
                "입력하신 이메일로 임시 비밀번호를 발급했습니다.\n로그인 후 반드시 비밀번호를 변경해 주세요.");
        // 필요하면 모달을 또 열 수도 있지만, 성공 후엔 굳이 안 열어도 됨
        // ra.addFlashAttribute("showPwModal", true);

        return "redirect:/login";
    }
}