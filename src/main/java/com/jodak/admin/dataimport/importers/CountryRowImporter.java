package com.jodak.admin.dataimport.importers;

import com.jodak.admin.dataimport.ImportRowOutcome;
import com.jodak.admin.dataimport.RowData;
import com.jodak.admin.dataimport.RowImporter;
import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.entities.Country;
import com.jodak.repositories.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Import d'une ligne de nation (clé naturelle : code ISO alpha-3, RM-01). Le code est normalisé en
 * majuscules ; le libellé est unique (insensible à la casse).
 */
@Component
@RequiredArgsConstructor
public class CountryRowImporter implements RowImporter {

    private final CountryRepository countryRepository;

    @Override
    public ImportJobType type() {
        return ImportJobType.COUNTRY;
    }

    @Override
    public List<String> requiredColumns() {
        return List.of("code", "name");
    }

    @Override
    public ImportRowOutcome importRow(RowData row, ImportMode mode, DuplicateStrategy strategy) {
        String code = normalizeCode(row.get("code"));
        if (code == null || !code.matches("^[A-Z]{3}$")) {
            return ImportRowOutcome.failed("code", "INVALID", "Code nation invalide (3 lettres, ex. SEN).");
        }
        String name = trim(row.get("name"));
        if (name == null || name.isBlank()) {
            return ImportRowOutcome.failed("name", "REQUIRED", "Le nom de la nation est obligatoire.");
        }
        if (name.length() > 100) {
            return ImportRowOutcome.failed("name", "INVALID", "Nom de nation trop long (max 100).");
        }

        Country existing = countryRepository.findByCodeIgnoreCase(code).orElse(null);
        if (existing != null) {
            return switch (strategy) {
                case REJECT -> ImportRowOutcome.failed("code", "DUPLICATE", "Nation déjà existante : " + code);
                case SKIP -> ImportRowOutcome.skipped("Nation déjà existante : " + code);
                case UPDATE -> {
                    if (mode == ImportMode.DRY_RUN) {
                        yield ImportRowOutcome.updated("COUNTRY", existing.getId());
                    }
                    existing.setName(name);
                    Country saved = countryRepository.save(existing);
                    yield ImportRowOutcome.updated("COUNTRY", saved.getId());
                }
            };
        }
        // Nouveau code : le libellé ne doit pas être déjà pris par une autre nation.
        if (countryRepository.existsByNameIgnoreCase(name)) {
            return ImportRowOutcome.failed("name", "DUPLICATE", "Nom de nation déjà utilisé : " + name);
        }
        if (mode == ImportMode.DRY_RUN) {
            return ImportRowOutcome.imported("COUNTRY", null);
        }
        try {
            Country saved = countryRepository.save(Country.builder().code(code).name(name).build());
            return ImportRowOutcome.imported("COUNTRY", saved.getId());
        } catch (DataIntegrityViolationException ex) {
            return ImportRowOutcome.failed("code", "DUPLICATE", "Nation déjà existante : " + code);
        }
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
