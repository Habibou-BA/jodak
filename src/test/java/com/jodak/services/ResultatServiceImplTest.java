package com.jodak.services;

import com.jodak.dtos.resultat.PodiumResponse;
import com.jodak.dtos.resultat.ResultatRequest;
import com.jodak.dtos.resultat.ResultatResponse;
import com.jodak.entities.Athlete;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import com.jodak.entities.Resultat;
import com.jodak.enums.Medal;
import com.jodak.exceptions.BusinessRuleException;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.ResultatMapper;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import com.jodak.services.implementations.ResultatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultatServiceImplTest {

    @Mock
    private ResultatRepository repository;
    @Mock
    private ResultatMapper mapper;
    @Mock
    private EpreuveRepository epreuveRepository;
    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private ResultatServiceImpl service;

    private Epreuve epreuve(long disciplineId) {
        Discipline discipline = Discipline.builder().id(disciplineId).name("Athlétisme").build();
        return Epreuve.builder().id(10L).label("100 m").discipline(discipline).build();
    }

    private Athlete athlete(long disciplineId) {
        Discipline discipline = Discipline.builder().id(disciplineId).name("Athlétisme").build();
        return Athlete.builder().id(20L).lastName("Bolt").firstName("Usain").discipline(discipline).build();
    }

    private ResultatRequest request(int rank) {
        return new ResultatRequest(10L, 20L, rank);
    }

    private ResultatResponse response() {
        return new ResultatResponse(1L, null, null, 1, Medal.OR, null, null);
    }

    @Test
    @DisplayName("create attribue automatiquement l'or au rang 1 et persiste")
    void createAssignsGoldForRank1() {
        Resultat entity = Resultat.builder().rankPosition(1).build();
        when(epreuveRepository.findById(10L)).thenReturn(Optional.of(epreuve(1)));
        when(athleteRepository.findById(20L)).thenReturn(Optional.of(athlete(1)));
        when(repository.existsByEpreuve_IdAndAthlete_Id(10L, 20L)).thenReturn(false);
        when(repository.existsByEpreuve_IdAndRankPosition(10L, 1)).thenReturn(false);
        when(mapper.toEntity(any(), any(), any())).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response());

        service.create(request(1));

        assertThat(entity.getMedal()).isEqualTo(Medal.OR);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    @DisplayName("create lève 404 lorsque l'épreuve est introuvable")
    void createThrowsWhenEpreuveMissing() {
        when(epreuveRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Épreuve introuvable");
    }

    @Test
    @DisplayName("create lève 422 lorsque l'athlète n'est pas de la discipline de l'épreuve")
    void createThrowsWhenDisciplineMismatch() {
        when(epreuveRepository.findById(10L)).thenReturn(Optional.of(epreuve(1)));
        when(athleteRepository.findById(20L)).thenReturn(Optional.of(athlete(2)));

        assertThatThrownBy(() -> service.create(request(1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("discipline");

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create lève 409 lorsque l'athlète a déjà un résultat pour l'épreuve")
    void createThrowsWhenAthleteDuplicate() {
        when(epreuveRepository.findById(10L)).thenReturn(Optional.of(epreuve(1)));
        when(athleteRepository.findById(20L)).thenReturn(Optional.of(athlete(1)));
        when(repository.existsByEpreuve_IdAndAthlete_Id(10L, 20L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(1)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("athlète");
    }

    @Test
    @DisplayName("create lève 409 lorsque le rang est déjà attribué")
    void createThrowsWhenRankDuplicate() {
        when(epreuveRepository.findById(10L)).thenReturn(Optional.of(epreuve(1)));
        when(athleteRepository.findById(20L)).thenReturn(Optional.of(athlete(1)));
        when(repository.existsByEpreuve_IdAndAthlete_Id(10L, 20L)).thenReturn(false);
        when(repository.existsByEpreuve_IdAndRankPosition(10L, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(1)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("rang");
    }

    @Test
    @DisplayName("getPodium restitue les médaillés ordonnés de l'épreuve")
    void getPodiumReturnsMedalists() {
        Epreuve epreuve = epreuve(1);
        Resultat gold = Resultat.builder().rankPosition(1).medal(Medal.OR).build();
        when(epreuveRepository.findById(10L)).thenReturn(Optional.of(epreuve));
        when(repository.findByEpreuve_IdAndMedalNotNullOrderByRankPositionAsc(10L))
                .thenReturn(List.of(gold));
        when(mapper.toPodiumPosition(eq(gold)))
                .thenReturn(new com.jodak.dtos.resultat.PodiumPosition(1, Medal.OR, null));

        PodiumResponse podium = service.getPodium(10L);

        assertThat(podium.epreuveLabel()).isEqualTo("100 m");
        assertThat(podium.positions()).hasSize(1);
    }
}
