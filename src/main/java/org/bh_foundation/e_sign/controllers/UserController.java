package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @SuppressWarnings("null")
    @PutMapping("/update")
    public ResponseEntity<ResponseDto<?>> update(
            @Valid @RequestParam String username,
            @Valid @RequestParam String email, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.status(200).body(userService.uppdateProfile(username, email));
    }

    @SuppressWarnings("null")
    @PutMapping("/reset-password")
    public ResponseEntity<ResponseDto<?>> resetPassword(
            @Valid @RequestParam String old_password,
            @Valid @RequestParam String new_password, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(userService.resetPassword(old_password, new_password));
    }

}
