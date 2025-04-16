package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionRepository extends JpaRepository<Session, String> {

    @Query("SELECT ses FROM Session ses WHERE ses.info = :infoIpUserAgent")
    Session findByInfo(String infoIpUserAgent);
    
}
