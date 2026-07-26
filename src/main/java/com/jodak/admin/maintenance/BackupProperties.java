package com.jodak.admin.maintenance;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de sauvegarde (préfixe {@code backup}).
 */
@ConfigurationProperties(prefix = "backup")
public record BackupProperties(String storageDir) {
}
