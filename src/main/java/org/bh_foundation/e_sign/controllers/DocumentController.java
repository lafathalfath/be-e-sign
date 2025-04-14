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

    @GetMapping("/get")
    public ResponseEntity<?> getMine() {
        return ResponseEntity.ok(documentService.getMine());
    }

    @GetMapping("/sign/{id}")
    public ResponseEntity<?> getRequestedById(@PathVariable String id) throws Exception {
        // String decodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
        // String decodedId = URLDecoder.decode(id, StandardCharsets.UTF_8.toString());
        return ResponseEntity.ok(documentService.getRequestedById(id));
        // return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> getMineById(
            @PathVariable String id) throws Exception {
        return ResponseEntity.ok(documentService.getMineById(id));
    }

    @GetMapping("/signed")
    public ResponseEntity<ResponseDto<?>> getMineSigned() {
        return ResponseEntity.ok(documentService.getMineSigned());
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(
            @RequestParam String title,
            @RequestParam boolean order_sign,
            @RequestParam MultipartFile file,
            @RequestParam List<Long> signers_id,
            @RequestParam List<Integer> page_number) throws IOException {
        return ResponseEntity.status(201).body(documentService.send(title, order_sign, file, signers_id, page_number));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ResponseDto<?>> approve(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(documentService.approve(id));
    }

    @PutMapping("/{id}/deny")
    public ResponseEntity<ResponseDto<?>> deny(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(documentService.deny(id));
    }

    @PutMapping("/{id}/sign")
    public ResponseEntity<?> sign(
            @PathVariable String id,
            @RequestParam MultipartFile file,
            @RequestParam String passphrase) throws IOException, Exception {
        return ResponseEntity.ok(documentService.sign(id, file, passphrase));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ResponseDto<?>> delete(@PathVariable String id) throws IOException, Exception {
        return ResponseEntity.status(204).body(documentService.delete(id));
    }

}
