package org.bh_foundation.e_sign.services.auth;

import java.util.List;

import org.bh_foundation.e_sign.dto.AuthenticationResponseDto;
import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.Role;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final HttpServletRequest servletRequest;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        HttpServletRequest servletRequest,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.servletRequest = servletRequest;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseDto<?> getUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseDto<List<User>>(
            200,
            "OK",
            users
        );
    }

    public AuthenticationResponseDto register(User request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);
        user = userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthenticationResponseDto(token);
    }

    public AuthenticationResponseDto authenticate(User request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsernameOrEmail(request.getUsername())
            .orElseThrow();
        String token = jwtService.generateToken(user);
        return new AuthenticationResponseDto(token);
    }

    public AuthenticationResponseDto refreshToken() {
        String header = servletRequest.getHeader("Authorization");
        if (header == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No token provided");
        String token = header;
        if (token.startsWith("Bearer ")) token = token.substring(7);
        User user = userRepository.findById(jwtService.extractUserId(token))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));
        return new AuthenticationResponseDto(jwtService.refreshToken(token, user));
    }

}
