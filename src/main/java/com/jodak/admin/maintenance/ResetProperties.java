package com.jodak.admin.maintenance;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de réinitialisation (préfixe {@code admin.reset}).
 */
@ConfigurationProperties(prefix = "admin.reset")
public record ResetProperties(boolean enabled, String confirmationPhrase) {
}
