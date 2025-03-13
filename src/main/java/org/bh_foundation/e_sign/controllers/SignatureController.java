package org.bh_foundation.e_sign.controllers;

import java.io.IOException;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/signature")
public class SignatureController {

    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @SuppressWarnings("null")
    @GetMapping("/get")
    public ResponseEntity<?> get(@Valid @RequestParam String passphrase, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(signatureService.get(passphrase));
    }

    @SuppressWarnings("null")
    @PostMapping("/store")
    public ResponseEntity<?> store(
            @Valid @RequestParam String passphrase,
            @Valid @RequestParam MultipartFile sign, BindingResult result) throws IOException {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.status(201).body(signatureService.store(passphrase, sign));
    }

    @SuppressWarnings("null")
    @PutMapping("/extends")
    public ResponseEntity<?> extend(@Valid @RequestParam String passphrase, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.status(200).body(signatureService.extend(passphrase));
    }

    @SuppressWarnings("null")
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@Valid @RequestParam String passphrase, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(signatureService.delete(passphrase));
    }

}
