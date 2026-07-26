package com.jodak.admin.services;

import com.jodak.admin.dtos.TokenResponse;
import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.entities.RefreshToken;
import com.jodak.admin.exceptions.AccountLockedException;
import com.jodak.admin.exceptions.InvalidCredentialsException;
import com.jodak.admin.repositories.AdminUserRepository;
import com.jodak.admin.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Authentification administrateur : connexion (avec verrouillage anti-brute-force), rafraîchissement
 * (avec rotation du refresh token) et déconnexion (révocation).
 *
 * <p>Volontairement non {@code @Transactional} au niveau méthode : chaque écriture est committée
 * immédiatement, afin que l'incrémentation du compteur d'échecs persiste malgré l'exception levée.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final String GENERIC = "Identifiants invalides.";

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AdminLogService adminLogService;

    public TokenResponse login(String email, String password, String ip, String userAgent) {
        OffsetDateTime now = OffsetDateTime.now();
        AdminUser admin = adminUserRepository.findByEmailIgnoreCase(email).orElse(null);

        if (admin == null) {
            adminLogService.record(null, "LOGIN", false, "Compte inconnu", ip, userAgent);
            throw new InvalidCredentialsException(GENERIC);
        }
        if (admin.isLocked(now)) {
            adminLogService.record(admin.getId(), "LOGIN", false, "Compte verrouillé", ip, userAgent);
            throw new AccountLockedException("Compte temporairement verrouillé. Réessayez plus tard.");
        }
        if (!admin.isEnabled() || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            admin.registerFailedLogin(MAX_ATTEMPTS, now.plus(LOCK_DURATION));
            adminUserRepository.save(admin);
            adminLogService.record(admin.getId(), "LOGIN", false, "Identifiants invalides", ip, userAgent);
            throw new InvalidCredentialsException(GENERIC);
        }

        admin.registerSuccessfulLogin(now);
        adminUserRepository.save(admin);
        TokenResponse tokens = issueTokens(admin, ip, userAgent);
        adminLogService.record(admin.getId(), "LOGIN", true, null, ip, userAgent);
        return tokens;
    }

    public TokenResponse refresh(String rawRefreshToken, String ip, String userAgent) {
        RefreshToken token = refreshTokenService.validate(rawRefreshToken);
        AdminUser admin = token.getAdmin();
        refreshTokenService.revoke(token); // rotation
        return issueTokens(admin, ip, userAgent);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeRaw(rawRefreshToken);
    }

    private TokenResponse issueTokens(AdminUser admin, String ip, String userAgent) {
        String accessToken = jwtService.generateAccessToken(admin);
        String refreshToken = refreshTokenService.issue(admin, ip, userAgent);
        return new TokenResponse("Bearer", accessToken, refreshToken,
                jwtService.accessTtlSeconds(), admin.isMustChangePassword());
    }
}
