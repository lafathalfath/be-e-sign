package org.bh_foundation.e_sign.services.data;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final HttpServletRequest servletRequest;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository, JwtService jwtService, HttpServletRequest servletRequest) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.servletRequest = servletRequest;
        this.passwordEncoder = null;
    }

    public ResponseDto<?> uppdateProfile(String username, String email) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (username == null && email == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
        if (username != null) user.setUsername(username);
        if (email != null) user.setEmail(email);
        userRepository.save(user);
        return new ResponseDto<>(200, "updated", null);
    }

    public ResponseDto<?> resetPassword(String oldPassword, String newPassword) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "wrong password");
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new ResponseDto<>(200, "updated", null);
    }

}
