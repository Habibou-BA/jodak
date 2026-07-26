package com.jodak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.enums.AdminRole;
import com.jodak.admin.repositories.AdminUserRepository;
import com.jodak.admin.repositories.ImportJobRepository;
import com.jodak.admin.repositories.RefreshTokenRepository;
import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.repositories.AthleteRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration de la sécurité (Option A) et de l'authentification JWT réelle :
 * lecture publique, mutations et administration protégées, cycle login → jeton → mutation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuthIT {

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
    private AdminUserRepository adminUserRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ImportJobRepository importJobRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ResultatRepository resultatRepository;
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private EpreuveRepository epreuveRepository;
    @Autowired
    private DisciplineRepository disciplineRepository;

    @BeforeEach
    void setUp() {
        resultatRepository.deleteAll();
        athleteRepository.deleteAll();
        epreuveRepository.deleteAll();
        disciplineRepository.deleteAll();
        importJobRepository.deleteAll(); // import_job.created_by référence admin_user (RESTRICT)
        refreshTokenRepository.deleteAll();
        adminUserRepository.deleteAll();
        adminUserRepository.save(AdminUser.builder()
                .email(EMAIL).passwordHash(passwordEncoder.encode(PASSWORD))
                .role(AdminRole.ROLE_ADMIN).enabled(true).failedLoginAttempts(0)
                .mustChangePassword(false).twoFactorEnabled(false)
                .passwordChangedAt(OffsetDateTime.now()).build());
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    @Test
    @DisplayName("La lecture publique reste ouverte sans authentification")
    void publicReadIsOpen() throws Exception {
        mockMvc.perform(get("/api/v1/athletes")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Une mutation sans jeton est refusée (401)")
    void mutationWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/disciplines")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DisciplineRequest("Judo"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Une route d'administration sans jeton est refusée (401)")
    void adminRouteWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/anything")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Connexion puis mutation avec le jeton (201)")
    void loginThenMutateWithToken() throws Exception {
        String token = login(EMAIL, PASSWORD);
        mockMvc.perform(post("/api/v1/disciplines")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DisciplineRequest("Escrime"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Escrime"));
    }

    @Test
    @DisplayName("Mot de passe incorrect : 401")
    void wrongPasswordUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"mauvais\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Le journal d'administration est consultable et trace la connexion")
    void adminLogsAreListed() throws Exception {
        String token = login(EMAIL, PASSWORD);
        mockMvc.perform(get("/api/admin/logs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements",
                        org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
