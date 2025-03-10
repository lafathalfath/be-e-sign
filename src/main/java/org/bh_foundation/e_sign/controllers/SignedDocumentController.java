package org.bh_foundation.e_sign.controllers;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.SignedDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signed-document")
public class SignedDocumentController {
    
    private final SignedDocumentService signedDocumentService;

    public SignedDocumentController(SignedDocumentService signedDocumentService) {
        this.signedDocumentService = signedDocumentService;
    }

    @GetMapping
    public ResponseEntity<ResponseDto<?>> getAll() {
        return ResponseEntity.ok(signedDocumentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> getById(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(signedDocumentService.getById(id));
    }

}
