package com.jodak;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base des tests d'intégration : démarre un PostgreSQL réel (Testcontainers) et exécute les
 * migrations Flyway. Nécessite un démon Docker disponible ; lancé par {@code mvn verify}.
 *
 * <p>Le conteneur est un <b>singleton</b> démarré une seule fois pour toute la JVM de test et
 * partagé par toutes les classes d'intégration (via {@link DynamicPropertySource}). On évite ainsi
 * qu'un cycle de vie par classe n'arrête le conteneur entre deux classes réutilisant le même
 * contexte Spring mis en cache.</p>
 */
@ActiveProfiles("test")
@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
