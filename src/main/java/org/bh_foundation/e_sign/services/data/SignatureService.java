package org.bh_foundation.e_sign.services.data;

import org.bh_foundation.e_sign.dto.ResponseDto;
import org.bh_foundation.e_sign.models.Signature;
import org.bh_foundation.e_sign.repository.SignatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SignatureService {
    
    private final SignatureRepository signatureRepository;

    public SignatureService(SignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    public ResponseDto<Signature> get() {
        Long id = (long) 1; // change with id from relation on authenticated user
        Signature signature = signatureRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(404, null, null));
        return new ResponseDto<>(200, "ok", signature);
    }

    public ResponseDto<Signature> store(Signature request) {
        Signature signature = signatureRepository.save(request);
        return new ResponseDto<>(201, "created", signature);
    }

    public ResponseDto<Signature> destroy() {
        Long id = (long) 1; // change with id from relation on authenticated user
        signatureRepository.deleteById(id);
        return new ResponseDto<>(204, "no content", null);
    }

}
