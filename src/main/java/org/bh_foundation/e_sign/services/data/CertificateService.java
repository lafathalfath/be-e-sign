package org.bh_foundation.e_sign.services.data;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.Certificate;
import org.bh_foundation.e_sign.models.Signature;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.CertificateRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.bh_foundation.e_sign.utils.CertificateGenerator;
import org.bouncycastle.asn1.x500.X500Name;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class CertificateService {

    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;
    private final HttpServletRequest servletRequest;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public CertificateService(
            UserRepository userRepository,
            CertificateRepository certificateRepository,
            HttpServletRequest servletRequest,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;
        this.servletRequest = servletRequest;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseDto<?> getAll() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Signature Found");
        List<Certificate> certificates = signature.getCertificates().reversed();
        return new ResponseDto<>(200, "OK", certificates);
    }

    public ResponseDto<?> getLast() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Signature Found");
        List<Certificate> certificates = signature.getCertificates();
        if (certificates.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Certificate Found");
        Certificate certificate = certificates.getLast();
        if (certificate.getIsRevoked())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Certificate already revoked");
        if (LocalDateTime.now().isAfter(certificate.getExpire()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Certificate already expired");
        return new ResponseDto<>(200, "OK", certificate);
    }

    public ResponseDto<?> store(Integer expiration, String passphrase) throws Exception {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Signature Found");
        List<Certificate> certificates = signature.getCertificates();
        if (!certificates.isEmpty()) {
            Certificate crt = certificates.getLast();
            if (LocalDateTime.now().isBefore(crt.getExpire()))
                crt.setIsRevoked(true);
            crt.setP12(null);
            certificateRepository.save(crt);
        }
        byte[] p12Blob = CertificateGenerator.generateP12(
                user.getEmail(),
                user.getUsername(),
                "Bogor",
                "West Java",
                "Indonesia",
                passphrase,
                expiration);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(p12Blob), passphrase.toCharArray());
        String alias = ks.aliases().nextElement();
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

        X500Name subjectName = new X500Name(cert.getSubjectX500Principal().getName());
        X500Name issuerName = new X500Name(cert.getIssuerX500Principal().getName());

        String subject = "Sub: " + subjectName.toString() +
                "\nIss: " + issuerName.toString();
        String serialNumber = cert.getSerialNumber().toString(16).toUpperCase();
        // if (certificateRepository.findBySerialNumber(serialNumber) != null)
        //     return new ResponseDto<>(201, "CREATED", "New certificate created successfully");
        LocalDateTime expire = cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Certificate entity = new Certificate();
        entity.setSerialNumber(serialNumber);
        // entity.setSerialNumber("0J9H8G7F7G845DF6");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpire(expire);
        entity.setPassphrase(passwordEncoder.encode(passphrase));
        entity.setSignature(signature);
        entity.setIsRevoked(false);
        entity.setSubject(subject);
        entity.setP12(p12Blob);
        // entity.setP12(null);
        certificateRepository.save(entity); // baris ini
        return new ResponseDto<>(201, "CREATED", "New certificate created successfully");
    }

    public ResponseDto<?> revoke(String serialNumber, String passphrase) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        if (signature == null || signature.getCertificates().isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Signature Found");
        Certificate cert = certificateRepository.findBySerialNumber(serialNumber);
        if (cert.getIsRevoked())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Certificate already revoked");
        if (!passwordEncoder.matches(passphrase, cert.getPassphrase()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid passphrase");
        cert.setIsRevoked(true);
        cert.setP12(null);
        certificateRepository.save(cert);
        return new ResponseDto<>(200, "OK", "Certificate revoked successfully");
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void nullifyP12IfExpired() {
        certificateRepository.nullifyP12IfExpired();
    }

}
