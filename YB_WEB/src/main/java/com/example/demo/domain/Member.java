package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;

@Entity
@Table(name = "member")
public class Member {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "id", nullable = false, unique = true)
    private String loginId;

    @Column(name = "pw", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "birth")
    private LocalDate birth;

    // DB: ENUM('M','F') 이지만, 코드에선 String 으로 받음 ("M" / "F")
    @Column(name = "gender")
    private String gender;

    @Column(name = "status")
    private String status = "ACTIVE";

    @Column(name = "role")
    private String role = "USER";

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "verify_question")
    private String verifyQuestion;

    @Column(name = "verify_answer")
    private String verifyAnswer;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;   // 기본 false

    @Column(name = "two_factor_type")
    private String twoFactorType;

    protected Member() {}

    // 기존에 쓰던 생성자
    public Member(String loginId, String password, String name, String email) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.status = "ACTIVE";
        this.role = "USER";
    }

    // 신규 필드까지 포함한 생성자
    public Member(String loginId,
                  String password,
                  String name,
                  String nickname,
                  String email,
                  String phone,
                  LocalDate birth,
                  String gender) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.birth = birth;
        this.gender = gender;
        this.status = "ACTIVE";
        this.role = "USER";
    }

    // === Getter ===
    public Long getIdx() { return idx; }
    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getNickname() { return nickname; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public LocalDate getBirth() { return birth; }
    public String getGender() { return gender; }
    public String getStatus() { return status; }
    public String getRole() { return role; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getVerifyQuestion() { return verifyQuestion; }
    public String getVerifyAnswer() { return verifyAnswer; }
    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public String getTwoFactorType() { return twoFactorType; }

    // === Setter ===
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setBirth(LocalDate birth) { this.birth = birth; }
    public void setGender(String gender) { this.gender = gender; }
    public void setStatus(String status) { this.status = status; }
    public void setRole(String role) { this.role = role; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setVerifyQuestion(String verifyQuestion) { this.verifyQuestion = verifyQuestion; }
    public void setVerifyAnswer(String verifyAnswer) { this.verifyAnswer = verifyAnswer; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    public void setTwoFactorType(String twoFactorType) { this.twoFactorType = twoFactorType; }
}