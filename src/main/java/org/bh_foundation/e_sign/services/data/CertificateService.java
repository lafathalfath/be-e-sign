package org.bh_foundation.e_sign.services.data;

import java.time.LocalDateTime;
import java.util.List;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.Certificate;
import org.bh_foundation.e_sign.models.Signature;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.CertificateRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.bh_foundation.e_sign.utils.CertificateReader;
import org.springframework.http.HttpStatus;
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
        if (!certificates.isEmpty() && (certificates.getLast().getExpire().isBefore(LocalDateTime.now())
                || !certificates.getLast().getIsRevoked())) {
                    Certificate cert = certificates.getLast();
                    cert.setIsRevoked(true);
                    certificateRepository.save(cert);
        }
        String certSubject = CertificateReader.getSubject();
        String subject = "Sub: E="+user.getEmail()+",CN="+user.getUsername()+"\nIss: "+certSubject;
        Certificate cert = new Certificate();
        cert.setCreatedAt(LocalDateTime.now());
        cert.setExpire(LocalDateTime.now().plusDays(expiration));
        cert.setPassphrase(passwordEncoder.encode(passphrase));
        cert.setSignature(signature);
        cert.setIsRevoked(false);
        cert.setSubject(subject);
        certificateRepository.save(cert);
        return new ResponseDto<>(201, "CREATED", "New certificate created successfully");
    }

    // public ResponseDto<?> extend(Integer days, String passphrase) {
    // Long userId =
    // jwtService.extractUserId(servletRequest.getHeader("Authorization"));
    // User user = userRepository.findById(userId)
    // .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
    // "forbidden"));
    // if (user.getVerifiedAt() == null)
    // throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
    // Signature signature = user.getSignature();
    // if (signature == null || signature.getCertificates().isEmpty())
    // throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Signature
    // Found");
    // Certificate cert = signature.getCertificates().getLast();
    // if (cert.getExpire().isAfter(LocalDateTime.now()))
    // throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Certificate already
    // expired");
    // if (cert.getIsRevoked())
    // throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Certificate already
    // revoked");
    // if (cert.getExpire().isBefore(cert.getExpire().minusMonths(1)))
    // throw new ResponseStatusException(HttpStatus.FORBIDDEN,
    // "Cannot extend until one month before expiration date");
    // if (!passwordEncoder.matches(passphrase, cert.getPassphrase()))
    // throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid
    // passphrase");
    // cert.setExpire(LocalDateTime.now().plusDays(days));
    // cert.setExtensionDate(LocalDateTime.now());
    // certificateRepository.save(cert);
    // return new ResponseDto<>(200, "OK", "Certificate extended successfully");
    // }

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
        certificateRepository.save(cert);
        return new ResponseDto<>(200, "OK", "Certificate revoked successfully");
    }

}
