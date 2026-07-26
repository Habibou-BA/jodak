package com.jodak.admin.repositories;

import com.jodak.admin.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Accès aux refresh tokens.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Query("select r from RefreshToken r join fetch r.admin where r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashWithAdmin(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.admin.id = :adminId and r.revoked = false")
    int revokeAllForAdmin(@Param("adminId") Long adminId);
}
