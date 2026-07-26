package com.jodak.admin;

import com.jodak.admin.dataimport.ImportRowOutcome;
import com.jodak.admin.dataimport.RowData;
import com.jodak.admin.dataimport.importers.DisciplineRowImporter;
import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportMode;
import com.jodak.entities.Discipline;
import com.jodak.repositories.DisciplineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisciplineRowImporterTest {

    @Mock
    private DisciplineRepository disciplineRepository;

    @InjectMocks
    private DisciplineRowImporter importer;

    private RowData row(String name) {
        Map<String, String> values = new HashMap<>();
        values.put("name", name);
        return new RowData(1, values);
    }

    @Test
    @DisplayName("Nom vide → échec")
    void blankNameFails() {
        ImportRowOutcome outcome = importer.importRow(row("  "), ImportMode.COMMIT, DuplicateStrategy.SKIP);
        assertThat(outcome.result()).isEqualTo(ImportRowOutcome.Result.FAILED);
        assertThat(outcome.errorCode()).isEqualTo("REQUIRED");
        verify(disciplineRepository, never()).save(any());
    }

    @Test
    @DisplayName("DRY_RUN n'écrit pas")
    void dryRunDoesNotWrite() {
        when(disciplineRepository.existsByNameIgnoreCase("Judo")).thenReturn(false);
        ImportRowOutcome outcome = importer.importRow(row("Judo"), ImportMode.DRY_RUN, DuplicateStrategy.SKIP);
        assertThat(outcome.result()).isEqualTo(ImportRowOutcome.Result.IMPORTED);
        assertThat(outcome.entityId()).isNull();
        verify(disciplineRepository, never()).save(any());
    }

    @Test
    @DisplayName("COMMIT persiste et renvoie l'identifiant")
    void commitPersists() {
        when(disciplineRepository.existsByNameIgnoreCase("Judo")).thenReturn(false);
        when(disciplineRepository.save(any())).thenReturn(Discipline.builder().id(5L).name("Judo").build());
        ImportRowOutcome outcome = importer.importRow(row("Judo"), ImportMode.COMMIT, DuplicateStrategy.SKIP);
        assertThat(outcome.result()).isEqualTo(ImportRowOutcome.Result.IMPORTED);
        assertThat(outcome.entityId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Doublon ignoré avec SKIP, rejeté avec REJECT")
    void duplicateStrategies() {
        when(disciplineRepository.existsByNameIgnoreCase("Judo")).thenReturn(true);
        assertThat(importer.importRow(row("Judo"), ImportMode.COMMIT, DuplicateStrategy.SKIP).result())
                .isEqualTo(ImportRowOutcome.Result.SKIPPED);
        assertThat(importer.importRow(row("Judo"), ImportMode.COMMIT, DuplicateStrategy.REJECT).result())
                .isEqualTo(ImportRowOutcome.Result.FAILED);
    }
}
