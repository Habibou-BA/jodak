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
import com.jodak.entities.Discipline;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de l'import asynchrone (CSV) : DRY_RUN sans écriture, COMMIT persistant,
 * rapport d'erreurs. Utilise une authentification JWT réelle (l'identifiant du créateur du job
 * provient du token).
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

    private long upload(String content, String jobType, String mode) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "import.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
        String body = mockMvc.perform(multipart("/api/admin/imports")
                        .file(file)
                        .param("jobType", jobType)
                        .param("format", "CSV")
                        .param("mode", mode)
                        .param("duplicateStrategy", "SKIP")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long uploadXlsx(byte[] content, String jobType, String mode) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "import.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
        String body = mockMvc.perform(multipart("/api/admin/imports")
                        .file(file)
                        .param("jobType", jobType)
                        .param("format", "XLSX")
                        .param("mode", mode)
                        .param("duplicateStrategy", "SKIP")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    /** Construit un classeur XLSX en mémoire : première ligne = en-tête, puis une ligne par valeur. */
    private byte[] xlsx(String header, String... rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("data");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue(header);
            for (int i = 0; i < rows.length; i++) {
                sheet.createRow(i + 1).createCell(0).setCellValue(rows[i]);
            }
            workbook.write(out);
            return out.toByteArray();
        }
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
    @DisplayName("DRY_RUN valide sans rien écrire en base")
    void dryRunDoesNotWrite() throws Exception {
        long jobId = upload("name\nJudo\nEscrime\n", "DISCIPLINE", "DRY_RUN");
        JsonNode job = awaitTerminal(jobId);

        assertThat(job.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(job.get("importedRows").asLong()).isEqualTo(2);
        assertThat(job.get("failedRows").asLong()).isZero();
        assertThat(disciplineRepository.count()).isZero();
    }

    @Test
    @DisplayName("COMMIT persiste les disciplines importées")
    void commitPersists() throws Exception {
        long jobId = upload("name\nJudo\nEscrime\n", "DISCIPLINE", "COMMIT");
        JsonNode job = awaitTerminal(jobId);

        assertThat(job.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(job.get("importedRows").asLong()).isEqualTo(2);
        assertThat(disciplineRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Les lignes fautives sont ignorées et rapportées")
    void reportsErrors() throws Exception {
        disciplineRepository.save(Discipline.builder().name("Athlétisme").build());
        String csv = """
                lastName,firstName,gender,birthDate,countryCode,discipline,heightCm,weightKg
                Bolt,Usain,Homme,1986-08-21,JAM,Athlétisme,195,94
                Kane,Bob,Homme,1992-02-02,FRA,DisciplineInconnue,180,80
                """;
        long jobId = upload(csv, "ATHLETE", "COMMIT");
        JsonNode job = awaitTerminal(jobId);

        assertThat(job.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(job.get("importedRows").asLong()).isEqualTo(1);
        assertThat(job.get("failedRows").asLong()).isEqualTo(1);
        assertThat(athleteRepository.count()).isEqualTo(1);

        String errors = mockMvc.perform(get("/api/admin/imports/{id}/errors", jobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(errors).get("totalElements").asLong()).isEqualTo(1);
    }

    @Test
    @DisplayName("COMMIT d'un fichier XLSX persiste les disciplines importées")
    void xlsxCommitPersists() throws Exception {
        long jobId = uploadXlsx(xlsx("name", "Judo", "Escrime", "Taekwondo"), "DISCIPLINE", "COMMIT");
        JsonNode job = awaitTerminal(jobId);

        assertThat(job.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(job.get("importedRows").asLong()).isEqualTo(3);
        assertThat(job.get("failedRows").asLong()).isZero();
        assertThat(disciplineRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Une extension incohérente avec le format est rejetée (400)")
    void rejectsExtensionMismatch() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "import.txt", "text/plain",
                "name\nJudo\n".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/admin/imports")
                        .file(file)
                        .param("jobType", "DISCIPLINE")
                        .param("format", "CSV")
                        .param("mode", "DRY_RUN")
                        .param("duplicateStrategy", "SKIP")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
