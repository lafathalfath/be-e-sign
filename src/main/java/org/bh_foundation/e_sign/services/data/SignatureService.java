package org.bh_foundation.e_sign.services.data;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

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

    public ResponseDto<?> get(String passphrase) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        if (!passwordEncoder.matches(passphrase, signature.getPassphrase()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid passphrase");
        return new ResponseDto<>(200, "ok",
                "data:" + signature.getType() + ";base64," + Base64.getEncoder().encodeToString(signature.getBytes()));
    }

    public ResponseDto<Signature> store(String passphrase, MultipartFile image) throws IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        if (image == null || image.isEmpty())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        byte[] bytes = image.getBytes();
        Signature signature = new Signature();
        signature.setUser(user);
        signature.setExpire(LocalDateTime.now().plusYears(1));
        signature.setPassphrase(passwordEncoder.encode(passphrase));
        signature.setBytes(bytes);
        signature.setType(image.getContentType());
        signature = signatureRepository.save(signature);
        return new ResponseDto<>(201, "created", signature);
    }

    public ResponseDto<?> extend(String passphrase) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        if (!passwordEncoder.matches(passphrase, signature.getPassphrase()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid passphrase");
        if (LocalDateTime.now().isBefore(signature.getExpire().minusMonths(1)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "signature hasn't expired yet");
        signature.setExpire(LocalDateTime.now().plusYears(1));
        signature = signatureRepository.save(signature);
        return new ResponseDto<>(200, "signature updated successfully", null);
    }

    public ResponseDto<Signature> delete(String passphrase) {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden"));
        Signature signature = user.getSignature();
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "signature not found");
        if (!passwordEncoder.matches(passphrase, signature.getPassphrase()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid passphrase");
        signatureRepository.delete(signature);
        return new ResponseDto<>(204, "signature deleted", null);
    }

}
