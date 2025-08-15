package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.Stamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StampRepository extends JpaRepository<Stamp, Long> {
    
}
