package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface CertificateRepository extends JpaRepository<Certificate, String> {

    Certificate findBySerialNumber(String serialNumber);
    
    @Modifying
    @Transactional
    @Query("UPDATE Certificate c SET c.p12 = NULL WHERE c.expire < CURRENT_TIMESTAMP")
    int nullifyP12IfExpired();

}
