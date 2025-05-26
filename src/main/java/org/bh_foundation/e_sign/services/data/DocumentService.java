package org.bh_foundation.e_sign.services.data;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.bh_foundation.e_sign.dto.DocSize;
import org.bh_foundation.e_sign.dto.RenderChoice;
import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.dto.SignMethodDto;
import org.bh_foundation.e_sign.models.Certificate;
import org.bh_foundation.e_sign.models.Document;
import org.bh_foundation.e_sign.models.DocumentApproval;
import org.bh_foundation.e_sign.models.Signature;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.DocumentApprovalRepository;
import org.bh_foundation.e_sign.repository.DocumentRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.bh_foundation.e_sign.services.location.LocationService;
import org.bh_foundation.e_sign.services.storage.FileStorageService;
import org.bh_foundation.e_sign.utils.Crypt;
import org.bh_foundation.e_sign.utils.ImageUtility;
import org.bh_foundation.e_sign.utils.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.PdfSignatureAppearance.RenderingMode;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class DocumentService {

    @Value("${server.base-url}")
    private String BASE_URL;

    @Value("${client.url}")
    private String CLIENT_URL;

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentApprovalRepository documentApprovalRepository;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;
    private final LocationService locationService;
    private final HttpServletRequest servletRequest;
    private final PasswordEncoder passwordEncoder;
    private final Crypt crypt;
    private final PDFVerifierService verifierService;

    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            DocumentApprovalRepository documentApprovalRepository,
            JwtService jwtService,
            FileStorageService fileStorageService,
            LocationService locationService,
            HttpServletRequest servletRequest,
            PasswordEncoder passwordEncoder,
            Crypt crypt,
            PDFVerifierService verifierService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.documentApprovalRepository = documentApprovalRepository;
        this.jwtService = jwtService;
        this.fileStorageService = fileStorageService;
        this.locationService = locationService;
        this.servletRequest = servletRequest;
        this.passwordEncoder = passwordEncoder;
        this.crypt = crypt;
        this.verifierService = verifierService;
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
            boolean onlyMe = false;
            if (doc.getSigners().size() == 1 && doc.getSigners().iterator().next().equals(user)) onlyMe = true;
            if (!onlyMe) {
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
        List<Document> documents = documentRepository.findAllSignedByUserSigning(user).reversed();
        // List<DocumentApproval> dovApp =
        // documentApprovalRepository.findAllSignedByUserSigning(user).reversed();
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

    public ResponseDto<?> sign(String documentId, MultipartFile signedFile, String passphrase,
            RenderChoice renderChoice, Rectangle rect, DocSize docSize)
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
        List<Certificate> certificates = signature.getCertificates();
        if (certificates.isEmpty())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Certificate certificate = certificates.getLast();
        if (!passwordEncoder.matches(passphrase, certificate.getPassphrase()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (LocalDateTime.now().isAfter(certificate.getExpire()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        SignMethodDto signMethodDto = signMethod(signedFile, approval.getPageNumber(), renderChoice, signature, user, rect, docSize, certificate.getP12(), passphrase);
        byte[] signedDocument = signMethodDto.getBlob();
        String url = fileStorageService.storeBlobCustomFilename(signedDocument, "document", signMethodDto.getFilename(), "pdf");
        document.setUrl(url);
        document.setSignedCount(document.getSignedCount() + 1);
        if (document.getSignedCount().equals(document.getRequestCount()))
            document.setSignedAt(LocalDateTime.now());
        document = documentRepository.save(document);
        approval.setSignedDocument(url);
        approval.setSerialNumber(certificate.getSerialNumber());
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

    public byte[] signSelf(MultipartFile file, Integer page, RenderChoice renderChoice, String passphrase,
            Rectangle rect,
            DocSize docSize) throws Exception {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature mySignature = user.getSignature();
        List<Certificate> certificates = mySignature.getCertificates();
        if (certificates.isEmpty())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Certificate certificate = certificates.getLast();
        if (!passwordEncoder.matches(passphrase, certificate.getPassphrase()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (LocalDateTime.now().isAfter(certificate.getExpire()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        SignMethodDto signMethodDto = signMethod(file, page, renderChoice, mySignature, user, rect, docSize, certificate.getP12(), passphrase);
        byte[] blob = signMethodDto.getBlob();
        String url = fileStorageService.storeBlobCustomFilename(blob, "document", signMethodDto.getFilename(), "pdf");
        Document newDoc = new Document();
        DocumentApproval documentApproval = new DocumentApproval();
        newDoc.setApplicant(user);
        newDoc.setCreatedAt(LocalDateTime.now());
        newDoc.setRequestCount(1);
        newDoc.setSignedCount(1);
        newDoc.setSignedAt(LocalDateTime.now());
        newDoc.setTitle(file.getOriginalFilename());
        newDoc.setUrl(url);
        newDoc.setEnabled(true);
        newDoc.setOrderSign(false);
        documentApproval.setUser(user);
        documentApproval.setApproved(true);
        documentApproval.setDenied(false);
        documentApproval.setPageNumber(page);
        documentApproval.setEnableSign(true);
        documentApproval.setSignedDocument(url);
        documentApproval.setSerialNumber(certificate.getSerialNumber());
        documentApproval.setDocument(newDoc);
        newDoc.setDocumentApprovals(List.of(documentApproval));
        documentRepository.save(newDoc);
        return blob;
    }

    private SignMethodDto signMethod(MultipartFile file, Integer page, RenderChoice renderChoice, Signature mySignature,
            User user,
            Rectangle rect,
            DocSize docSize, byte[] certificate, String passphrase) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(certificate), passphrase.toCharArray());
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, passphrase.toCharArray());
        java.security.cert.Certificate[] chain = ks.getCertificateChain(alias);

        ByteArrayOutputStream signedPdfOutput = new ByteArrayOutputStream();

        PdfReader reader = new PdfReader(file.getInputStream()).setUnethicalReading(true);
        PdfSigner signer = new PdfSigner(
                reader,
                signedPdfOutput,
                new StampingProperties().useAppendMode());
        PdfDocument pdfDoc = signer.getDocument();

        Rectangle pageSize = pdfDoc.getPage(page).getPageSize(); // current (to export) document

        float scaleW = pageSize.getWidth() / docSize.getWidth();
        float scaleH = pageSize.getHeight() / docSize.getHeight();

        Rectangle exportSignRectangle = new Rectangle(
                scaleW * rect.getX(),
                pageSize.getHeight() - (scaleH * rect.getHeight()) - (scaleH * rect.getY()),
                scaleW * rect.getWidth(),
                (pageSize.getHeight() / docSize.getHeight()) * rect.getHeight());
        String country = locationService.getCountryByIp();
        PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason("Document has signed")
                .setLocation(country)
                .setPageRect(exportSignRectangle)
                .setPageNumber(page);
        String filename = UUID.randomUUID().toString() + ".pdf";
        String downloadUrl = CLIENT_URL + "/esign/" + filename;
        String stamp = "digitally signed @ " + CLIENT_URL;
        if (CLIENT_URL.startsWith("http://"))
            stamp = stamp.replace("http://", "");
        else if (CLIENT_URL.startsWith("https://"))
            stamp = stamp.replace("https://", "");
        else
            stamp = "...";

        if (renderChoice.equals(RenderChoice.IMAGE)) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(mySignature.getBytes()));
            BufferedImage borderedImage = ImageUtility.addBorderImageSign(image, 10, 10, stamp, user, 24);
            ByteArrayOutputStream baosImage = new ByteArrayOutputStream();
            ImageIO.write(borderedImage, "png", baosImage);
            ImageData imageData = ImageDataFactory.create(baosImage.toByteArray());
            appearance.setSignatureGraphic(imageData);
        } else if (renderChoice.equals(RenderChoice.QR)) {
            byte[] qrcodebyte = QRCodeGenerator.generate(downloadUrl,
                    Math.min(exportSignRectangle.getWidth(), exportSignRectangle.getHeight()));
            BufferedImage bufferQr = ImageIO.read(new ByteArrayInputStream(qrcodebyte));
            BufferedImage textedQr = ImageUtility.addTextQr(bufferQr, user.getUsername(), CLIENT_URL);
            ByteArrayOutputStream baosQr = new ByteArrayOutputStream();
            ImageIO.write(textedQr, "png", baosQr);
            ImageData qrImagedata = ImageDataFactory.create(baosQr.toByteArray());
            appearance.setSignatureGraphic(qrImagedata);
        } else if (renderChoice.equals(RenderChoice.BOTH)) {
            BufferedImage ttdImage = ImageIO.read(new ByteArrayInputStream(mySignature.getBytes()));
            byte[] qrcodebyte = QRCodeGenerator.generate(downloadUrl,
                    ttdImage.getHeight() * 130 / 100);
            BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrcodebyte));

            BufferedImage combined = new BufferedImage(
                    qrImage.getWidth() + ttdImage.getWidth(),
                    ttdImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            Graphics graphics = combined.getGraphics();
            graphics.drawImage(qrImage, 0, 0, null);
            graphics.drawImage(ttdImage, qrImage.getWidth(), 0, null);
            graphics.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage borderedAndTextedQrSign = ImageUtility.addBorderTextQrSign(combined, CLIENT_URL);
            ImageIO.write(borderedAndTextedQrSign, "png", baos);
            ImageData combinedImage = ImageDataFactory.create(baos.toByteArray());
            appearance.setSignatureGraphic(combinedImage);
        }

        appearance.setRenderingMode(RenderingMode.GRAPHIC);
        appearance.setLayer2Text("");
        appearance.setReuseAppearance(false);
        signer.setFieldName(UUID.randomUUID().toString());

        IExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, "BC");
        IExternalDigest digest = new BouncyCastleDigest();

        signer.signDetached(digest, signature, chain, null, null, null, 0, PdfSigner.CryptoStandard.CADES);
        pdfDoc.close();

        byte[] outputByte = signedPdfOutput.toByteArray();
        SignMethodDto dto = new SignMethodDto();
        dto.setFilename(filename);
        dto.setBlob(outputByte);
        return dto;
    }

    public ResponseDto<?> verify(MultipartFile file) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        try (InputStream inputStream = file.getInputStream()) {
            return new ResponseDto<>(200, "OK", verifierService.verifySignature(inputStream));
        } catch (Exception e) {
            throw new RuntimeException("Verifikasi gagal: " + e.getMessage(), e);
        }
    }

    public String getUrlByFilename(String filename) {
        if (filename.equals(null)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String url = documentRepository.findUrlByFilename(filename);
        if (url.equals(null)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return url;
    }

}