package com.jodak.admin.maintenance;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Active les propriétés de sauvegarde et de réinitialisation.
 */
@Configuration
@EnableConfigurationProperties({BackupProperties.class, ResetProperties.class})
public class MaintenanceConfig {
}
