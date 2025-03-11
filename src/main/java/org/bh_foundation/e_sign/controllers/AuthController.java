package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.dto.AuthenticationResponseDto;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.services.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(authService.getUsers());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@RequestBody User request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@RequestBody User request) {
        return ResponseEntity.ok(authService.register(request));
    }

}
