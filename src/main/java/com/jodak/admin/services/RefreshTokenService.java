package com.jodak.admin.services;

import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.entities.RefreshToken;
import com.jodak.admin.exceptions.InvalidCredentialsException;
import com.jodak.admin.repositories.RefreshTokenRepository;
import com.jodak.admin.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Émission, validation (avec rotation) et révocation des refresh tokens. Seul le hachage SHA-256
 * est stocké ; la valeur en clair n'existe que le temps de la réponse HTTP.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public String issue(AdminUser admin, String ip, String userAgent) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokenRepository.save(RefreshToken.builder()
                .admin(admin)
                .tokenHash(sha256(raw))
                .expiresAt(OffsetDateTime.now().plus(jwtProperties.refreshTtl()))
                .revoked(false)
                .ip(ip)
                .userAgent(userAgent)
                .build());
        return raw;
    }

    /** Valide un refresh token (avec son administrateur chargé). Lève 401 si invalide/expiré/révoqué. */
    public RefreshToken validate(String raw) {
        RefreshToken token = refreshTokenRepository.findByTokenHashWithAdmin(sha256(raw))
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token invalide."));
        if (!token.isUsable(OffsetDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token expiré ou révoqué.");
        }
        return token;
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void revokeRaw(String raw) {
        refreshTokenRepository.findByTokenHash(sha256(raw)).ifPresent(this::revoke);
    }

    static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponible", ex);
        }
    }
}
