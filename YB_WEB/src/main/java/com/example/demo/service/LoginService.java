package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Member;
import com.example.demo.repository.MemberRepository;

@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;

    public LoginService(MemberRepository memberRepository, EmailService emailService) {
        this.memberRepository = memberRepository;
        this.emailService = emailService;
    }

    /**
     * 로그인
     */
    @Transactional   // 로그인도 DB 업데이트가 있으니 트랜잭션 추가
    public Member login(String loginId, String password) {
        return memberRepository.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .map(member -> {
                    // 로그인 성공 시 마지막 로그인 시간 갱신
                    member.setLastLoginAt(LocalDateTime.now());
                    // 트랜잭션 끝날 때 JPA가 UPDATE 쿼리 날려줌
                    return member;
                })
                .orElse(null);
    }

    /**
     * 회원가입
     * - 아이디/이메일 중복 체크
     * - Member 생성 & 저장
     */
    @Transactional
    public Member join(String loginId,
                       String name,
                       String nickname,
                       String phone,
                       String email,
                       String birth,
                       String gender,
                       String password,
                       String verifyQuestion,
                       String verifyAnswer) {

        // 아이디 중복 체크
        if (memberRepository.findByLoginId(loginId).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 체크
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 체크
        if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }

        if (phone != null && !phone.isBlank()) {
            if (memberRepository.findByPhone(phone).isPresent()) {
                throw new IllegalStateException("이미 등록된 휴대폰 번호입니다.");
            }
        }
        
        // 생년월일 파싱 (선택값)
        LocalDate birthDate = null;
        if (birth != null && !birth.isBlank()) {
            birthDate = LocalDate.parse(birth);
        }

        Member member = new Member(
                loginId,
                password,
                name,
                nickname,
                email,
                phone,
                birthDate,
                gender
        );
        
     // 2단계 인증 정보 세팅 (질문을 적은 경우에만 활성화)
        if (verifyQuestion != null && !verifyQuestion.isBlank()
                && verifyAnswer != null && !verifyAnswer.isBlank()) {
            member.setVerifyQuestion(verifyQuestion);
            member.setVerifyAnswer(verifyAnswer);
            member.setTwoFactorEnabled(true);
            member.setTwoFactorType("QUESTION");
        } else {
            member.setTwoFactorEnabled(false);
            member.setTwoFactorType(null);
        }

        return memberRepository.save(member);
    }
    
    /**
     * 비밀번호 찾기: 아이디 + 이메일로 회원 찾고
     * 임시 비밀번호 발급 후 DB 저장 + 메일 발송
     *
     * @return 성공 여부 (true: 발급 완료, false: 일치하는 계정 없음)
     */
    public boolean resetPasswordWithTemp(String loginId, String email) {
        return memberRepository.findByLoginIdAndEmail(loginId, email)
                .map(member -> {
                    // 1) 임시 비밀번호 생성
                    String tempPassword = generateTempPassword();

                    // 2) DB 비밀번호를 임시 비밀번호로 변경
                    //    (지금은 평문 저장 기준. 나중에 PasswordEncoder 도입하면 여기서 encode)
                    member.setPassword(tempPassword);
                    memberRepository.save(member);

                    // 3) 이메일 발송
                    emailService.sendTempPasswordMail(member.getEmail(), tempPassword);

                    return true;
                })
                .orElse(false);   // 회원 없음
    }

    // 🔹 영문+숫자 섞인 10자리 임시 비밀번호 생성
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();

        for (int i = 0; i < 10; i++) {
            int idx = random.nextInt(chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}