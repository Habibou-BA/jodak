package com.jodak.services;

import com.jodak.dtos.athlete.AthletePatchRequest;
import com.jodak.dtos.athlete.AthleteRequest;
import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.enums.Gender;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.AthleteMapper;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.CountryRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.services.implementations.AthleteServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AthleteServiceImplTest {

    @Mock
    private AthleteRepository repository;
    @Mock
    private AthleteMapper mapper;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private DisciplineRepository disciplineRepository;

    @InjectMocks
    private AthleteServiceImpl service;

    private AthleteRequest validRequest() {
        return new AthleteRequest("Bolt", "Usain", Gender.MALE,
                LocalDate.of(1986, 8, 21), 3L, 1L, 195, 94);
    }

    @Test
    @DisplayName("create résout les références et persiste l'athlète")
    void createResolvesReferences() {
        Country country = Country.builder().id(3L).code("FRA").name("France").build();
        Discipline discipline = Discipline.builder().id(1L).name("Athlétisme").build();
        Athlete entity = Athlete.builder().build();
        Athlete saved = Athlete.builder().id(9L).build();
        when(countryRepository.findById(3L)).thenReturn(Optional.of(country));
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline));
        when(mapper.toEntity(any(), any(), any())).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(mock(9L));

        AthleteResponse response = service.create(validRequest());

        assertThat(response.id()).isEqualTo(9L);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    @DisplayName("create lève 404 lorsque la nationalité est introuvable")
    void createThrowsWhenCountryMissing() {
        when(countryRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nation introuvable");

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create lève 404 lorsque la discipline est introuvable")
    void createThrowsWhenDisciplineMissing() {
        Country country = Country.builder().id(3L).build();
        when(countryRepository.findById(3L)).thenReturn(Optional.of(country));
        when(disciplineRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Discipline introuvable");
    }

    @Test
    @DisplayName("patch partiel ne résout pas les références absentes")
    void patchAppliesPartialWithoutResolvingAbsentReferences() {
        Athlete entity = Athlete.builder().id(5L).build();
        when(repository.findById(5L)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(mock(5L));

        AthletePatchRequest patch =
                new AthletePatchRequest(null, null, null, null, null, null, null, 80);
        service.patch(5L, patch);

        verify(countryRepository, never()).findById(any());
        verify(disciplineRepository, never()).findById(any());
        verify(mapper).applyPatch(entity, patch, null, null);
    }

    @Test
    @DisplayName("getById lève 404 lorsque l'athlète est absent")
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    private static AthleteResponse mock(Long id) {
        return new AthleteResponse(id, "Bolt", "Usain", Gender.MALE,
                LocalDate.of(1986, 8, 21), 39, null, null, 195, 94, null, null);
    }
}
