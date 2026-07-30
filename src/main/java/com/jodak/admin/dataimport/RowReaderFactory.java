package com.jodak.admin.dataimport;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Fabrique de lecteurs pour l'import système : accès aux feuilles nommées d'un classeur XLSX
 * (Apache POI).
 */
@Component
public class RowReaderFactory {

    /** Ouvre une feuille XLSX nommée. */
    public RowReader openSheet(Path path, String sheetName) throws IOException {
        return new XlsxRowReader(path, sheetName);
    }

    /** Noms des feuilles d'un classeur XLSX (dans l'ordre). */
    public List<String> sheetNames(Path path) throws IOException {
        return XlsxRowReader.sheetNames(path);
    }
}
