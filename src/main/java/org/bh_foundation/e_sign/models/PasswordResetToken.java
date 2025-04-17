package org.bh_foundation.e_sign.models;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_token")
public class PasswordResetToken {
    
    @Id
    @Column(name = "email", nullable = false, unique = true)
    @Email
    @NotNull
    private String email;

    @Column(name = "token", nullable = false, unique = true)
    @NotNull
    private String token;

    @Column(name = "expired_at", nullable = false)
    @DateTimeFormat(pattern = "yyy-MM-dd HH:mm:ss")
    private LocalDateTime expiredAt;

}
