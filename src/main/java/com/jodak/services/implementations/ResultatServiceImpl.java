package com.jodak.services.implementations;

import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.resultat.PodiumPosition;
import com.jodak.dtos.resultat.PodiumResponse;
import com.jodak.dtos.resultat.ResultatRequest;
import com.jodak.dtos.resultat.ResultatResponse;
import com.jodak.dtos.resultat.ResultatSearchCriteria;
import com.jodak.entities.Athlete;
import com.jodak.entities.Epreuve;
import com.jodak.entities.Resultat;
import com.jodak.exceptions.BusinessRuleException;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.ResultatMapper;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import com.jodak.services.interfaces.ResultatService;
import com.jodak.specifications.ResultatSpecifications;
import com.jodak.utils.PageResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation métier de la gestion des résultats. Garantit la cohérence (RM-09/10/11) et
 * l'attribution automatique de la médaille (RM-12).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResultatServiceImpl implements ResultatService {

    private final ResultatRepository repository;
    private final ResultatMapper mapper;
    private final EpreuveRepository epreuveRepository;
    private final AthleteRepository athleteRepository;

    @Override
    public ResultatResponse create(ResultatRequest request) {
        Epreuve epreuve = requireEpreuve(request.epreuveId());
        Athlete athlete = requireAthlete(request.athleteId());
        checkDisciplineCoherence(athlete, epreuve);
        checkUnique(epreuve.getId(), athlete.getId(), request.rank(), null);

        Resultat entity = mapper.toEntity(request, epreuve, athlete);
        entity.assignMedalFromRank();
        Resultat saved = repository.saveAndFlush(entity);
        log.info("Résultat enregistré : id={}, épreuve={}, rang={}, médaille={}",
                saved.getId(), epreuve.getId(), saved.getRankPosition(), saved.getMedal());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultatResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ResultatResponse> search(ResultatSearchCriteria criteria, Pageable pageable) {
        return PageResponseFactory.from(
                repository.findAll(ResultatSpecifications.build(criteria), pageable),
                mapper::toResponse);
    }

    @Override
    public ResultatResponse update(Long id, ResultatRequest request) {
        Resultat entity = findOrThrow(id);
        Epreuve epreuve = requireEpreuve(request.epreuveId());
        Athlete athlete = requireAthlete(request.athleteId());
        checkDisciplineCoherence(athlete, epreuve);
        checkUnique(epreuve.getId(), athlete.getId(), request.rank(), id);

        mapper.updateEntity(entity, request, epreuve, athlete);
        entity.assignMedalFromRank();
        Resultat saved = repository.saveAndFlush(entity);
        log.info("Résultat modifié : id={}, rang={}, médaille={}",
                id, saved.getRankPosition(), saved.getMedal());
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Resultat entity = findOrThrow(id);
        repository.delete(entity);
        log.info("Résultat supprimé : id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PodiumResponse getPodium(Long epreuveId) {
        Epreuve epreuve = requireEpreuve(epreuveId);
        List<PodiumPosition> positions =
                repository.findByEpreuve_IdAndMedalNotNullOrderByRankPositionAsc(epreuveId).stream()
                        .map(mapper::toPodiumPosition)
                        .toList();
        return new PodiumResponse(epreuve.getId(), epreuve.getLabel(), positions);
    }

    private void checkDisciplineCoherence(Athlete athlete, Epreuve epreuve) {
        if (!athlete.getDiscipline().getId().equals(epreuve.getDiscipline().getId())) {
            throw new BusinessRuleException("L'athlète ne pratique pas la discipline de l'épreuve.");
        }
    }

    private void checkUnique(Long epreuveId, Long athleteId, Integer rank, Long excludedId) {
        boolean athleteTaken = excludedId == null
                ? repository.existsByEpreuve_IdAndAthlete_Id(epreuveId, athleteId)
                : repository.existsByEpreuve_IdAndAthlete_IdAndIdNot(epreuveId, athleteId, excludedId);
        if (athleteTaken) {
            throw new ConflictException("Cet athlète a déjà un résultat pour cette épreuve.");
        }
        boolean rankTaken = excludedId == null
                ? repository.existsByEpreuve_IdAndRankPosition(epreuveId, rank)
                : repository.existsByEpreuve_IdAndRankPositionAndIdNot(epreuveId, rank, excludedId);
        if (rankTaken) {
            throw new ConflictException("Le rang " + rank + " est déjà attribué pour cette épreuve.");
        }
    }

    private Resultat findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Résultat introuvable pour l'identifiant " + id + "."));
    }

    private Epreuve requireEpreuve(Long epreuveId) {
        return epreuveRepository.findById(epreuveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Épreuve introuvable pour l'identifiant " + epreuveId + "."));
    }

    private Athlete requireAthlete(Long athleteId) {
        return athleteRepository.findById(athleteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Athlète introuvable pour l'identifiant " + athleteId + "."));
    }
}
