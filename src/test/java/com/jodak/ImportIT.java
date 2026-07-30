package com.jodak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.enums.AdminRole;
import com.jodak.admin.repositories.AdminUserRepository;
import com.jodak.admin.repositories.ImportJobErrorRepository;
import com.jodak.admin.repositories.ImportJobRecordRepository;
import com.jodak.admin.repositories.ImportJobRepository;
import com.jodak.admin.repositories.RefreshTokenRepository;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.CountryRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de l'import « système » : le classeur unique d'initialisation
 * ({@code sample-imports/initialisation-systeme.xlsx}) charge toutes les entités en une passe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportIT {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractIntegrationTest.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", AbstractIntegrationTest.POSTGRES::getUsername);
        registry.add("spring.datasource.password", AbstractIntegrationTest.POSTGRES::getPassword);
    }

    private static final String EMAIL = "admin@jodak.sn";
    private static final String PASSWORD = "MotDePasse-123!";
    private static final Path INIT_FILE = Path.of("sample-imports/initialisation-systeme.xlsx");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminUserRepository adminUserRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ImportJobRepository importJobRepository;
    @Autowired
    private ImportJobErrorRepository importJobErrorRepository;
    @Autowired
    private ImportJobRecordRepository importJobRecordRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private DisciplineRepository disciplineRepository;
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private EpreuveRepository epreuveRepository;
    @Autowired
    private ResultatRepository resultatRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        resultatRepository.deleteAll();
        athleteRepository.deleteAll();
        epreuveRepository.deleteAll();
        disciplineRepository.deleteAll();
        countryRepository.deleteAll();
        importJobErrorRepository.deleteAll();
        importJobRecordRepository.deleteAll();
        importJobRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        adminUserRepository.deleteAll();
        adminUserRepository.save(AdminUser.builder()
                .email(EMAIL).passwordHash(passwordEncoder.encode(PASSWORD))
                .role(AdminRole.ROLE_ADMIN).enabled(true).failedLoginAttempts(0)
                .mustChangePassword(false).twoFactorEnabled(false)
                .passwordChangedAt(OffsetDateTime.now()).build());
        String body = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("accessToken").asText();
    }

    private long uploadInit(String mode, String strategy) throws Exception {
        byte[] content = Files.readAllBytes(INIT_FILE);
        MockMultipartFile file = new MockMultipartFile("file", "initialisation-systeme.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
        String body = mockMvc.perform(multipart("/api/admin/imports")
                        .file(file)
                        .param("mode", mode)
                        .param("duplicateStrategy", strategy)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private JsonNode awaitTerminal(long jobId) throws Exception {
        for (int i = 0; i < 100; i++) {
            String body = mockMvc.perform(get("/api/admin/imports/{id}", jobId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            JsonNode node = objectMapper.readTree(body);
            String status = node.get("status").asText();
            if (status.equals("COMPLETED") || status.equals("FAILED") || status.equals("CANCELLED")) {
                return node;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Le job d'import n'a pas atteint un état terminal à temps.");
    }

    @Test
    @DisplayName("Le classeur d'initialisation charge toutes les entités dans un système vide")
    void initialisationChargeToutLeSysteme() throws Exception {
        long jobId = uploadInit("COMMIT", "SKIP");
        JsonNode job = awaitTerminal(jobId);

        assertThat(job.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(job.get("failedRows").asLong()).isZero();
        assertThat(countryRepository.count()).isEqualTo(206);
        assertThat(disciplineRepository.count()).isEqualTo(32);
        assertThat(epreuveRepository.count()).isEqualTo(86);
        assertThat(athleteRepository.count()).isEqualTo(47);
        assertThat(resultatRepository.count()).isEqualTo(42);
    }

    @Test
    @DisplayName("La simulation (DRY_RUN) n'écrit rien en base")
    void simulationNecritRien() throws Exception {
        long jobId = uploadInit("DRY_RUN", "SKIP");
        JsonNode job = awaitTerminal(jobId);

        assertThat(job.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(countryRepository.count()).isZero();
        assertThat(disciplineRepository.count()).isZero();
        assertThat(athleteRepository.count()).isZero();
    }

    @Test
    @DisplayName("Un fichier non .xlsx est rejeté (400)")
    void rejetteFichierNonXlsx() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "donnees.csv", "text/csv",
                "code,name\nSEN,Sénégal\n".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/admin/imports")
                        .file(file)
                        .param("mode", "DRY_RUN")
                        .param("duplicateStrategy", "SKIP")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
