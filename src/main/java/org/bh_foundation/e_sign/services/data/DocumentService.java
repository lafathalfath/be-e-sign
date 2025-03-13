package org.bh_foundation.e_sign.services.data;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.Document;
import org.bh_foundation.e_sign.models.DocumentApproval;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.DocumentApprovalRepository;
import org.bh_foundation.e_sign.repository.DocumentRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.bh_foundation.e_sign.services.storage.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentApprovalRepository documentApprovalRepository;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;
    private final HttpServletRequest servletRequest;

    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            DocumentApprovalRepository documentApprovalRepository,
            JwtService jwtService,
            FileStorageService fileStorageService,
            HttpServletRequest servletRequest) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.documentApprovalRepository = documentApprovalRepository;
        this.jwtService = jwtService;
        this.fileStorageService = fileStorageService;
        this.servletRequest = servletRequest;
    }

    public ResponseDto<?> getRequested() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Unauthorized"));
        List<Document> documents = documentRepository.findAllBySigners(user);
        return new ResponseDto<>(200, "OK", documents);
    }

    public ResponseDto<?> getRequestedById(Long id) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!document.getSigners().contains(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return new ResponseDto<>(200, "OK", document);
    }

    public ResponseDto<?> getMine() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        List<Document> documents = user.getDocuments();
        return new ResponseDto<>(200, "OK", documents);
    }

    public ResponseDto<?> getMineById(Long id) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!document.getApplicant().equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return new ResponseDto<>(200, "OK", document);
    }

    public ResponseDto<?> send(String title, boolean orderSign, MultipartFile file, List<Long> signersId)
            throws IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        if (signersId == null || signersId.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
        Set<User> signers = new HashSet<>();
        signers.addAll(userRepository.findAllById(signersId));

        if (file == null || file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
        String url = fileStorageService.store(file, "document", 5 * 1024 * 1024, List.of("application/pdf"));

        Document document = new Document();
        document.setApplicant(user);
        document.setTitle(title);
        document.setUrl(url);
        document.setOrderSign(orderSign || false);
        document.setEnabled(false);
        document.setRequestCount(signers.toArray().length);
        document.setSignedCount(0);
        document.setSigners(signers);
        document = documentRepository.save(document);
        return new ResponseDto<>(201, "Created", document);
    }

    public ResponseDto<?> approve(Long documentId) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "document not found"));
        if (document.getOrderSign() && !document.getSigners().toArray()[document.getSignedCount()].equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid order");
        DocumentApproval approval = documentApprovalRepository.findByDocumentIdAndUserId(documentId, userId);
        approval.setApproved(true);
        documentApprovalRepository.save(approval);
        return new ResponseDto<>(200, "document approved", null);
    }

    public ResponseDto<?> sign(Long documentId, MultipartFile signedFile) throws IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "document not found"));

        if (signedFile == null || signedFile.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "document required");
        if (!document.getSigners().contains(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (document.getOrderSign() && !document.getSigners().toArray()[document.getSignedCount()].equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        boolean isApproved = documentApprovalRepository.findByDocumentIdAndUserId(documentId, userId).getApproved();
        if (!isApproved)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        String url = fileStorageService.store(signedFile, "document", 5 * 1024 * 1024, List.of("application/pdf"));
        document.setUrl(url);
        document.setSignedCount(document.getSignedCount() + 1);
        document = documentRepository.save(document);

        return new ResponseDto<>(200, "Document signed successfully", document);
    }

    public ResponseDto<?> delete(Long id) throws IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document Not Found"));
        if (!document.getApplicant().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");
        if (document.getSignedCount() > 0)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Document In Signed");
        document.getSigners().clear();
        documentRepository.save(document);
        documentApprovalRepository.deleteAllByDocument(document);
        fileStorageService.deleteByUrl(document.getUrl());
        documentRepository.delete(document);
        return new ResponseDto<>(204, "Document Deleted", null);
    }

}
