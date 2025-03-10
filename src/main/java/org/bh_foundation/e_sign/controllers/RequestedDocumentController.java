package org.bh_foundation.e_sign.controllers;

import java.io.IOException;
import java.util.List;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.RequestedDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/request-sign")
public class RequestedDocumentController {

    private final RequestedDocumentService requestedDocumentService;

    public RequestedDocumentController(
            RequestedDocumentService requestedDocumentService) {
        this.requestedDocumentService = requestedDocumentService;
    }

    @PostMapping("/send")
    public ResponseEntity<ResponseDto<?>> send(
            @RequestParam String title,
            @RequestParam boolean order_sign,
            @RequestParam MultipartFile document,
            @RequestParam List<Long> signers) throws IOException {
        return ResponseEntity.status(200).body(requestedDocumentService.send(title, order_sign, document, signers));
    }

}
