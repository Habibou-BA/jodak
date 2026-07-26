package com.jodak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.enums.AdminRole;
import com.jodak.admin.repositories.AdminUserRepository;
import com.jodak.admin.repositories.ImportJobRepository;
import com.jodak.admin.repositories.RefreshTokenRepository;
import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.enums.Gender;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de l'export, de la sauvegarde et de la réinitialisation sécurisée
 * (authentification JWT réelle ; réinitialisation activée via propriétés de test).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "admin.reset.enabled=true",
        "admin.reset.confirmation-phrase=RESET-TEST"
})
class ExportBackupResetIT {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractIntegrationTest.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", AbstractIntegrationTest.POSTGRES::getUsername);
        registry.add("spring.datasource.password", AbstractIntegrationTest.POSTGRES::getPassword);
    }

    private static final String EMAIL = "admin@jodak.sn";
    private static final String PASSWORD = "MotDePasse-123!";

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
    private ResultatRepository resultatRepository;
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private EpreuveRepository epreuveRepository;
    @Autowired
    private DisciplineRepository disciplineRepository;
    @Autowired
    private CountryRepository countryRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        resultatRepository.deleteAll();
        athleteRepository.deleteAll();
        epreuveRepository.deleteAll();
        disciplineRepository.deleteAll();
        importJobRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        adminUserRepository.deleteAll();
        adminUserRepository.save(AdminUser.builder()
                .email(EMAIL).passwordHash(passwordEncoder.encode(PASSWORD))
                .role(AdminRole.ROLE_ADMIN).enabled(true).failedLoginAttempts(0)
                .mustChangePassword(false).twoFactorEnabled(false)
                .passwordChangedAt(OffsetDateTime.now()).build());

        Discipline discipline = disciplineRepository.save(Discipline.builder().name("Athlétisme").build());
        Country country = countryRepository.findAll().get(0);
        athleteRepository.save(Athlete.builder()
                .lastName("Bolt").firstName("Usain").gender(Gender.MALE)
                .birthDate(LocalDate.of(1986, 8, 21)).country(country).discipline(discipline)
                .heightCm(195).weightKg(94).build());

        String body = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = objectMapper.readTree(body).get("accessToken").asText();
    }

    @Test
    @DisplayName("L'export renvoie une archive ZIP")
    void exportReturnsZip() throws Exception {
        byte[] zip = mockMvc.perform(get("/api/admin/export")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(zip.length).isGreaterThan(0);
        assertThat(zip[0]).isEqualTo((byte) 'P');
        assertThat(zip[1]).isEqualTo((byte) 'K');
    }

    @Test
    @DisplayName("La sauvegarde crée un fichier téléchargeable")
    void backupCreatesDownloadableFile() throws Exception {
        String body = mockMvc.perform(post("/api/admin/backup")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode backup = objectMapper.readTree(body);
        assertThat(backup.get("checksum").asText()).isNotBlank();
        assertThat(backup.get("records").get("athletes").asLong()).isEqualTo(1);

        String fileName = backup.get("fileName").asText();
        mockMvc.perform(get("/api/admin/backup/{name}/download", fileName)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Réinitialisation refusée si la phrase ou le mot de passe est incorrect")
    void resetRejectsBadConfirmation() throws Exception {
        mockMvc.perform(post("/api/admin/reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"password\":\"" + PASSWORD + "\",\"confirmationPhrase\":\"MAUVAISE\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"password\":\"mauvais\",\"confirmationPhrase\":\"RESET-TEST\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Réinitialisation confirmée : sauvegarde puis suppression des données")
    void resetBacksUpThenDeletes() throws Exception {
        mockMvc.perform(post("/api/admin/reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"password\":\"" + PASSWORD + "\",\"confirmationPhrase\":\"RESET-TEST\"}"))
                .andExpect(status().isOk());

        assertThat(athleteRepository.count()).isZero();
        assertThat(disciplineRepository.count()).isZero();
        assertThat(countryRepository.count()).isGreaterThan(0); // le référentiel est préservé
    }
}
