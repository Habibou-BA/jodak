package com.jodak.admin.dataimport.importers;

import com.jodak.admin.dataimport.ImportRowOutcome;
import com.jodak.admin.dataimport.RowData;
import com.jodak.admin.dataimport.RowImporter;
import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.entities.Discipline;
import com.jodak.repositories.DisciplineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Import d'une ligne de discipline (clé naturelle : nom, insensible à la casse).
 */
@Component
@RequiredArgsConstructor
public class DisciplineRowImporter implements RowImporter {

    private final DisciplineRepository disciplineRepository;

    @Override
    public ImportJobType type() {
        return ImportJobType.DISCIPLINE;
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("name");
    }

    @Override
    public ImportRowOutcome importRow(RowData row, ImportMode mode, DuplicateStrategy strategy) {
        String name = trim(row.get("name"));
        if (name == null || name.isBlank()) {
            return ImportRowOutcome.failed("name", "REQUIRED", "Le nom de la discipline est obligatoire.");
        }
        if (disciplineRepository.existsByNameIgnoreCase(name)) {
            return switch (strategy) {
                case REJECT -> ImportRowOutcome.failed("name", "DUPLICATE", "Discipline déjà existante : " + name);
                case SKIP, UPDATE -> ImportRowOutcome.skipped("Discipline déjà existante : " + name);
            };
        }
        if (mode == ImportMode.DRY_RUN) {
            return ImportRowOutcome.imported("DISCIPLINE", null);
        }
        try {
            Discipline saved = disciplineRepository.save(Discipline.builder().name(name).build());
            return ImportRowOutcome.imported("DISCIPLINE", saved.getId());
        } catch (DataIntegrityViolationException ex) {
            return ImportRowOutcome.failed("name", "DUPLICATE", "Discipline déjà existante : " + name);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
