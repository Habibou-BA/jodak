package com.jodak.admin.dataimport;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;

/**
 * Lecture séquentielle (streaming) des lignes d'un fichier d'import.
 */
public interface RowReader extends Closeable, Iterable<RowData> {

    List<String> headers();

    @Override
    Iterator<RowData> iterator();
}
