package org.bh_foundation.e_sign.controllers;

import java.util.Base64;

import org.bh_foundation.e_sign.utils.QRCodeGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
public class QRController {

    @GetMapping("/generate")
    public ResponseEntity<?> generate(
            @RequestParam String content,
            @RequestParam Float size) throws Exception {
        byte[] qr = QRCodeGenerator.generate(content, size);
        String payload = "data:image/png" + ";base64," + Base64.getEncoder().encodeToString(qr);
        return ResponseEntity.ok(payload);
    }

}
