package org.bh_foundation.e_sign.controllers;

import java.io.IOException;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.StampService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/stamp")
public class StampController {
    
    private final StampService stampService;

    public StampController(StampService stampService) {
        this.stampService = stampService;
    }

    @GetMapping("/get")
    public ResponseEntity<ResponseDto<?>> get() {
        return ResponseEntity.ok(stampService.get());
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseDto<?>> update(@RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(stampService.storeOrUpdate(file));
    }

}
