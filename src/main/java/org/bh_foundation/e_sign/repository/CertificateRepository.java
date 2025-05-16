package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, String> {

    Certificate findBySerialNumber(String serialNumber);
    
}
