package com.jodak.admin.dataimport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Lecteur XLSX (Apache POI) : la première feuille sert de source, sa première ligne d'en-tête.
 *
 * <p>Le durcissement anti « zip bomb » / XXE est appliqué globalement par {@link PoiSecurityConfig}.
 * Les tailles de fichier étant bornées en amont (25 Mio), le classeur est chargé via l'API
 * {@code usermodel} ; les lignes entièrement vides sont ignorées et les dates sont normalisées en
 * ISO (comme les CSV).</p>
 */
public class XlsxRowReader implements RowReader {

    private final XSSFWorkbook workbook;
    private final DataFormatter formatter = new DataFormatter();
    private final List<String> headers;
    private final Iterator<Row> rowIterator;

    public XlsxRowReader(Path path) throws IOException {
        InputStream in = Files.newInputStream(path);
        try {
            this.workbook = new XSSFWorkbook(in);
        } catch (RuntimeException ex) {
            in.close();
            throw new IOException("Fichier XLSX illisible ou corrompu.", ex);
        }
        in.close();

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        List<String> heads = new ArrayList<>();
        if (it.hasNext()) {
            Row headerRow = it.next();
            int last = headerRow.getLastCellNum();
            for (int c = 0; c < last; c++) {
                heads.add(cellString(headerRow.getCell(c)).trim());
            }
        }
        this.headers = List.copyOf(heads);
        this.rowIterator = it;
    }

    @Override
    public List<String> headers() {
        return headers;
    }

    @Override
    public Iterator<RowData> iterator() {
        return new Iterator<>() {
            private RowData next = advance();

            private RowData advance() {
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Map<String, String> values = new LinkedHashMap<>();
                    boolean allBlank = true;
                    for (int c = 0; c < headers.size(); c++) {
                        String value = cellString(row.getCell(c)).trim();
                        if (!value.isEmpty()) {
                            allBlank = false;
                        }
                        values.put(headers.get(c), value.isEmpty() ? null : value);
                    }
                    if (!allBlank) {
                        return new RowData(row.getRowNum() + 1L, values);
                    }
                }
                return null;
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public RowData next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                RowData current = next;
                next = advance();
                return current;
            }
        };
    }

    private String cellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : numericToString(cell.getNumericCellValue());
            case FORMULA -> formatter.formatCellValue(cell);
            default -> "";
        };
    }

    private String numericToString(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
