package org.bh_foundation.e_sign.services.data;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.Signature;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.SignatureRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class SignatureService {

    private final SignatureRepository signatureRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest servletRequest;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public SignatureService(SignatureRepository signatureRepository, UserRepository userRepository,
            HttpServletRequest servletRequest, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.signatureRepository = signatureRepository;
        this.userRepository = userRepository;
        this.servletRequest = servletRequest;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseDto<?> getImage() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("signature",
                "data:" + signature.getType() + ";base64," + Base64.getEncoder().encodeToString(signature.getBytes()));
        if (signature.getExpire() != null)
            payload.put("isExpired", LocalDateTime.now().isAfter(signature.getExpire()));
        else
            payload.put("isExpired", false);
        return new ResponseDto<>(200, "ok", payload);
    }

    public ResponseDto<?> getCertificate() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        return new ResponseDto<>(200, "ok", signature);
    }

    public ResponseDto<?> storeSign(MultipartFile image) throws IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        if (image == null || image.isEmpty())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        if (image.getSize() > 2 * 1024 * 1024)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        byte[] bytes = image.getBytes();
        Signature signature = user.getSignature();
        if (signature == null) {
            signature = new Signature();
            signature.setUser(user);
            signature.setIsEnabled(false);
            signature.setCreatedAt(null);
            signature.setPassphrase(null);
            signature.setExpire(null);
        }
        signature.setBytes(bytes);
        signature.setType(image.getContentType());
        signatureRepository.save(signature); //
        return new ResponseDto<>(201, "created", signature);
    }

    public ResponseDto<?> storeSignBase64(byte[] bytes) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        if (bytes == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Signature signature = user.getSignature();
        if (signature == null) {
            signature = new Signature();
            signature.setUser(user);
            signature.setIsEnabled(false);
            signature.setType("image/png");
            signature.setCreatedAt(null);
            signature.setPassphrase(null);
            signature.setExpire(null);
        }
        signature.setBytes(bytes);
        signatureRepository.save(signature);
        return new ResponseDto<>(201, "created", signature);
    }

    public ResponseDto<?> storeCertificate(String passphrase, Integer expireIn) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        if (passphrase == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid passphrase");
        Signature signature = user.getSignature();
        if (signature.getExpire() != null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (signature.getPassphrase() != null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (signature.getIsEnabled() == true)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        signature.setCreatedAt(LocalDateTime.now());
        signature.setPassphrase(passwordEncoder.encode(passphrase));
        signature.setExpire(LocalDateTime.now().plusDays(expireIn));
        signature.setIsEnabled(true);
        signatureRepository.save(signature);
        return new ResponseDto<>(201, "created", signature);
    }

    public ResponseDto<?> extend(String passphrase, Integer extendInDays) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        if (!passwordEncoder.matches(passphrase, signature.getPassphrase()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid passphrase");
        if (LocalDateTime.now().isBefore(signature.getExpire().minusMonths(1)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "signature hasn't expired yet");
        if (extendInDays < 1)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "extend in days must be greater than 0");
        signature.setExpire(LocalDateTime.now().plusDays(extendInDays));
        signature.setExtensionDate(LocalDateTime.now());
        signature = signatureRepository.save(signature);
        return new ResponseDto<>(200, "signature updated successfully", null);
    }

    public ResponseDto<?> revoke(String passphrase) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        if (!passwordEncoder.matches(passphrase, signature.getPassphrase()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid passphrase");
        signature.setExpire(null);
        signature.setIsEnabled(false);
        signature.setPassphrase(null);
        signatureRepository.save(signature);
        return new ResponseDto<>(200, "signature revoked successfully", null);
    }

    public ResponseDto<Signature> delete() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        user.setSignature(null);
        userRepository.save(user);
        return new ResponseDto<>(204, "signature deleted", null);
    }

}
