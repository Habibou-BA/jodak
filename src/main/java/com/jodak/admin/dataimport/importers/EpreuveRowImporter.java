package com.jodak.admin.dataimport.importers;

import com.jodak.admin.dataimport.ImportRowOutcome;
import com.jodak.admin.dataimport.RowData;
import com.jodak.admin.dataimport.RowImporter;
import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Import d'une ligne d'épreuve : résolution de la discipline par nom, normalisation de la date,
 * détection de doublon (libellé + discipline + date).
 */
@Component
@RequiredArgsConstructor
public class EpreuveRowImporter implements RowImporter {

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EpreuveRepository epreuveRepository;
    private final DisciplineRepository disciplineRepository;

    @Override
    public ImportJobType type() {
        return ImportJobType.EPREUVE;
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("label", "discipline", "date");
    }

    @Override
    public ImportRowOutcome importRow(RowData row, ImportMode mode, DuplicateStrategy strategy) {
        String label = trim(row.get("label"));
        if (label == null || label.isBlank()) {
            return ImportRowOutcome.failed("label", "REQUIRED", "Le libellé de l'épreuve est obligatoire.");
        }
        if (label.length() > 150) {
            return ImportRowOutcome.failed("label", "INVALID", "Libellé trop long (max 150).");
        }
        String disciplineName = trim(row.get("discipline"));
        Discipline discipline = disciplineName == null ? null
                : disciplineRepository.findByNameIgnoreCase(disciplineName).orElse(null);
        if (discipline == null) {
            return ImportRowOutcome.failed("discipline", "FK", "Discipline inconnue : " + disciplineName);
        }
        LocalDate date = parseDate(row.get("date"));
        if (date == null) {
            return ImportRowOutcome.failed("date", "INVALID", "Date invalide (AAAA-MM-JJ ou JJ/MM/AAAA).");
        }

        boolean duplicate = epreuveRepository
                .existsByLabelIgnoreCaseAndDiscipline_IdAndEventDate(label, discipline.getId(), date);
        if (duplicate) {
            return switch (strategy) {
                case REJECT -> ImportRowOutcome.failed("label", "DUPLICATE",
                        "Épreuve déjà existante : " + label);
                case SKIP, UPDATE -> ImportRowOutcome.skipped("Épreuve déjà existante : " + label);
            };
        }
        if (mode == ImportMode.DRY_RUN) {
            return ImportRowOutcome.imported("EPREUVE", null);
        }
        Epreuve saved = epreuveRepository.save(Epreuve.builder()
                .label(label).discipline(discipline).eventDate(date).build());
        return ImportRowOutcome.imported("EPREUVE", saved.getId());
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
