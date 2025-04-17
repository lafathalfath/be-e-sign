package org.bh_foundation.e_sign.repository;

import org.bh_foundation.e_sign.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    @Query("SELECT p FROM PasswordResetToken p WHERE p.token = :token")
    PasswordResetToken findByToken(String token);

    @Query("SELECT p FROM PasswordResetToken p WHERE p.email = :email")
    PasswordResetToken findByEmail(String email);

}
