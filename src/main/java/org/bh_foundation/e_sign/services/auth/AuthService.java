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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    @Value("${server.base-url}")
    private String BASE_URL;
    @Value("${client.url}")
    private String CLIENT_URL;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;
    private final HttpServletRequest servletRequest;
    private final HttpServletResponse servletResponse;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public AuthService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    public boolean verification(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user == null)
            return false;
        user.setVerifiedAt(LocalDateTime.now());
        user.setVerificationToken(null);
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        Cookie cookie = new Cookie("bhf-e-sign-access-token", jwtToken);
        cookie.setHttpOnly(false);
        cookie.setPath("/");

        if ((!CLIENT_URL.startsWith("http://") && !CLIENT_URL.startsWith("https://"))
                || (!BASE_URL.startsWith("http://") && !BASE_URL.startsWith("https://")))
            throw new ResponseStatusException(500, "Internal Server Error", null);
        if (CLIENT_URL.startsWith("https://") && BASE_URL.startsWith("https://"))
            cookie.setSecure(true);
        else
            cookie.setSecure(false);

        cookie.setMaxAge(1 * 60 * 60);
        servletResponse.addCookie(cookie);
        return true;
    }

    public ResponseDto<?> verifyWithOtp(String otp) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(otp, user.getVerificationToken()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        user.setVerifiedAt(LocalDateTime.now());
        user.setVerificationToken(null);
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        Cookie cookie = new Cookie("bhf-e-sign-access-token", jwtToken);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        if ((!CLIENT_URL.startsWith("http://") && !CLIENT_URL.startsWith("https://"))
                || (!BASE_URL.startsWith("http://") && !BASE_URL.startsWith("https://")))
            throw new ResponseStatusException(500, "Internal Server Error", null);
        if (CLIENT_URL.startsWith("https://") && BASE_URL.startsWith("https://"))
            cookie.setSecure(true);
        else
            cookie.setSecure(false);
        cookie.setMaxAge(1 * 60 * 60);
        servletResponse.addCookie(cookie);
        return new ResponseDto<>(200, "Verification Successful", null);
    }

    public ResponseDto<?> resendVerificationEmail() throws MessagingException, IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String token = RandomStringUtils.generate(256);
        user.setVerificationToken(token);
        userRepository.save(user);
        mailService.sendVerificationEmail(user.getEmail(), BASE_URL + "/api/auth/verification/" + token + "/verify"); // ini
        // mailService.sendVerificationEmail(user.getEmail(),
        // "http://localhost:5173/verifikasi/98h76f58h7g6f5d");
        return new ResponseDto<>(200, "Verification email sent", null);
    }
    
    public ResponseDto<?> resendOtpEmail() throws MessagingException, IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        // String token = RandomStringUtils.generate(256);
        String otp = RandomStringUtils.generateOtp();
        // user.setVerificationToken(token);
        user.setVerificationToken(passwordEncoder.encode(otp));
        userRepository.save(user);
        mailService.sendOtpEmail(user.getEmail(), otp);
        return new ResponseDto<>(200, "Verification email sent", null);
    }

    public AuthenticationResponseDto register(User request) throws MessagingException, IOException {
        User user = new User();
        // String verificationToken = RandomStringUtils.generate(256);
        String otp = RandomStringUtils.generateOtp();

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);
        // user.setVerificationToken(verificationToken);
        user.setVerificationToken(passwordEncoder.encode(otp));
        user = userRepository.save(user);

        // mailService.sendVerificationEmail(user.getEmail(),
        //         BASE_URL + "/api/auth/verification/" + verificationToken + "/verify");
        mailService.sendOtpEmail(user.getEmail(), otp);

        String token = jwtService.generateToken(user);
        return new AuthenticationResponseDto(token);
    }

    public AuthenticationResponseDto authenticate(User request) {
        if (!sessionService.checkLoginAttempt(servletRequest))
            return new AuthenticationResponseDto(null);
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow();
        String token = jwtService.generateToken(user);
        Cookie cookie = new Cookie("bhf-e-sign-access-token", token);
        cookie.setHttpOnly(false);
        cookie.setPath("/");

        if ((!CLIENT_URL.startsWith("http://") && !CLIENT_URL.startsWith("https://"))
                || (!BASE_URL.startsWith("http://") && !BASE_URL.startsWith("https://")))
            throw new ResponseStatusException(500, "Internal Server Error", null);
        if (CLIENT_URL.startsWith("https://") && BASE_URL.startsWith("https://"))
            cookie.setSecure(true);
        else
            cookie.setSecure(false);

        cookie.setMaxAge(1 * 60 * 60);
        servletResponse.addCookie(cookie);
        sessionService.clearLoginAttempt(servletRequest);
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

    public ResponseDto<?> sendForgotPasswordEmail(String email)
            throws MessagingException, IOException {
        userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        String token = RandomStringUtils.generate(16);
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByEmail(email);
        if (passwordResetToken == null) {
            passwordResetToken = new PasswordResetToken();
            passwordResetToken.setEmail(email);
        }
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        passwordResetTokenRepository.save(passwordResetToken);
        mailService.sendResetPasswordEmail(email, token);
        return new ResponseDto<>(200, "Reset Password Email sent", null);
    }

    public ResponseDto<?> resetForgottenPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null || LocalDateTime.now().isAfter(passwordResetToken.getExpiredAt()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        User user = userRepository.findByEmail(passwordResetToken.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new ResponseDto<>(200, "password reset successfully", null);
    }

    public boolean validateResetPasswordToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null)
            return false;
        return LocalDateTime.now().isBefore(passwordResetToken.getExpiredAt());
    }

    public boolean validateExpirationToken() {
        String token = servletRequest.getHeader("Authorization");
        if (token == null)
            return false;
        if (token.startsWith("Bearer "))
            token = token.substring(7);
        return !jwtService.isTokenExpired(token);
    }

}
