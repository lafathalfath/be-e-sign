package org.bh_foundation.e_sign.services.data;

import java.io.IOException;
import java.util.List;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.Stamp;
import org.bh_foundation.e_sign.models.User;
import org.bh_foundation.e_sign.repository.StampRepository;
import org.bh_foundation.e_sign.repository.UserRepository;
import org.bh_foundation.e_sign.services.auth.JwtService;
import org.bh_foundation.e_sign.services.storage.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class StampService {

    private final StampRepository stampRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final HttpServletRequest servletRequest;
    private final FileStorageService storageService;

    public StampService(
            StampRepository stampRepository,
            UserRepository userRepository,
            JwtService jwtService,
            HttpServletRequest servletRequest,
            FileStorageService storageService) {
        this.stampRepository = stampRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.servletRequest = servletRequest;
        this.storageService = storageService;
    }

    public ResponseDto<?> get() {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        Stamp stamp = stampRepository.findAll().getFirst();
        return new ResponseDto<>(200, "OK", stamp);
    }

    public ResponseDto<?> storeOrUpdate(MultipartFile file) throws IOException {
        Long userId = jwtService.extractUserId(servletRequest.getHeader("Authorization"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (user.getVerifiedAt() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user unverified");
        if (file == null || file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
        String url = storageService.store(file, "stamp", 2*1024*1024, List.of("image/*"));
        Stamp stamp = stampRepository.findAll().getFirst();
        if (stamp == null) stamp = new Stamp();
        stamp.setUrl(url);
        Stamp uploadedStamp = stampRepository.save(stamp);
        return new ResponseDto<>(
                201,
                "CREATED",
                uploadedStamp);
    }

}
