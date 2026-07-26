package com.jodak.admin.dataimport;

import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;

import java.util.List;

/**
 * Stratégie d'import d'une ligne pour un type d'entité donné (validation, doublons, persistance).
 */
public interface RowImporter {

    ImportJobType type();

    List<String> requiredColumns();

    ImportRowOutcome importRow(RowData row, ImportMode mode, DuplicateStrategy strategy);
}
