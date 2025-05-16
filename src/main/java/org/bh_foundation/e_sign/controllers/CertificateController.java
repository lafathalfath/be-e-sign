package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.services.data.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(
            CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(certificateService.getAll());
    }

    @GetMapping("/get-last")
    public ResponseEntity<?> getLast() {
        return ResponseEntity.ok(certificateService.getLast());
    }

    @PostMapping("/store")
    public ResponseEntity<?> store(
            @RequestParam Integer expiration,
            @RequestParam String passphrase) throws Exception {
        return ResponseEntity.ok(certificateService.store(expiration, passphrase));
    }

    @DeleteMapping("/{serial}/revoke")
    public ResponseEntity<?> revoke(
        @PathVariable String serial,
        @RequestParam String passphrase
    ) {
        return ResponseEntity.ok(certificateService.revoke(serial, passphrase));
    }

}
