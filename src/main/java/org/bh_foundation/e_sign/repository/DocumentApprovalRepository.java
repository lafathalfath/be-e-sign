package org.bh_foundation.e_sign.repository;


import java.util.List;

import org.bh_foundation.e_sign.models.Document;
import org.bh_foundation.e_sign.models.DocumentApproval;
import org.bh_foundation.e_sign.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentApprovalRepository extends JpaRepository<DocumentApproval, Long> {

    DocumentApproval findByDocumentIdAndUserId(Long documentId, Long userId);

    @Query("SELECT da FROM DocumentApproval da WHERE da.user = :user AND da.document.signedCount = da.document.requestCount")
    List<DocumentApproval> findAllSignedByUserSigning(User user);

    void deleteAllByDocument(Document document);

    @Query("SELECT da FROM DocumentApproval da WHERE da.serialNumber = :serial")
    List<DocumentApproval> findBySerialNumber(String serial);
    
}
