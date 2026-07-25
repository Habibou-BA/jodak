package com.jodak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Active l'audit JPA (renseignement automatique de {@code createdAt} / {@code updatedAt}).
 *
 * <p>Un {@link DateTimeProvider} explicite renvoyant des {@link OffsetDateTime} est requis :
 * le fournisseur par défaut de Spring Data produit un {@code LocalDateTime}, non convertible
 * automatiquement vers le type {@code OffsetDateTime} des champs d'audit.</p>
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
