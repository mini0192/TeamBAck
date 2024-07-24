package com.example.demo.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final JoinEmailService joinEmailService;

    private SimpleMailMessage createEmailForm(String toEmail, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);

        return message;
    }

    /**
     * 이메일 전송
     */
    public void sendEmail(String email, String title, String text, String authedKey) {
        SimpleMailMessage emailForm = createEmailForm(email, title, text);

        try {
            javaMailSender.send(emailForm);
            joinEmailService.addEmailAuthedKeyMap(email, authedKey);
            log.info("이메일 전송 성공 To.{}, Title. {}, Text. {}", email, title, text);
        } catch (RuntimeException e) {
            log.error("이메일 전송 실패 To.{}, Title. {}, Text. {}", email, title, text);
        }
    }
}