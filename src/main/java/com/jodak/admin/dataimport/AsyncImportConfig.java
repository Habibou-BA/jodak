package com.jodak.admin.dataimport;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Active l'exécution asynchrone des imports et configure l'exécuteur dédié (borné).
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(ImportProperties.class)
public class AsyncImportConfig {

    public static final String IMPORT_EXECUTOR = "importTaskExecutor";

    @Bean(IMPORT_EXECUTOR)
    public ThreadPoolTaskExecutor importExecutor(ImportProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int pool = Math.max(1, properties.maxConcurrentJobs());
        executor.setCorePoolSize(pool);
        executor.setMaxPoolSize(pool);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("import-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
