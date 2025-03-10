package org.bh_foundation.e_sign.services.data;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.RequestedDocument;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.RequestedDocumentRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.bh_foundation.e_sign.services.storage.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class RequestedDocumentService {
    
    private final RequestedDocumentRepository requestedDocumentRepository;
    private final JwtService jwtService;
    private final HttpServletRequest servletRequest;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public RequestedDocumentService(
        RequestedDocumentRepository requestedDocumentRepository,
        JwtService jwtService,
        HttpServletRequest servletRequest,
        UserRepository userRepository,
        FileStorageService fileStorageService
    ) {
        this.requestedDocumentRepository = requestedDocumentRepository;
        this.jwtService = jwtService;
        this.servletRequest = servletRequest;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public ResponseDto<?> send(String title, boolean orderSign, MultipartFile document, List<Long> signers) throws IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User applicant = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (signers == null || signers.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "signers not specified");
        Set<User> signerSet = new HashSet<>();
        signerSet.addAll(userRepository.findAllById(signers));
        if (document == null || document.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "document not specified");
        String fileUrl = fileStorageService.store(document, "document");

        RequestedDocument requestedDocument = new RequestedDocument();
        requestedDocument.setApplicant(applicant);
        requestedDocument.setTitle(title);
        requestedDocument.setOrderSign(orderSign);
        requestedDocument.setUsers(signerSet);
        requestedDocument.setUrl(fileUrl);

        return new ResponseDto<>(201, "document requested", 
        requestedDocumentRepository.save(requestedDocument));
    }

}
