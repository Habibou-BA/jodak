package com.jodak;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base des tests d'intégration : démarre un PostgreSQL réel via Testcontainers et exécute les
 * migrations Flyway. Nécessite un démon Docker disponible ; lancé par {@code mvn verify}.
 */
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");
}
