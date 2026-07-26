package com.jodak.admin.dataimport;

import com.jodak.admin.enums.ImportFormat;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Fabrique de lecteurs selon le format. (CSV pris en charge ; XLSX prévu au sous-palier suivant.)
 */
@Component
public class RowReaderFactory {

    public RowReader open(Path path, ImportFormat format) throws IOException {
        return switch (format) {
            case CSV -> new CsvRowReader(path);
            case XLSX -> throw new IllegalArgumentException("Le format XLSX n'est pas encore pris en charge.");
        };
    }
}
