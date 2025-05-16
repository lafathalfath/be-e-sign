package org.bh_foundation.e_sign.repository;

import java.util.List;

import org.bh_foundation.e_sign.models.Document;
import org.bh_foundation.e_sign.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // @Query("SELECT d FROM Document d WHERE d = :user")
    List<Document> findAllBySigners(User user);

    @Query("SELECT d FROM Document d WHERE d.applicant = :user AND d.signedCount = d.requestCount")
    List<Document> findAllSignedByUser(User user);

    @Query("SELECT da.document FROM DocumentApproval da WHERE da.user = :user AND da.document.signedCount = da.document.requestCount")
    List<Document> findAllSignedByUserSigning(User user);
}
