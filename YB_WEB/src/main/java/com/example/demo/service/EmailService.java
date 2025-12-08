package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 6자리 인증코드 생성
    public String generateCode() {
        return String.valueOf((int)(Math.random()*900000)+100000);
    }

 // 📩 인증메일 전송 (AWS 스타일 느낌으로 꾸미기)
    public void sendAuthMail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom("YOUR_EMAIL@gmail.com", "YBWEB 관리자"); // 발신 이메일/이름

            helper.setSubject("[YB WEB] 이메일 인증 코드 안내");

            String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>YB WEB 이메일 인증</title>
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            background-color: #f3f4f6;
                            font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue",
                                         "맑은 고딕", "Malgun Gothic", system-ui, sans-serif;
                        }
                        .wrapper {
                            max-width: 560px;
                            margin: 40px auto;
                            background-color: #ffffff;
                            border-radius: 8px;
                            box-shadow: 0 10px 25px rgba(15, 23, 42, 0.08);
                            overflow: hidden;
                        }
                        .header {
                            background-color: #111827;
                            padding: 18px 24px;
                            text-align: center;
                        }
                        .header-logo {
                            color: #ffffff;
                            font-weight: 700;
                            font-size: 20px;
                            letter-spacing: 0.08em;
                        }
                        .content {
                            padding: 28px 30px 32px 30px;
                            color: #111827;
                            font-size: 14px;
                            line-height: 1.6;
                        }
                        .title {
                            font-size: 20px;
                            font-weight: 700;
                            margin-bottom: 10px;
                        }
                        .desc {
                            margin: 4px 0;
                        }
                        .code-box {
                            margin: 26px 0 18px 0;
                            text-align: center;
                            border-top: 1px solid #e5e7eb;
                            border-bottom: 1px solid #e5e7eb;
                            padding: 18px 0 20px 0;
                        }
                        .code-label {
                            font-size: 13px;
                            color: #6b7280;
                            margin-bottom: 6px;
                        }
                        .code-value {
                            font-size: 32px;
                            font-weight: 700;
                            letter-spacing: 0.25em;
                            color: #111827;
                        }
                        .expire-text {
                            margin-top: 8px;
                            font-size: 12px;
                            color: #6b7280;
                        }
                        .footer {
                            padding: 16px 30px 22px 30px;
                            border-top: 1px solid #e5e7eb;
                            font-size: 11px;
                            color: #9ca3af;
                            line-height: 1.5;
                        }
                    </style>
                </head>
                <body>
                    <div class="wrapper">
                        <div class="header">
                            <div class="header-logo">YB WEB</div>
                        </div>
                        <div class="content">
                            <div class="title">YB WEB 이메일 주소 확인</div>
                            <p class="desc">안녕하세요.</p>
                            <p class="desc">
                                YB WEB 계정 보호를 위해 이메일 인증을 진행하고 있습니다.
                                아래 인증 코드를 인증 화면에 입력해 주세요.
                            </p>

                            <div class="code-box">
                                <div class="code-label">인증 코드</div>
                                <div class="code-value">%s</div>
                                <div class="expire-text">
                                    이 코드는 발송 후 3분 동안만 유효합니다.
                                </div>
                            </div>

                            <p class="desc">
                                만약 회원가입이나 로그인 인증을 시도한 적이 없다면
                                이 메일을 무시하셔도 됩니다.
                            </p>
                        </div>
                        <div class="footer">
                            이 메일은 발신 전용입니다. 비밀번호, 카드 번호, 계좌 번호와 같은
                            민감한 정보는 절대 메일로 요청하지 않습니다.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(code);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}