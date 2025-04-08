package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.services.data.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/ll")
    public ResponseEntity<?> aa() {
        return ResponseEntity.ok("GHJKNHJG");
    }

}
