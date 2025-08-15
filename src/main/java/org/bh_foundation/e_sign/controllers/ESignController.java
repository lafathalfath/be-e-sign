package org.bh_foundation.e_sign.controllers;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.bh_foundation.e_sign.component.PathComponent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/esign")
public class ESignController {

    // private final PathComponent pathComponent;

    // public ESignController(PathComponent pathComponent) {
    // this.pathComponent = pathComponent;
    // }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<?> downloadDocument(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(PathComponent.STORAGE_PATH + "/document/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists())
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            // return ResponseEntity.ok(filePath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
