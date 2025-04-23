package org.bh_foundation.e_sign.services.data;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.Document;
import org.bh_foundation.e_sign.models.DocumentApproval;
import org.bh_foundation.e_sign.models.Signature;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.DocumentApprovalRepository;
import org.bh_foundation.e_sign.repository.DocumentRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.bh_foundation.e_sign.services.storage.FileStorageService;
import org.bh_foundation.e_sign.utils.Crypt;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final Crypt crypt;

    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            DocumentApprovalRepository documentApprovalRepository,
            JwtService jwtService,
            FileStorageService fileStorageService,
            HttpServletRequest servletRequest,
            PasswordEncoder passwordEncoder,
            Crypt crypt) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.documentApprovalRepository = documentApprovalRepository;
        this.jwtService = jwtService;
        this.fileStorageService = fileStorageService;
        this.servletRequest = servletRequest;
        this.passwordEncoder = passwordEncoder;
        this.crypt = crypt;
    }

    public ResponseDto<?> getRequested() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Unauthorized"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        List<Document> documents = documentRepository.findAllBySigners(user).reversed();
        List<Document> dataDocuments = new ArrayList<>();
        for (Document doc : documents) {
            for (DocumentApproval dap : doc.getDocumentApprovals()) {
                if (dap.getUser().getId() == userId) {
                    List<DocumentApproval> listDap = new ArrayList<>();
                    listDap.add(dap);
                    doc.setDocumentApprovals(listDap);
                }
            }
            Set<User> docSign = new HashSet<>();
            docSign.add(user);
            doc.setSigners(docSign);
            dataDocuments.add(doc);
        }
        return new ResponseDto<>(200, "OK", dataDocuments);
    }

    public ResponseDto<?> getRequestedById(String id) throws Exception {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Long decryptedId = Long.parseLong(crypt.decryptString(id));
        Document document = documentRepository.findById(decryptedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!document.getSigners().contains(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return new ResponseDto<>(200, "OK", document);
    }

    public ResponseDto<?> getMine() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        List<Document> payload = user.getDocuments().reversed();
        return new ResponseDto<>(200, "OK", payload);
    }

    public ResponseDto<?> getMineById(String id) throws Exception {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Long decryptedId = Long.parseLong(crypt.decryptString(id));
        Document document = documentRepository.findById(decryptedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!document.getApplicant().equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return new ResponseDto<>(200, "OK", document);
    }

    public ResponseDto<?> getMineSigned() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        List<Document> documents = documentRepository.findAllSignedByUser(user).reversed();
        return new ResponseDto<>(200, "OK", documents);
    }

    public ResponseDto<?> send(String title, boolean orderSign, MultipartFile file,
            List<Long> signersId,
            List<Integer> pageNumbers)
            throws IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        if (signersId == null || signersId.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");

        Set<User> signers = new HashSet<>();
        List<User> signersList = userRepository.findAllById(signersId);
        signers.addAll(signersList);
        List<DocumentApproval> approvals = new ArrayList<>();
        Integer index = 0;
        for (User signerItem : signersList) {
            DocumentApproval docApp = new DocumentApproval();
            if (orderSign) {
                if (index == 1)
                    docApp.setEnableSign(true);
                else
                    docApp.setEnableSign(false);
            } else
                docApp.setEnableSign(true);
            docApp.setApproved(false);
            docApp.setDenied(false);
            docApp.setUser(signerItem);
            docApp.setPageNumber(pageNumbers.get(index));
            approvals.add(docApp);
            index++;
        }

        if (file == null || file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
        String url = fileStorageService.store(file, "document", 50 * 1024 * 1024, List.of("application/pdf"));

        Document document = new Document();
        document.setApplicant(user);
        document.setTitle(title);
        document.setUrl(url);
        document.setOrderSign(orderSign || false);
        document.setEnabled(false);
        document.setRequestCount(signers.size());
        document.setSignedCount(0);

        for (DocumentApproval dap : approvals) {
            dap.setDocument(document);
        }

        document.setDocumentApprovals(approvals);
        document.setCreatedAt(LocalDateTime.now());
        documentRepository.save(document);
        return new ResponseDto<>(201, "Created", document);
    }

    public ResponseDto<?> approve(String documentId) throws Exception {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Long decryptedId = Long.parseLong(crypt.decryptString(documentId));
        Document document = documentRepository.findById(decryptedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "document not found"));
        if (document.getOrderSign() && !document.getSigners().toArray()[document.getSignedCount()].equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid order");
        DocumentApproval approval = documentApprovalRepository.findByDocumentIdAndUserId(decryptedId, userId);
        approval.setApproved(true);
        approval.setDenied(false);
        documentApprovalRepository.save(approval);
        return new ResponseDto<>(200, "document approved", null);
    }

    public ResponseDto<?> deny(String documentId) throws Exception {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Long decryptedId = Long.parseLong(crypt.decryptString(documentId));
        Document document = documentRepository.findById(decryptedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "document not found"));
        if (document.getOrderSign() && !document.getSigners().toArray()[document.getSignedCount()].equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid order");
        DocumentApproval approval = documentApprovalRepository.findByDocumentIdAndUserId(decryptedId, userId);
        approval.setApproved(false);
        approval.setDenied(true);
        documentApprovalRepository.save(approval);
        return new ResponseDto<>(200, "document approved", null);
    }

    public ResponseDto<?> sign(String documentId, MultipartFile signedFile, String passphrase)
            throws IOException, Exception {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Long decryptedId = Long.parseLong(crypt.decryptString(documentId));
        Document document = documentRepository.findById(decryptedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "document not found"));

        if (signedFile == null || signedFile.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "document required");
        if (!document.getSigners().contains(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (document.getOrderSign() && !document.getSigners().toArray()[document.getSignedCount()].equals(user))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        DocumentApproval approval = documentApprovalRepository.findByDocumentIdAndUserId(decryptedId, userId);
        if (approval == null || approval.getApproved().equals(false))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        Signature signature = user.getSignature();
        boolean isPassphraseValid = passwordEncoder.matches(passphrase, signature.getPassphrase());
        if (!isPassphraseValid)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        String url = fileStorageService.store(signedFile, "document", 50 * 1024 * 1024, List.of("application/pdf"));
        document.setUrl(url);
        document.setSignedCount(document.getSignedCount() + 1);
        if (document.getSignedCount().equals(document.getRequestCount()))
            document.setSignedAt(LocalDateTime.now());
        document = documentRepository.save(document);
        approval.setSignedDocument(url);
        documentApprovalRepository.save(approval);

        return new ResponseDto<>(200, "Document signed successfully", document);
    }

    public ResponseDto<?> delete(String id) throws IOException, Exception {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Long decryptedId = Long.parseLong(crypt.decryptString(id));
        Document document = documentRepository.findById(decryptedId)
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
