package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDto<?>> update(
            @RequestParam String username,
            @RequestParam String email) {
        return ResponseEntity.status(200).body(userService.uppdateProfile(username, email));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ResponseDto<?>> resetPassword(
            @RequestParam String old_password,
            @RequestParam String new_password) {
        return ResponseEntity.ok(userService.resetPassword(old_password, new_password));
    }

}
