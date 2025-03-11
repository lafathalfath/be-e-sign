package org.bh_foundation.e_sign.repository;

import java.util.Optional;

import org.bh_foundation.e_sign.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u ORDER BY u.id ASC")
    Optional<User> findFirst();

}
