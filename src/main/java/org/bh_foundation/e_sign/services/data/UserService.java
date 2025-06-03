package org.bh_foundation.e_sign.services.data;

import java.util.List;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.dto.UserDto;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserService {

    @Value("${client.url}")
    private String CLIENT_URL;
    
    @Value("${server.base-url}")
    private String BASE_URL;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final HttpServletRequest servletRequest;
    private final HttpServletResponse servletResponse;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository, 
            JwtService jwtService, 
            HttpServletRequest servletRequest, 
            HttpServletResponse servletResponse,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseDto<?> uppdateProfile(String username, String email) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (user.getVerifiedAt() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        if (username == null && email == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
        if (username != null)
            user.setUsername(username);
        if (email != null)
            user.setEmail(email);
        userRepository.save(user);
        return new ResponseDto<>(200, "updated", null);
    }

    public ResponseDto<?> resetPassword(String oldPassword, String newPassword) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));
        if (user.getVerifiedAt() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "wrong password");
        user.setPassword(passwordEncoder.encode(newPassword));
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
        return new ResponseDto<>(200, "updated", null);
    }
    
    public ResponseDto<?> getAllUsers() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));
        if (user.getVerifiedAt() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        List<UserDto> users = userRepository.findUsernameEmailId();
        return new ResponseDto<>(200, "OK", users);
    }

}
