package org.bh_foundation.e_sign.models;

// import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "p_sign_user")
public class pSignUser {
    
    @Id
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "approved", nullable = false)
    // @Value("#{props.approved}")
    private boolean approved=false;

}
