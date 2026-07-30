package com.jodak.admin.dataimport;

import com.jodak.admin.enums.ImportFormat;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Fabrique de lecteurs selon le format (CSV via Commons CSV, XLSX via Apache POI).
 */
@Component
public class RowReaderFactory {

    public RowReader open(Path path, ImportFormat format) throws IOException {
        return switch (format) {
            case CSV -> new CsvRowReader(path);
            case XLSX -> new XlsxRowReader(path);
        };
    }

    /** Ouvre une feuille XLSX nommée (import « système » multi-feuilles). */
    public RowReader openSheet(Path path, String sheetName) throws IOException {
        return new XlsxRowReader(path, sheetName);
    }

    /** Noms des feuilles d'un classeur XLSX (dans l'ordre). */
    public List<String> sheetNames(Path path) throws IOException {
        return XlsxRowReader.sheetNames(path);
    }
}
