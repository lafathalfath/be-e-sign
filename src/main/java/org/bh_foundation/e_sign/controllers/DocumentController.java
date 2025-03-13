package org.bh_foundation.e_sign.controllers;

import java.io.IOException;
import java.util.List;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/sign")
    public ResponseEntity<ResponseDto<?>> getRequested() {
        return ResponseEntity.ok(documentService.getRequested());
    }

    @GetMapping
    public ResponseEntity<ResponseDto<?>> getMine() {
        return ResponseEntity.ok(documentService.getMine());
    }

    @GetMapping("/sign/{id}")
    public ResponseEntity<ResponseDto<?>> getRequestedById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getRequestedById(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> getMineById(
            @PathVariable Long id) {
        return ResponseEntity.ok(documentService.getMineById(id));
    }

    @PostMapping("/send")
    public ResponseEntity<ResponseDto<?>> send(
            @RequestParam String title,
            @RequestParam boolean order_sign,
            @RequestParam MultipartFile file,
            @RequestParam List<Long> signers_id) throws IOException {
        return ResponseEntity.status(201).body(documentService.send(title, order_sign, file, signers_id));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ResponseDto<?>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.approve(id));
    }

    @PutMapping("/{id}/sign")
    public ResponseEntity<ResponseDto<?>> sign(
            @PathVariable Long id,
            @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(documentService.sign(id, file));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ResponseDto<?>> delete(@PathVariable Long id) throws IOException {
        return ResponseEntity.status(204).body(documentService.delete(id));
    }

}
