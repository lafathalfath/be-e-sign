package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.services.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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

    @SuppressWarnings("null")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User request, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(authService.register(request));
    }

}
