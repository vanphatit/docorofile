package com.group.docorofile.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Chú ý: phải khởi tạo map để tránh NullPointerException
    private Map<String, String> emailTokenMap = new ConcurrentHashMap<>();

    public void sendVerificationEmail(String to, String verificationCode) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Xác thực tài khoản - DoCoroFile");
        helper.setText("<h3>Xin chào!</h3>" +
                "<p>Mã xác thực của bạn: <b>" + verificationCode + "</b></p>" +
                "<p>Vui lòng nhập mã này để kích hoạt tài khoản.</p>", true);
        mailSender.send(mimeMessage);

        if(emailTokenMap.containsKey(to)) {
            emailTokenMap.replace(to, verificationCode);
        } else {
            emailTokenMap.put(to, verificationCode);
        }

    }

    public String getVerificationCode(String email) {
        return emailTokenMap.get(email);
    }

    // Nếu muốn xóa code khỏi map sau khi xác thực
    public void removeVerificationCode(String email) {
        emailTokenMap.remove(email);
    }
}
