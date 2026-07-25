package com.jodak.services;

import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.dtos.discipline.DisciplineResponse;
import com.jodak.entities.Discipline;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.DisciplineMapper;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.services.implementations.DisciplineServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisciplineServiceImplTest {

    @Mock
    private DisciplineRepository repository;

    @Mock
    private DisciplineMapper mapper;

    @InjectMocks
    private DisciplineServiceImpl service;

    @Test
    @DisplayName("create persiste une discipline lorsque le nom est libre")
    void createPersistsWhenNameFree() {
        DisciplineRequest request = new DisciplineRequest("Athlétisme");
        Discipline entity = Discipline.builder().name("Athlétisme").build();
        Discipline saved = Discipline.builder().id(1L).name("Athlétisme").build();
        when(repository.existsByNameIgnoreCase("Athlétisme")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(saved);
        when(mapper.toResponse(saved))
                .thenReturn(new DisciplineResponse(1L, "Athlétisme", null, null));

        DisciplineResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(1L);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    @DisplayName("create lève un conflit lorsque le nom existe déjà")
    void createThrowsConflictWhenNameExists() {
        DisciplineRequest request = new DisciplineRequest("Judo");
        when(repository.existsByNameIgnoreCase("Judo")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Judo");

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("getById lève une exception lorsque la discipline est absente")
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("update modifie la discipline existante")
    void updateModifiesExisting() {
        Discipline entity = Discipline.builder().id(2L).name("Ancien").build();
        DisciplineRequest request = new DisciplineRequest("Nouveau");
        when(repository.findById(2L)).thenReturn(Optional.of(entity));
        when(repository.existsByNameIgnoreCaseAndIdNot("Nouveau", 2L)).thenReturn(false);
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.toResponse(entity))
                .thenReturn(new DisciplineResponse(2L, "Nouveau", null, null));

        DisciplineResponse response = service.update(2L, request);

        assertThat(response.name()).isEqualTo("Nouveau");
        verify(mapper).updateEntity(entity, request);
    }

    @Test
    @DisplayName("delete supprime la discipline existante")
    void deleteRemovesExisting() {
        Discipline entity = Discipline.builder().id(3L).name("Escrime").build();
        when(repository.findById(3L)).thenReturn(Optional.of(entity));

        service.delete(3L);

        verify(repository).delete(entity);
    }
}
