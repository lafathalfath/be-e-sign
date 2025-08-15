package org.bh_foundation.e_sign.controllers;

import java.io.IOException;
import java.util.List;

import org.bh_foundation.e_sign.dto.DocSize;
import org.bh_foundation.e_sign.dto.RenderChoice;
import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.services.data.DocumentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.itextpdf.kernel.geom.Rectangle;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/requested-list")
    public ResponseEntity<ResponseDto<?>> getRequested() {
        return ResponseEntity.ok(documentService.getRequested());
    }

    @GetMapping("/get-list")
    public ResponseEntity<?> getMine() {
        return ResponseEntity.ok(documentService.getMine());
    }

    @GetMapping("/requested")
    public ResponseEntity<?> getRequestedById(@RequestParam(name = "document") String id) throws Exception {
        // String decodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
        // String decodedId = URLDecoder.decode(id, StandardCharsets.UTF_8.toString());
        return ResponseEntity.ok(documentService.getRequestedById(id));
        // return ResponseEntity.ok(id);
    }

    @GetMapping("/get")
    public ResponseEntity<ResponseDto<?>> getMineById(
            @RequestParam(name = "document") String id) throws Exception {
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

    @PutMapping("/approve")
    public ResponseEntity<?> approve(@RequestParam(name = "document") String id) throws Exception {
        return ResponseEntity.ok(documentService.approve(id));
    }

    @PutMapping("/deny")
    public ResponseEntity<ResponseDto<?>> deny(@RequestParam(name = "document") String id) throws Exception {
        return ResponseEntity.ok(documentService.deny(id));
    }

    @PutMapping("/sign")
    public ResponseEntity<?> sign(
            @RequestParam(name = "document") String id,
            @RequestParam MultipartFile file,
            @RequestParam String passphrase,
            @RequestParam RenderChoice render_choice,
            @RequestParam String rect,
            @RequestParam String doc_size) throws IOException, Exception {
        String[] rectParts = rect.split(",");
        String[] docSizeParts = doc_size.split(",");
        Rectangle rectangle = new Rectangle(
                Float.parseFloat(rectParts[0]),
                Float.parseFloat(rectParts[1]),
                Float.parseFloat(rectParts[2]),
                Float.parseFloat(rectParts[3]));
        DocSize docSize = new DocSize(Float.parseFloat(docSizeParts[0]), Float.parseFloat(docSizeParts[1]));
        return ResponseEntity.ok(documentService.sign(id, file, passphrase, render_choice, rectangle, docSize));
        // return ResponseEntity.ok(rectangle);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto<?>> delete(@RequestParam(name = "document") String id)
            throws IOException, Exception {
        return ResponseEntity.status(204).body(documentService.delete(id));
    }

    @PutMapping("/sign-self")
    public ResponseEntity<?> newSign(
            @RequestParam MultipartFile file,
            @RequestParam Integer page,
            @RequestParam RenderChoice render_choice,
            @RequestParam String passphrase,
            @RequestParam String rect,
            @RequestParam String doc_size) throws Exception {
        String[] rectParts = rect.split(",");
        String[] docSizeParts = doc_size.split(",");
        Rectangle rectangle = new Rectangle(
                Float.parseFloat(rectParts[0]),
                Float.parseFloat(rectParts[1]),
                Float.parseFloat(rectParts[2]),
                Float.parseFloat(rectParts[3]));
        DocSize docSize = new DocSize(Float.parseFloat(docSizeParts[0]), Float.parseFloat(docSizeParts[1]));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("[SIGNED]" + file.getOriginalFilename()).build());
        return new ResponseEntity<>(documentService.signSelf(file, page, render_choice, passphrase, rectangle, docSize),
                headers, HttpStatus.OK);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @RequestParam MultipartFile file) {
        return ResponseEntity.ok(documentService.verify(file));
    }

    @GetMapping("/get-url-by-filename/{filename}")
    public ResponseEntity<?> getUrlByFilename(@PathVariable String filename) {
        String url = documentService.getUrlByFilename(filename);
        // String url = "lzkvx";
        return ResponseEntity.ok(url);
    }

}
