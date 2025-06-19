package org.bh_foundation.e_sign.services.mail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    @Value("${spring.mail.username}")
    private String USER_EMAIL;

    @Value("${client.url}")
    private String CLIENT_URL;

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String verificationLink) throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setFrom(USER_EMAIL);
        helper.setSubject("Verify Your Account");

        String htmlTemplate = new String(Files.readAllBytes(
                Paths.get(new ClassPathResource("templates/mail/verification.html").getURI())), StandardCharsets.UTF_8);
        String htmlContent = htmlTemplate.replace("{{VERIFICATION_LINK}}", verificationLink);
        helper.setText(htmlContent, true);

        ClassPathResource logoImage = new ClassPathResource("static/images/company_logo.png");
        helper.addInline("{{COMPANY_LOGO}}", logoImage);

        mailSender.send(message);
    }

    public void sendOtpEmail(String toEmail, String otp) throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setFrom(USER_EMAIL);
        helper.setSubject("Verify Your Account");

        String htmlTemplate = new String(Files.readAllBytes(
                Paths.get(new ClassPathResource("templates/mail/otp.html").getURI())), StandardCharsets.UTF_8);
        String htmlContent = htmlTemplate.replace("{{VERIFICATION_CODE}}", otp);
        helper.setText(htmlContent, true);

        ClassPathResource logoImage = new ClassPathResource("static/images/company_logo.png");
        helper.addInline("{{COMPANY_LOGO}}", logoImage);

        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String toEmail, String token) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setFrom(USER_EMAIL);
        helper.setSubject("Reset Your Password");
        String htmlTemplate = new String(Files.readAllBytes(Paths.get(new ClassPathResource("templates/mail/resetPassword.html").getURI())), StandardCharsets.UTF_8); // pada baris ini error muncul
        String htmlContent = htmlTemplate.replace("{{RESET_PASSWORD_LINK}}", CLIENT_URL + "/reset-password");
        htmlContent = htmlContent.replace("{{TOKEN}}", token);
        helper.setText(htmlContent, true);
        ClassPathResource logoImage = new ClassPathResource("static/images/company_logo.png");
        helper.addInline("{{COMPANY_LOGO}}", logoImage);
        mailSender.send(message);
    }

}
