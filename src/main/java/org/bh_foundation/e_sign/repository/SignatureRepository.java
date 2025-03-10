package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.Signature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureRepository extends JpaRepository<Signature, Long> {
    
}
