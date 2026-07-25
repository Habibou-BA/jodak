package com.jodak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Active l'audit JPA (renseignement automatique de {@code createdAt} / {@code updatedAt}).
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
