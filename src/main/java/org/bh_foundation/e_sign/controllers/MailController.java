package org.bh_foundation.e_sign.controllers;

import java.io.IOException;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.mail.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    private MailService mailService;

    public MailController(
            MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/send")
    public ResponseEntity<ResponseDto<?>> sendVerification() throws MessagingException, IOException {
        mailService.sendVerificationEmail("iqnaraidan12@gmail.com", "lzmcvzcv");
        return ResponseEntity.ok(new ResponseDto<>(200, "email sent", null));
    }

}
