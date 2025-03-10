package org.bh_foundation.e_sign.services.data;

import java.io.IOException;
import java.util.List;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.SignedDocument;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.SignedDocumentRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.bh_foundation.e_sign.services.storage.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class SignedDocumentService {

    private final SignedDocumentRepository signedDocumentRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;
    private final HttpServletRequest servletRequest;

    public SignedDocumentService(
            SignedDocumentRepository signedDocumentRepository,
            UserRepository userRepository,
            JwtService jwtService,
            FileStorageService fileStorageService,
            HttpServletRequest servletRequest) {
        this.signedDocumentRepository = signedDocumentRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.fileStorageService = fileStorageService;
        this.servletRequest = servletRequest;
    }

    public ResponseDto<?> getAll() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        return new ResponseDto<>(200, "OK", user.getSigned());
    }

    public ResponseDto<?> getById(Long id) {
        return new ResponseDto<>(200, "OK", signedDocumentRepository.findById(id));
    }

    // public ResponseDto<?> store(String title, MultipartFile document, List<User>
    // signers) throws IOException {
    // Long userId =
    // jwtService.extractUserId(servletRequest.getHeader("Authorization"));
    // User user = userRepository.findById(userId).orElseThrow(() -> new
    // ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    // if (document == null || document.isEmpty()) throw new
    // ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
    // String url = fileStorageService.store(document, "document");
    // SignedDocument signedDocument = new SignedDocument();
    // signedDocument.setApplicant(user);
    // signedDocument.setTitle(title);
    // signedDocument.setUrl(url);
    // signedDocument.set
    // }

}
