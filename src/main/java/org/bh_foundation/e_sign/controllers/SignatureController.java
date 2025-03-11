package org.bh_foundation.e_sign.controllers;

import java.io.IOException;

import org.bh_foundation.e_sign.services.data.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/signature")
public class SignatureController {

    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @GetMapping("/get")
    public ResponseEntity<?> get(@RequestParam String passphrase) {
        return ResponseEntity.ok(signatureService.get(passphrase));
    }

    @PostMapping("/store")
    public ResponseEntity<?> store(
            @RequestParam String passphrase,
            @RequestParam MultipartFile sign) throws IOException {
        return ResponseEntity.status(201).body(signatureService.store(passphrase, sign));
    }

    @PutMapping("/extends")
    public ResponseEntity<?> extend(@RequestParam String passphrase) {
        return ResponseEntity.status(200).body(signatureService.extend(passphrase));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam String passphrase) {
        return ResponseEntity.status(204).body(signatureService.delete(passphrase));
    }

}
