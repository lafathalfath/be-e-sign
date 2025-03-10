package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.RequestedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestedDocumentRepository extends JpaRepository<RequestedDocument, Long> {
    
}
