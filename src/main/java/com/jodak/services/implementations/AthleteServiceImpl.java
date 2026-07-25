package com.jodak.services.implementations;

import com.jodak.dtos.athlete.AthletePatchRequest;
import com.jodak.dtos.athlete.AthleteRequest;
import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.dtos.athlete.AthleteSearchCriteria;
import com.jodak.dtos.common.PageResponse;
import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.AthleteMapper;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.CountryRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.services.interfaces.AthleteService;
import com.jodak.specifications.AthleteSpecifications;
import com.jodak.utils.PageResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation métier de la gestion des athlètes. Résout les références (nation, discipline)
 * et applique les règles métier (RM-03).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AthleteServiceImpl implements AthleteService {

    private final AthleteRepository repository;
    private final AthleteMapper mapper;
    private final CountryRepository countryRepository;
    private final DisciplineRepository disciplineRepository;

    @Override
    public AthleteResponse create(AthleteRequest request) {
        Country country = requireCountry(request.countryId());
        Discipline discipline = requireDiscipline(request.disciplineId());
        Athlete saved = repository.saveAndFlush(mapper.toEntity(request, country, discipline));
        log.info("Athlète créé : id={}, name={} {}", saved.getId(), saved.getFirstName(), saved.getLastName());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AthleteResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AthleteResponse> search(AthleteSearchCriteria criteria, Pageable pageable) {
        return PageResponseFactory.from(
                repository.findAll(AthleteSpecifications.build(criteria), pageable),
                mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AthleteResponse> getByDiscipline(Long disciplineId, Pageable pageable) {
        AthleteSearchCriteria criteria =
                new AthleteSearchCriteria(null, null, null, null, disciplineId, null, null);
        return search(criteria, pageable);
    }

    @Override
    public AthleteResponse update(Long id, AthleteRequest request) {
        Athlete entity = findOrThrow(id);
        Country country = requireCountry(request.countryId());
        Discipline discipline = requireDiscipline(request.disciplineId());
        mapper.updateEntity(entity, request, country, discipline);
        Athlete saved = repository.saveAndFlush(entity);
        log.info("Athlète modifié (complet) : id={}", id);
        return mapper.toResponse(saved);
    }

    @Override
    public AthleteResponse patch(Long id, AthletePatchRequest request) {
        Athlete entity = findOrThrow(id);
        Country country = request.countryId() == null ? null : requireCountry(request.countryId());
        Discipline discipline = request.disciplineId() == null ? null : requireDiscipline(request.disciplineId());
        mapper.applyPatch(entity, request, country, discipline);
        Athlete saved = repository.saveAndFlush(entity);
        log.info("Athlète modifié (partiel) : id={}", id);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Athlete entity = findOrThrow(id);
        repository.delete(entity);
        log.info("Athlète supprimé : id={}", id);
    }

    private Athlete findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Athlète introuvable pour l'identifiant " + id + "."));
    }

    private Country requireCountry(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nation introuvable pour l'identifiant " + countryId + "."));
    }

    private Discipline requireDiscipline(Long disciplineId) {
        return disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Discipline introuvable pour l'identifiant " + disciplineId + "."));
    }
}
