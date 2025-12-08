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
    public String loginForm() {
        // Flash Attribute 로 넘어온 error 는 자동으로 Model 에 들어감
        return "login/login";   // templates/login/login.html
    }

    //  로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam("loginId") String loginId,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {

        Member member = loginService.login(loginId, password);

        if (member == null) {
            // 실패 → 다음 /login 요청에서 한 번만 보이는 에러
            redirectAttributes.addFlashAttribute(
                    "error",
                    "아이디 또는 비밀번호가 일치하지 않아요.\n정확하게 입력해 주세요."
            );
            return "redirect:/login";
        }

        // 로그인 성공
        HttpSession session = request.getSession();
     
        // 2단계 인증 사용 여부 체크
        if (Boolean.TRUE.equals(member.getTwoFactorEnabled())
                && "QUESTION".equals(member.getTwoFactorType())) {

            // 아직 진짜 로그인은 안 하고, 임시로 member id만 저장
            session.setAttribute("tempMemberId", member.getIdx());
            return "redirect:/login/second";    // 2차 인증 페이지로
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
    
 // 2단계 인증 페이지
    @GetMapping("/login/second")
    public String secondStepForm(HttpServletRequest request, Model model,
                                 RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("tempMemberId") == null) {
            redirectAttributes.addFlashAttribute("error", "다시 로그인해 주세요.");
            return "redirect:/login";
        }

        Long memberId = (Long) session.getAttribute("tempMemberId");
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        model.addAttribute("question", member.getVerifyQuestion());

        // ✅ 실제 이메일 + 마스킹 처리
        String realEmail = member.getEmail();
        String maskedEmail = realEmail;

        if (realEmail != null) {
            int atIndex = realEmail.indexOf("@");
            if (atIndex > 1) {
                String first = realEmail.substring(0, 1);
                String hidden = "*".repeat(atIndex - 1);
                String domain = realEmail.substring(atIndex);
                maskedEmail = first + hidden + domain;  // y*****@naver.com 이런식
            }
        }

        model.addAttribute("email", realEmail);        // hidden value
        model.addAttribute("maskedEmail", maskedEmail); // 화면에 보이는 값

        // ⛔ 여기 아래 expire_time / remainSec / expired / emailSent 세팅하는 로직은
        //     타이머 이상하게 남는 원인이라면 과감히 삭제하는 걸 추천

        return "login/second-step-question";
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
            return "redirect:/login/second";
        }

        // 2차 인증 성공 → 실제 로그인 세션 완성
        session.removeAttribute("tempMemberId");
        session.setAttribute("loginMember", member);

        return "redirect:/";
    }
    
 // 인증코드 이메일 발송
    @PostMapping("/email/send")
    public String sendEmailCode(@RequestParam("email") String email,
                                HttpServletRequest request,
                                RedirectAttributes ra) {

        // 🔐 세션 / 회원 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("tempMemberId") == null) {
            ra.addFlashAttribute("error", "다시 로그인해 주세요.");
            return "redirect:/login";
        }

        Long memberId = (Long) session.getAttribute("tempMemberId");
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        String realEmail = member.getEmail();

        // 🔐 이메일 검증: null/공백 또는 회원 이메일과 다르면 전송 차단
        email = (email == null) ? null : email.trim();
        if (email == null || email.isBlank() || !email.equalsIgnoreCase(realEmail)) {
            ra.addFlashAttribute("error", "이메일 정보가 올바르지 않습니다. 다시 시도해 주세요.");
            ra.addFlashAttribute("forceEmailTab", true); // 이메일 탭은 유지
            return "redirect:/login/second";
        }

        // ✅ 여기서부터는 검증 통과한 올바른 이메일일 때만 실행
        String code = emailService.generateCode();
        emailService.sendAuthMail(email, code);

        jdbcTemplate.update("""
            INSERT INTO email_auth(email, code, expire_time)
            VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 3 MINUTE))
            ON DUPLICATE KEY UPDATE code=?, expire_time=DATE_ADD(NOW(), INTERVAL 3 MINUTE)
        """, email, code, code);

        // 이메일 발송 완료 → 이메일 탭 + 타이머 활성화
        ra.addFlashAttribute("emailSent", true);
        ra.addFlashAttribute("forceEmailTab", true);

        return "redirect:/login/second";
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
            ra.addFlashAttribute("forceEmailTab", true);   // 🔹 이메일 탭 유지
            // emailSent 는 false 여도 됨 (안 보냈으니까)
            return "redirect:/login/second";
        }

        Map<String,Object> row = rows.get(0);
        String savedCode = (String) row.get("code");
        java.time.LocalDateTime expireTime = (java.time.LocalDateTime) row.get("expire_time");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // ② 코드 불일치
        if (!savedCode.equals(code)) {
            ra.addFlashAttribute("error",
                    "인증번호가 일치하지 않습니다. 다시 인증번호를 요청해 주세요.");
            ra.addFlashAttribute("forceEmailTab", true);  // 이메일 탭은 유지
            ra.addFlashAttribute("emailSent", false);     // 🔥 재입력 막기 (버튼 비활성 + 타이머 숨김)

            // 옵션) 아예 DB 값도 삭제하고 싶으면:
            // jdbcTemplate.update("DELETE FROM email_auth WHERE email = ?", email);

            return "redirect:/login/second";
        }

        // ③ 시간 만료
        if (expireTime.isBefore(now)) {
            ra.addFlashAttribute("error",
                    "인증번호가 만료되었습니다. 다시 인증번호를 요청해 주세요.");
            ra.addFlashAttribute("forceEmailTab", true);
            ra.addFlashAttribute("emailSent", false);     // 🔥 재입력 막기

            // 옵션) 여기서도 기존 코드 삭제하고 싶으면:
            // jdbcTemplate.update("DELETE FROM email_auth WHERE email = ?", email);

            return "redirect:/login/second";
        }

        // ④ 성공
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            ra.addFlashAttribute("error", "해당 이메일의 회원 정보를 찾을 수 없습니다.");
            ra.addFlashAttribute("forceEmailTab", true);
            ra.addFlashAttribute("emailSent", true);
            return "redirect:/login/second";
        }

        HttpSession session = request.getSession();
        session.removeAttribute("tempMemberId");
        session.setAttribute("loginMember", member);

        return "redirect:/";
    }
}