package org.bh_foundation.e_sign.repository;


import org.bh_foundation.e_sign.models.Document;
import org.bh_foundation.e_sign.models.DocumentApproval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentApprovalRepository extends JpaRepository<DocumentApproval, Long> {

    DocumentApproval findByDocumentIdAndUserId(Long documentId, Long userId);


    void deleteAllByDocument(Document document);
    
}
