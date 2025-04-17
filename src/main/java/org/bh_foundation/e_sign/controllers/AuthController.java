package org.bh_foundation.e_sign.controllers;

import java.io.IOException;
import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.services.auth.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${client.url}")
    private String CLIENT_URL;

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @SuppressWarnings("null")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody User request, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh-auth")
    public ResponseEntity<?> refreshAuth() {
        return ResponseEntity.ok(authService.refreshToken());
    }

    @PostMapping("/validate-expiration")
    public ResponseEntity<Boolean> validate() {
        return ResponseEntity.ok(authService.validateExpirationToken());
    }

    @SuppressWarnings("null")
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody User request,
            BindingResult result) throws MessagingException, IOException {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/verification/{token}/verify")
    public RedirectView verification(@PathVariable String token) {
        if (authService.verification(token))
            return new RedirectView(CLIENT_URL + "/dashboard");
        return new RedirectView(CLIENT_URL);
    }

    @GetMapping("/verification/resend")
    public ResponseEntity<?> resendVerification() throws MessagingException, IOException {
        return ResponseEntity.ok(authService.resendVerificationEmail());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseDto<?>> forgotPassword(
            @Email @RequestParam String email) throws MessagingException, IOException {
        return ResponseEntity.ok(authService.sendForgotPasswordEmail(email));
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<ResponseDto<?>> resetPassword(
            @PathVariable String token,
            @RequestParam String password) {
        return ResponseEntity.ok(authService.resetForgottenPassword(token, password));
    }

    @PostMapping("/validate-reset-password-token")
    public ResponseEntity<?> validateResetPasswordToken(@RequestParam String token) {
        return ResponseEntity.ok(authService.validateResetPasswordToken(token));
    }

    @PostMapping("/logout")
    public String logout() {
        return "logout";
    }

}
