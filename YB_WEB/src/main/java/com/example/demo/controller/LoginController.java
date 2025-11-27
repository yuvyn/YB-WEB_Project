package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.domain.Member;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
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
        // 처음 들어올 때 빈 값 세팅 (에러로 돌아올 때 덮어씌워짐)
        if (!model.containsAttribute("loginId")) {
            model.addAttribute("loginId", "");
            model.addAttribute("name", "");
            model.addAttribute("email", "");
        }
        return "login/join";   // templates/login/join.html
    }

 // 회원가입 처리
    @PostMapping("/join")
    public String join(@RequestParam("loginId") String loginId,
                       @RequestParam("name") String name,
                       @RequestParam("email") String email,
                       @RequestParam("password") String password,
                       @RequestParam("passwordConfirm") String passwordConfirm,
                       Model model) {

        // 1) 비밀번호 확인 (이건 화면쪽 검증이니까 컨트롤러에서 해도 괜찮음)
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "login/join";
        }

        try {
            // 2) 나머지 가입 로직은 서비스에 위임
            loginService.join(loginId, name, email, password);

        } catch (IllegalStateException e) {
            // 서비스에서 던진 중복 에러 처리
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "login/join";
        }

        // 3) 가입 완료 → 로그인 페이지로
        return "redirect:/login";
    }
}