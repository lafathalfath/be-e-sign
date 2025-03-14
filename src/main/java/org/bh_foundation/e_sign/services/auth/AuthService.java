package org.bh_foundation.e_sign.services.auth;

import java.io.IOException;
import java.time.LocalDateTime;

import org.bh_foundation.e_sign.dto.AuthenticationResponseDto;
import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.PasswordResetToken;
import org.bh_foundation.e_sign.models.Role;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.PasswordResetTokenRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.mail.MailService;
import org.bh_foundation.e_sign.utils.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {

    @Value("${server.base-url}")
    private String BASE_URL;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;
    private final HttpServletRequest servletRequest;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            HttpServletRequest servletRequest,
            PasswordEncoder passwordEncoder,
            MailService mailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.servletRequest = servletRequest;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseDto<?> verification(String token) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authentication"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!token.equals(user.getVerificationToken())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        user.setVerifiedAt(LocalDateTime.now());
        user.setVerificationToken(null);
        userRepository.save(user);
        return new ResponseDto<>(200, "User Verified", null);
    }

    public ResponseDto<?> resendVerificationEmail() throws MessagingException, IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authentication"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String token = RandomStringUtils.generate(256);
        user.setVerificationToken(token);
        userRepository.save(user);
        mailService.sendVerificationEmail(user.getEmail(), BASE_URL + "/api/auth/" + token + "/verify");
        return new ResponseDto<>(200, "Verification email sent", null);
    }

    public AuthenticationResponseDto register(User request) throws MessagingException, IOException {
        User user = new User();
        String verificationToken = RandomStringUtils.generate(256);

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);
        user.setVerificationToken(verificationToken);
        user = userRepository.save(user);

        mailService.sendVerificationEmail(user.getEmail(), BASE_URL + "/api/auth/" + verificationToken + "/verify");
        
        String token = jwtService.generateToken(user);
        return new AuthenticationResponseDto(token);
    }

    public AuthenticationResponseDto authenticate(User request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow();
        String token = jwtService.generateToken(user);
        return new AuthenticationResponseDto(token);
    }

    public AuthenticationResponseDto refreshToken() {
        String header = servletRequest.getHeader("Authorization");
        if (header == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No token provided");
        String token = header;
        if (token.startsWith("Bearer "))
            token = token.substring(7);
        User user = userRepository.findById(jwtService.extractUserId(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));
        return new AuthenticationResponseDto(jwtService.refreshToken(token, user));
    }

    public ResponseDto<?> sendForgotPasswordEmail(String email, String pageLink) throws MessagingException, IOException {
        userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        String token = RandomStringUtils.generate(16);
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setEmail(email);
        passwordResetToken.setToken(token);
        passwordResetTokenRepository.save(passwordResetToken);
        mailService.sendResetPasswordEmail(email, pageLink, token);
        return new ResponseDto<>(200, "Reset Password Email sent", null);
    }

    public ResponseDto<?> resetForgottenPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        User user = userRepository.findByEmail(passwordResetToken.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        user.setPassword(newPassword);
        userRepository.save(user);
        return new ResponseDto<>(200, "password reset successfully", null);
    }

}
