package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.models.Signature;
import org.bh_foundation.e_sign.services.data.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signature")
public class SignatureController {
    
    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(signatureService.get());
    }

    @PostMapping("/store")
    public ResponseEntity<?> store(@RequestBody Signature request) {
        return ResponseEntity.ok(signatureService.store(request));
    }

    @DeleteMapping("/destroy")
    public ResponseEntity<?> destroy() {
        return ResponseEntity.ok(signatureService.destroy());
    }
    
}
