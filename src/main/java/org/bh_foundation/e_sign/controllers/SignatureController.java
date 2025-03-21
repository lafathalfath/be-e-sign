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
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(signatureService.getImage());
    }

    @GetMapping("/get-certificate")
    public ResponseEntity<?> getCertificate() {
        return ResponseEntity.ok(signatureService.getCertificate());
    }

    @PostMapping("/store-sign")
    public ResponseEntity<?> storeSign(
            @RequestParam(required = true) MultipartFile sign) throws IOException {
        return ResponseEntity.status(201).body(signatureService.storeSign(sign));
    }

    @PostMapping("/store-sign-base64")
    public ResponseEntity<?> storeSign(
            @RequestParam(required = true) byte[]  bytes) {
        return ResponseEntity.status(201).body(signatureService.storeSignBase64(bytes));
        // return ResponseEntity.status(201).body(bytes);
    }

    @PostMapping("/store-certificate")
    public ResponseEntity<?> storeCertificate(
            @RequestParam(required = true) String passphrase,
            @RequestParam(required = true, name = "expire_in") Integer expireIn) {
        return ResponseEntity.status(201).body(signatureService.storeCertificate(passphrase, expireIn));
        // return ResponseEntity.status(201).body("passphrase: " + passphrase + "\nexpire in: " + expireIn);
    }

    @PutMapping("/extends")
    public ResponseEntity<?> extend(@RequestParam(required = true) String passphrase) {
        return ResponseEntity.status(200).body(signatureService.extend(passphrase));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam(required = true) String passphrase) {
        return ResponseEntity.ok(signatureService.delete(passphrase));
    }

}
