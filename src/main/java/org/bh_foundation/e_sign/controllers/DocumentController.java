package org.bh_foundation.e_sign.controllers;

import java.io.IOException;
import java.util.List;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

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

    @SuppressWarnings("null")
    @GetMapping("/sign/{id}")
    public ResponseEntity<ResponseDto<?>> getRequestedById(@Valid @PathVariable Long id, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(documentService.getRequestedById(id));
    }

    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> getMineById(
            @Valid @PathVariable Long id, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(documentService.getMineById(id));
    }

    @SuppressWarnings("null")
    @PostMapping("/send")
    public ResponseEntity<ResponseDto<?>> send(
            @Valid @RequestParam String title,
            @Valid @RequestParam boolean order_sign,
            @Valid @RequestParam MultipartFile file,
            @Valid @RequestParam List<Long> signers_id, BindingResult result) throws IOException {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.status(201).body(documentService.send(title, order_sign, file, signers_id));
    }

    @SuppressWarnings("null")
    @PutMapping("/{id}/approve")
    public ResponseEntity<ResponseDto<?>> approve(@Valid @PathVariable Long id, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(documentService.approve(id));
    }

    @SuppressWarnings("null")
    @PutMapping("/{id}/sign")
    public ResponseEntity<ResponseDto<?>> sign(
            @Valid @PathVariable Long id,
            @Valid @RequestParam MultipartFile file, BindingResult result) throws IOException {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.ok(documentService.sign(id, file));
    }

    @SuppressWarnings("null")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ResponseDto<?>> delete(@Valid @PathVariable Long id, BindingResult result)
            throws IOException {
        if (result.hasErrors())
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, result.getFieldError().getDefaultMessage(), null));
        return ResponseEntity.status(204).body(documentService.delete(id));
    }

}
