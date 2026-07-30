package com.jodak.admin.dataimport.importers;

import com.jodak.admin.dataimport.ImportRowOutcome;
import com.jodak.admin.dataimport.RowData;
import com.jodak.admin.dataimport.RowImporter;
import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.entities.Athlete;
import com.jodak.entities.Epreuve;
import com.jodak.entities.Discipline;
import com.jodak.entities.Resultat;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Import d'une ligne de résultat : résolution de l'épreuve (libellé + discipline + date) et de
 * l'athlète (nom + prénom + naissance), rang valide, médaille dérivée du rang (RM-12). Les
 * cohérences « un seul athlète par rang » et « un seul résultat par athlète et épreuve » sont
 * garanties par la base ; on les vérifie en amont pour un message clair.
 */
@Component
@RequiredArgsConstructor
public class ResultatRowImporter implements RowImporter {

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ResultatRepository resultatRepository;
    private final EpreuveRepository epreuveRepository;
    private final AthleteRepository athleteRepository;
    private final DisciplineRepository disciplineRepository;

    @Override
    public ImportJobType type() {
        return ImportJobType.RESULTAT;
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("epreuveLabel", "discipline", "date",
                "athleteLastName", "athleteFirstName", "athleteBirthDate", "rank");
    }

    @Override
    public ImportRowOutcome importRow(RowData row, ImportMode mode, DuplicateStrategy strategy) {
        Integer rank = parseInt(row.get("rank"));
        if (rank == null || rank < 1) {
            return ImportRowOutcome.failed("rank", "INVALID", "Rang invalide (entier ≥ 1).");
        }

        String disciplineName = trim(row.get("discipline"));
        Discipline discipline = disciplineName == null ? null
                : disciplineRepository.findByNameIgnoreCase(disciplineName).orElse(null);
        if (discipline == null) {
            return ImportRowOutcome.failed("discipline", "FK", "Discipline inconnue : " + disciplineName);
        }
        String epreuveLabel = trim(row.get("epreuveLabel"));
        LocalDate epreuveDate = parseDate(row.get("date"));
        if (epreuveDate == null) {
            return ImportRowOutcome.failed("date", "INVALID", "Date invalide (AAAA-MM-JJ ou JJ/MM/AAAA).");
        }
        Epreuve epreuve = epreuveLabel == null ? null
                : epreuveRepository.findByLabelIgnoreCaseAndDiscipline_IdAndEventDate(
                        epreuveLabel, discipline.getId(), epreuveDate).orElse(null);
        if (epreuve == null) {
            return ImportRowOutcome.failed("epreuveLabel", "FK",
                    "Épreuve inconnue : " + epreuveLabel + " (" + disciplineName + ", " + epreuveDate + ")");
        }

        String lastName = trim(row.get("athleteLastName"));
        String firstName = trim(row.get("athleteFirstName"));
        LocalDate birthDate = parseDate(row.get("athleteBirthDate"));
        if (birthDate == null) {
            return ImportRowOutcome.failed("athleteBirthDate", "INVALID", "Date de naissance invalide.");
        }
        Athlete athlete = (lastName == null || firstName == null) ? null
                : athleteRepository.findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndBirthDate(
                        lastName, firstName, birthDate).orElse(null);
        if (athlete == null) {
            return ImportRowOutcome.failed("athleteLastName", "FK",
                    "Athlète inconnu : " + lastName + " " + firstName + " (" + birthDate + ")");
        }

        // Un athlète ne peut avoir qu'un résultat par épreuve.
        if (resultatRepository.existsByEpreuve_IdAndAthlete_Id(epreuve.getId(), athlete.getId())) {
            return switch (strategy) {
                case REJECT -> ImportRowOutcome.failed("athleteLastName", "DUPLICATE",
                        "Résultat déjà présent pour cet athlète dans cette épreuve.");
                case SKIP, UPDATE -> ImportRowOutcome.skipped(
                        "Résultat déjà présent pour " + lastName + " " + firstName + ".");
            };
        }
        // Un seul athlète par rang dans une épreuve (un seul or, etc.).
        if (resultatRepository.existsByEpreuve_IdAndRankPosition(epreuve.getId(), rank)) {
            return ImportRowOutcome.failed("rank", "CONFLICT",
                    "Rang " + rank + " déjà attribué dans l'épreuve « " + epreuveLabel + " ».");
        }

        if (mode == ImportMode.DRY_RUN) {
            return ImportRowOutcome.imported("RESULTAT", null);
        }
        try {
            Resultat resultat = Resultat.builder()
                    .epreuve(epreuve).athlete(athlete).rankPosition(rank).build();
            resultat.assignMedalFromRank();
            Resultat saved = resultatRepository.save(resultat);
            return ImportRowOutcome.imported("RESULTAT", saved.getId());
        } catch (DataIntegrityViolationException ex) {
            return ImportRowOutcome.failed("rank", "CONFLICT",
                    "Conflit d'unicité (athlète/rang déjà présent dans l'épreuve).");
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
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
        if (value == null || value.isBlank()) {
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
}
