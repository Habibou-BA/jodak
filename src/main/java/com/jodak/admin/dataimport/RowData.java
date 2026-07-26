package com.jodak.admin.dataimport;

import java.util.Map;

/**
 * Une ligne de données lue d'un fichier d'import (numéro de ligne + valeurs par colonne).
 */
public record RowData(long number, Map<String, String> values) {

    public String get(String column) {
        return values.get(column);
    }
}
