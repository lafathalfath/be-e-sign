package org.bh_foundation.e_sign.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "certificate")
public class Certificate {

    @Id
    @Column(name = "serial_number", unique = true)
    private String serialNumber;
    @Column(unique = true, nullable = false)
    private String subject;
    @Size(min = 6, message = "Passphrase must be at least 6 characters long")
    @NotNull
    private String passphrase;
    @NotNull
    private LocalDateTime expire;
    // @Column(name = "extension_date", nullable = true)
    // @NotNull
    // private LocalDateTime extensionDate;
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "is_revoked", nullable = false)
    @ColumnDefault("0")
    private Boolean isRevoked;

    // RELATIONS
    @ManyToOne
    @JoinColumn(name = "signature_id", referencedColumnName = "id", nullable = false)
    private Signature signature;

    // PREPRESISTS
    @PrePersist public void generateSerialNumber() {
        if (serialNumber == null)
            serialNumber = UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

}
