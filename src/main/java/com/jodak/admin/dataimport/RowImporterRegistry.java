package com.jodak.admin.dataimport;

import com.jodak.admin.enums.ImportJobType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registre des importeurs disponibles, indexés par type de job.
 */
@Component
public class RowImporterRegistry {

    private final Map<ImportJobType, RowImporter> importers;

    public RowImporterRegistry(List<RowImporter> importers) {
        this.importers = importers.stream()
                .collect(Collectors.toMap(RowImporter::type, Function.identity()));
    }

    public RowImporter forType(ImportJobType type) {
        RowImporter importer = importers.get(type);
        if (importer == null) {
            throw new IllegalArgumentException("Type d'import non pris en charge : " + type);
        }
        return importer;
    }

    public boolean supports(ImportJobType type) {
        return importers.containsKey(type);
    }
}
