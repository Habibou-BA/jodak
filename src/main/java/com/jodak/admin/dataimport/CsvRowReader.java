package com.jodak.admin.dataimport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lecteur CSV en streaming (Apache Commons CSV) : la première ligne est l'en-tête.
 */
public class CsvRowReader implements RowReader {

    private final BufferedReader reader;
    private final CSVParser parser;

    public CsvRowReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        this.parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .build()
                .parse(reader);
    }

    @Override
    public List<String> headers() {
        return parser.getHeaderNames();
    }

    @Override
    public Iterator<RowData> iterator() {
        Iterator<CSVRecord> delegate = parser.iterator();
        List<String> headers = parser.getHeaderNames();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public RowData next() {
                CSVRecord record = delegate.next();
                Map<String, String> values = new LinkedHashMap<>();
                for (String header : headers) {
                    values.put(header, record.isSet(header) ? record.get(header) : null);
                }
                return new RowData(record.getRecordNumber(), values);
            }
        };
    }

    @Override
    public void close() throws IOException {
        parser.close();
        reader.close();
    }
}
