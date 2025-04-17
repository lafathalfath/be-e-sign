package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.services.data.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String old_password,
            @RequestParam String new_password) {
        return ResponseEntity.ok(userService.resetPassword(old_password, new_password));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody User request) {
        return ResponseEntity.ok(userService.uppdateProfile(request.getUsername(), request.getEmail()));
    }

}
