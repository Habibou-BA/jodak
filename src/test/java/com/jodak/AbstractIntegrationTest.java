package com.jodak;

import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base des tests d'intégration : démarre un PostgreSQL réel (Testcontainers) et exécute les
 * migrations Flyway. Nécessite un démon Docker disponible ; lancé par {@code mvn verify}.
 *
 * <p>Le conteneur est un <b>singleton</b> démarré une seule fois pour toute la JVM de test et
 * partagé par toutes les classes d'intégration (via {@link DynamicPropertySource}).</p>
 *
 * <p>Avant chaque test, les tables transactionnelles sont vidées dans l'ordre des clés étrangères.
 * Le référentiel des nations (données de référence Flyway) est conservé.</p>
 */
@ActiveProfiles("test")
@SpringBootTest
@WithMockUser(roles = "ADMIN") // les mutations /api/v1/** sont protégées (Option A)
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

    @Autowired
    protected ResultatRepository resultatRepository;
    @Autowired
    protected AthleteRepository athleteRepository;
    @Autowired
    protected EpreuveRepository epreuveRepository;
    @Autowired
    protected DisciplineRepository disciplineRepository;

    @BeforeEach
    void resetDatabase() {
        resultatRepository.deleteAll();
        athleteRepository.deleteAll();
        epreuveRepository.deleteAll();
        disciplineRepository.deleteAll();
    }
}
