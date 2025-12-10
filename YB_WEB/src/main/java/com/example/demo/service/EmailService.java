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
            helper.setFrom("YOUR_EMAIL@gmail.com", "YBWEB 관리자");
            helper.setSubject("[YB WEB] 이메일 인증 코드 안내");

            String html = """
                <!-- 네이버/모바일 호환용: table + inline style -->
                <table width="100%%" bgcolor="#f3f4f6" 
                       style="margin:0;padding:20px 0;font-family:-apple-system,BlinkMacSystemFont,'Helvetica Neue','맑은 고딕',system-ui,sans-serif;">
                  <tr>
                    <td align="center">

                      <!-- 메인 카드 -->
                      <table width="560" cellpadding="0" cellspacing="0" bgcolor="#ffffff"
                             style="max-width:560px;border-radius:8px;overflow:hidden;border:1px solid #e5e7eb;">
                        <!-- 상단 헤더 -->
                        <tr>
                          <td align="center"
                              style="background-color:#111827;padding:16px 24px;">
                            <span style="color:#ffffff;font-weight:700;font-size:20px;letter-spacing:0.08em;">
                              YB WEB
                            </span>
                          </td>
                        </tr>

                        <!-- 본문 -->
                        <tr>
                          <td style="padding:28px 30px 32px 30px;color:#111827;font-size:14px;line-height:1.6;">
                            <div style="font-size:20px;font-weight:700;margin-bottom:12px;">
                              YB WEB 이메일 주소 확인
                            </div>

                            <p style="margin:4px 0;">안녕하세요.</p>
                            <p style="margin:4px 0;">
                              YB WEB 계정 보호를 위해 이메일 인증을 진행하고 있습니다.
                              아래 인증 코드를 인증 화면에 입력해 주세요.
                            </p>

                            <!-- 코드 박스 -->
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="margin:26px 0 18px 0;border-top:1px solid #e5e7eb;border-bottom:1px solid #e5e7eb;">
                              <tr>
                                <td align="center" style="padding:18px 0 20px 0;">
                                  <div style="font-size:13px;color:#6b7280;margin-bottom:6px;">
                                    인증 코드
                                  </div>
                                  <div style="font-size:32px;font-weight:700;letter-spacing:8px;color:#111827;">
                                    %s
                                  </div>
                                  <div style="margin-top:8px;font-size:12px;color:#6b7280;">
                                    이 코드는 발송 후 3분 동안만 유효합니다.
                                  </div>
                                </td>
                              </tr>
                            </table>

                            <p style="margin:4px 0;">
                              만약 회원가입이나 로그인 인증을 시도한 적이 없다면
                              이 메일을 무시하셔도 됩니다.
                            </p>
                          </td>
                        </tr>

                        <!-- 푸터 -->
                        <tr>
                          <td style="padding:14px 30px 18px 30px;
                                     border-top:1px solid #e5e7eb;
                                     font-size:11px;color:#9ca3af;line-height:1.5;">
                            이 메일은 발신 전용입니다. 비밀번호, 카드 번호, 계좌 번호와 같은
                            민감한 정보는 절대 메일로 요청하지 않습니다.
                          </td>
                        </tr>

                      </table>

                    </td>
                  </tr>
                </table>
                """.formatted(code);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
 // 🔹 임시 비밀번호 메일 전송 (비밀번호 찾기용)
    public void sendTempPasswordMail(String to, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom("YOUR_EMAIL@gmail.com", "YBWEB 관리자");
            helper.setSubject("[YB WEB] 임시 비밀번호 안내");

            String html = """
                <table width="100%%" bgcolor="#f3f4f6" 
                       style="margin:0;padding:20px 0;font-family:-apple-system,BlinkMacSystemFont,'Helvetica Neue','맑은 고딕',system-ui,sans-serif;">
                  <tr>
                    <td align="center">

                      <table width="560" cellpadding="0" cellspacing="0" bgcolor="#ffffff"
                             style="max-width:560px;border-radius:8px;overflow:hidden;border:1px solid #e5e7eb;">
                        
                        <!-- 헤더 -->
                        <tr>
                          <td align="center"
                              style="background-color:#111827;padding:16px 24px;">
                            <span style="color:#ffffff;font-weight:700;font-size:20px;letter-spacing:0.08em;">
                              YB WEB
                            </span>
                          </td>
                        </tr>

                        <!-- 본문 -->
                        <tr>
                          <td style="padding:28px 30px 32px 30px;color:#111827;font-size:14px;line-height:1.6;">
                            <div style="font-size:20px;font-weight:700;margin-bottom:12px;">
                              임시 비밀번호가 발급되었습니다.
                            </div>

                            <p style="margin:4px 0;">안녕하세요.</p>
                            <p style="margin:4px 0;">
                              비밀번호 찾기 요청에 따라 아래의 임시 비밀번호가 발급되었습니다.
                              로그인 후 반드시 <b>마이페이지 &gt; 비밀번호 변경</b> 메뉴에서
                              비밀번호를 다시 설정해 주세요.
                            </p>

                            <!-- 임시 비밀번호 박스 -->
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="margin:26px 0 18px 0;border-top:1px solid #e5e7eb;border-bottom:1px solid #e5e7eb;">
                              <tr>
                                <td align="center" style="padding:18px 0 20px 0;">
                                  <div style="font-size:13px;color:#6b7280;margin-bottom:6px;">
                                    임시 비밀번호
                                  </div>
                                  <div style="font-size:26px;font-weight:700;letter-spacing:4px;color:#111827;">
                                    %s
                                  </div>
                                </td>
                              </tr>
                            </table>

                            <p style="margin:4px 0;">
                              본인이 요청한 게 아니라면, 보안을 위해 즉시 비밀번호 변경 후
                              고객센터로 문의해 주세요.
                            </p>
                          </td>
                        </tr>

                        <!-- 푸터 -->
                        <tr>
                          <td style="padding:14px 30px 18px 30px;
                                     border-top:1px solid #e5e7eb;
                                     font-size:11px;color:#9ca3af;line-height:1.5;">
                            이 메일은 발신 전용입니다. 비밀번호, 카드 번호, 계좌 번호와 같은
                            민감한 정보는 절대 메일로 요청하지 않습니다.
                          </td>
                        </tr>

                      </table>

                    </td>
                  </tr>
                </table>
                """.formatted(tempPassword);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}