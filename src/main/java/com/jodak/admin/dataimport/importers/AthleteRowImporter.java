package com.jodak.admin.dataimport.importers;

import com.jodak.admin.dataimport.ImportRowOutcome;
import com.jodak.admin.dataimport.RowData;
import com.jodak.admin.dataimport.RowImporter;
import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.enums.Gender;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.CountryRepository;
import com.jodak.repositories.DisciplineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Import d'une ligne d'athlète : normalisation (sexe, dates), résolution des références (nation par
 * code, discipline par nom), validation métier et détection de doublon (nom + prénom + naissance).
 */
@Component
@RequiredArgsConstructor
public class AthleteRowImporter implements RowImporter {

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AthleteRepository athleteRepository;
    private final CountryRepository countryRepository;
    private final DisciplineRepository disciplineRepository;

    @Override
    public ImportJobType type() {
        return ImportJobType.ATHLETE;
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("lastName", "firstName", "gender", "birthDate", "countryCode", "discipline",
                "heightCm", "weightKg");
    }

    @Override
    public ImportRowOutcome importRow(RowData row, ImportMode mode, DuplicateStrategy strategy) {
        String lastName = trim(row.get("lastName"));
        if (isBlank(lastName)) {
            return ImportRowOutcome.failed("lastName", "REQUIRED", "Le nom est obligatoire.");
        }
        String firstName = trim(row.get("firstName"));
        if (isBlank(firstName)) {
            return ImportRowOutcome.failed("firstName", "REQUIRED", "Le prénom est obligatoire.");
        }
        Gender gender = parseGender(row.get("gender"));
        if (gender == null) {
            return ImportRowOutcome.failed("gender", "INVALID", "Sexe invalide (attendu Homme/Femme).");
        }
        LocalDate birthDate = parseDate(row.get("birthDate"));
        if (birthDate == null) {
            return ImportRowOutcome.failed("birthDate", "INVALID", "Date invalide (AAAA-MM-JJ ou JJ/MM/AAAA).");
        }
        if (!birthDate.isBefore(LocalDate.now())) {
            return ImportRowOutcome.failed("birthDate", "INVALID", "La date de naissance doit être passée.");
        }
        Integer height = parseInt(row.get("heightCm"));
        if (height == null || height < 100 || height > 260) {
            return ImportRowOutcome.failed("heightCm", "INVALID", "Taille invalide (100–260 cm).");
        }
        Integer weight = parseInt(row.get("weightKg"));
        if (weight == null || weight < 30 || weight > 250) {
            return ImportRowOutcome.failed("weightKg", "INVALID", "Poids invalide (30–250 kg).");
        }
        String countryCode = trim(row.get("countryCode"));
        Country country = countryCode == null ? null
                : countryRepository.findByCodeIgnoreCase(countryCode).orElse(null);
        if (country == null) {
            return ImportRowOutcome.failed("countryCode", "FK", "Nation inconnue : " + countryCode);
        }
        String disciplineName = trim(row.get("discipline"));
        Discipline discipline = disciplineName == null ? null
                : disciplineRepository.findByNameIgnoreCase(disciplineName).orElse(null);
        if (discipline == null) {
            return ImportRowOutcome.failed("discipline", "FK", "Discipline inconnue : " + disciplineName);
        }

        boolean duplicate = athleteRepository
                .existsByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndBirthDate(lastName, firstName, birthDate);
        if (duplicate) {
            return switch (strategy) {
                case REJECT -> ImportRowOutcome.failed("lastName", "DUPLICATE",
                        "Athlète déjà présent : " + lastName + " " + firstName);
                case SKIP, UPDATE -> ImportRowOutcome.skipped(
                        "Athlète déjà présent : " + lastName + " " + firstName);
            };
        }
        if (mode == ImportMode.DRY_RUN) {
            return ImportRowOutcome.imported("ATHLETE", null);
        }
        Athlete saved = athleteRepository.save(Athlete.builder()
                .lastName(lastName).firstName(firstName).gender(gender).birthDate(birthDate)
                .country(country).discipline(discipline).heightCm(height).weightKg(weight).build());
        return ImportRowOutcome.imported("ATHLETE", saved.getId());
    }

    private Gender parseGender(String value) {
        if (isBlank(value)) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "homme", "h", "male", "m", "masculin" -> Gender.MALE;
            case "femme", "f", "female", "féminin", "feminin" -> Gender.FEMALE;
            default -> null;
        };
    }

    private LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        String v = value.trim();
        try {
            return LocalDate.parse(v);
        } catch (Exception ignored) {
            try {
                return LocalDate.parse(v, FR_DATE);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private Integer parseInt(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
