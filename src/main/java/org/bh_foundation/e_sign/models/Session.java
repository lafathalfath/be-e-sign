package org.bh_foundation.e_sign.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "session")
public class Session {
    
    @Id
    @Column(name = "info", nullable = false, unique = true)
    @NotNull
    private String info;

    @Column(name = "attempts", nullable = false)
    @ColumnDefault("1")
    private Integer attempts;

    @Column(name = "expired_at", nullable = false, updatable = true, insertable = true)
    @DateTimeFormat(pattern = "yyy-MM-dd HH:mm:ss")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @DateTimeFormat(pattern = "yyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
