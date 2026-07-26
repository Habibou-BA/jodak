package com.jodak.admin;

import com.jodak.admin.dtos.TokenResponse;
import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.enums.AdminRole;
import com.jodak.admin.exceptions.AccountLockedException;
import com.jodak.admin.exceptions.InvalidCredentialsException;
import com.jodak.admin.repositories.AdminUserRepository;
import com.jodak.admin.security.JwtService;
import com.jodak.admin.services.AdminLogService;
import com.jodak.admin.services.AuthService;
import com.jodak.admin.services.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AdminLogService adminLogService;

    @InjectMocks
    private AuthService authService;

    private AdminUser admin() {
        return AdminUser.builder()
                .id(1L).email("admin@jodak.sn").passwordHash("hash")
                .role(AdminRole.ROLE_ADMIN).enabled(true).failedLoginAttempts(0)
                .mustChangePassword(true).twoFactorEnabled(false)
                .build();
    }

    @Test
    @DisplayName("login réussi émet des jetons et trace le succès")
    void loginSucceeds() {
        AdminUser admin = admin();
        when(adminUserRepository.findByEmailIgnoreCase("admin@jodak.sn")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("pw", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(admin)).thenReturn("access-token");
        when(refreshTokenService.issue(eq(admin), any(), any())).thenReturn("refresh-token");
        when(jwtService.accessTtlSeconds()).thenReturn(900L);

        TokenResponse response = authService.login("admin@jodak.sn", "pw", "1.2.3.4", "UA");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900);
        assertThat(response.mustChangePassword()).isTrue();
        verify(adminLogService).record(1L, "LOGIN", true, null, "1.2.3.4", "UA");
    }

    @Test
    @DisplayName("login inconnu lève 401 sans révéler l'absence du compte")
    void loginUnknownFails() {
        when(adminUserRepository.findByEmailIgnoreCase("x@y.z")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("x@y.z", "pw", null, null))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(adminUserRepository, never()).save(any());
        verify(adminLogService).record(eq(null), eq("LOGIN"), eq(false), any(), any(), any());
    }

    @Test
    @DisplayName("mot de passe incorrect incrémente le compteur d'échecs")
    void wrongPasswordIncrementsCounter() {
        AdminUser admin = admin();
        when(adminUserRepository.findByEmailIgnoreCase("admin@jodak.sn")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("admin@jodak.sn", "bad", null, null))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(admin.getFailedLoginAttempts()).isEqualTo(1);
        verify(adminUserRepository).save(admin);
    }

    @Test
    @DisplayName("compte verrouillé lève 423")
    void lockedAccountFails() {
        AdminUser admin = admin();
        admin.setLockedUntil(OffsetDateTime.now().plusMinutes(10));
        when(adminUserRepository.findByEmailIgnoreCase("admin@jodak.sn")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> authService.login("admin@jodak.sn", "pw", null, null))
                .isInstanceOf(AccountLockedException.class);
    }
}
