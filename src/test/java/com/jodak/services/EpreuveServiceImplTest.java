package com.jodak.services;

import com.jodak.dtos.epreuve.EpreuveRequest;
import com.jodak.dtos.epreuve.EpreuveResponse;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.EpreuveMapper;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.services.implementations.EpreuveServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EpreuveServiceImplTest {

    @Mock
    private EpreuveRepository repository;
    @Mock
    private EpreuveMapper mapper;
    @Mock
    private DisciplineRepository disciplineRepository;

    @InjectMocks
    private EpreuveServiceImpl service;

    private static final LocalDate DATE = LocalDate.of(2024, 8, 4);

    private EpreuveRequest request() {
        return new EpreuveRequest("100 m", 1L, DATE);
    }

    private EpreuveResponse response(Long id) {
        return new EpreuveResponse(id, "100 m", null, DATE, null, null);
    }

    @Test
    @DisplayName("create persiste l'épreuve lorsque la discipline existe et le triplet est libre")
    void createPersists() {
        Discipline discipline = Discipline.builder().id(1L).name("Athlétisme").build();
        Epreuve entity = Epreuve.builder().build();
        Epreuve saved = Epreuve.builder().id(9L).build();
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline));
        when(repository.existsByLabelIgnoreCaseAndDiscipline_IdAndEventDate("100 m", 1L, DATE)).thenReturn(false);
        when(mapper.toEntity(any(), eq(discipline))).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response(9L));

        EpreuveResponse result = service.create(request());

        assertThat(result.id()).isEqualTo(9L);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    @DisplayName("create lève 404 lorsque la discipline est introuvable")
    void createThrowsWhenDisciplineMissing() {
        when(disciplineRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Discipline introuvable");

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create lève un conflit lorsque le triplet (libellé, discipline, date) existe déjà")
    void createThrowsConflictWhenDuplicate() {
        Discipline discipline = Discipline.builder().id(1L).name("Athlétisme").build();
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline));
        when(repository.existsByLabelIgnoreCaseAndDiscipline_IdAndEventDate("100 m", 1L, DATE)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("100 m");

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("getById lève 404 lorsque l'épreuve est absente")
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
