package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.SignedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignedDocumentRepository extends JpaRepository<SignedDocument, Long> {

    SignedDocument findOne(String url);
    
}
