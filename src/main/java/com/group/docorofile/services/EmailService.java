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
        String url = "https://docorofile.phatit.id.vn/auth/verify-email?code=" + verificationCode + "&email=" + to;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Xác thực tài khoản - DoCoroFile");
        helper.setText("<h3>Xin chào!</h3>" +
                "<p>Vui lòng nhấn vào liên kết dưới đây để xác thực tài khoản của bạn:</p>" +
                "<a href=\"" + url + "\">Xác thực tài khoản</a>", true);
        mailSender.send(mimeMessage);

        if(emailTokenMap.containsKey(to)) {
            emailTokenMap.replace(to, verificationCode);
        } else {
            emailTokenMap.put(to, verificationCode);
        }

    }

    public void sendResetPasswordEmail(String to, String code, String password) throws MessagingException {
        String url = "https://docorofile.phatit.id.vn/auth/reset-password?code=" + code + "&email=" + to + "&newPassword=" + password;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Đặt lại mật khẩu - DoCoroFile");
        helper.setText("<h3>Xin chào!</h3>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.</p>" +
                "<p>Mật khẩu mới của bạn là: <strong>" + password + "</strong></p>" +
                "<p>Vui lòng nhấn vào liên kết dưới đây để hoàn tất đặt lại mật khẩu của bạn:</p>" +
                "<a href=\"" + url + "\">Đặt lại mật khẩu</a>", true);
        mailSender.send(mimeMessage);

        if(emailTokenMap.containsKey(to)) {
            emailTokenMap.replace(to, code);
        } else {
            emailTokenMap.put(to, code);
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
