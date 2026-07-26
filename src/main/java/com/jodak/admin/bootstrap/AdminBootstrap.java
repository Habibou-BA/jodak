package com.jodak.admin.bootstrap;

import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.enums.AdminRole;
import com.jodak.admin.repositories.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Crée le premier administrateur au démarrage s'il n'en existe aucun, à partir des variables
 * {@code ADMIN_EMAIL} / {@code ADMIN_PASSWORD}. Sans ces variables, aucun compte n'est créé
 * (le module reste inaccessible) — comportement volontaire pour ne pas introduire d'identifiants
 * faibles par défaut.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.email:}")
    private String bootstrapEmail;

    @Value("${admin.bootstrap.password:}")
    private String bootstrapPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }
        if (bootstrapEmail.isBlank() || bootstrapPassword.isBlank()) {
            log.warn("Aucun administrateur et variables ADMIN_EMAIL/ADMIN_PASSWORD absentes : "
                    + "aucun compte créé. Renseignez-les pour activer le back-office.");
            return;
        }
        AdminUser admin = AdminUser.builder()
                .email(bootstrapEmail.trim())
                .passwordHash(passwordEncoder.encode(bootstrapPassword))
                .role(AdminRole.ROLE_SUPER_ADMIN)
                .enabled(true)
                .failedLoginAttempts(0)
                .mustChangePassword(true)
                .twoFactorEnabled(false)
                .passwordChangedAt(OffsetDateTime.now())
                .build();
        adminUserRepository.save(admin);
        log.info("Administrateur initial créé : {}", admin.getEmail());
    }
}
