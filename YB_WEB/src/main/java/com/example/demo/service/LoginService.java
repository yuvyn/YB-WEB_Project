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

    public LoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
                       String password) {

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

        return memberRepository.save(member);
    }
}