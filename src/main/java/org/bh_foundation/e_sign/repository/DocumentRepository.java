package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // SignedDocument findOne(String url);
    
}
