package com.jodak.services.implementations;

import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.epreuve.EpreuveRequest;
import com.jodak.dtos.epreuve.EpreuveResponse;
import com.jodak.dtos.epreuve.EpreuveSearchCriteria;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.EpreuveMapper;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.services.interfaces.EpreuveService;
import com.jodak.specifications.EpreuveSpecifications;
import com.jodak.utils.PageResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Implémentation métier de la gestion des épreuves.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EpreuveServiceImpl implements EpreuveService {

    private final EpreuveRepository repository;
    private final EpreuveMapper mapper;
    private final DisciplineRepository disciplineRepository;

    @Override
    public EpreuveResponse create(EpreuveRequest request) {
        Discipline discipline = requireDiscipline(request.disciplineId());
        String label = request.label().trim();
        if (repository.existsByLabelIgnoreCaseAndDiscipline_IdAndEventDate(
                label, discipline.getId(), request.eventDate())) {
            throw duplicate(label, request.eventDate());
        }
        Epreuve saved = repository.saveAndFlush(mapper.toEntity(request, discipline));
        log.info("Épreuve créée : id={}, label={}", saved.getId(), saved.getLabel());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EpreuveResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EpreuveResponse> search(EpreuveSearchCriteria criteria, Pageable pageable) {
        return PageResponseFactory.from(
                repository.findAll(EpreuveSpecifications.build(criteria), pageable),
                mapper::toResponse);
    }

    @Override
    public EpreuveResponse update(Long id, EpreuveRequest request) {
        Epreuve entity = findOrThrow(id);
        Discipline discipline = requireDiscipline(request.disciplineId());
        String label = request.label().trim();
        if (repository.existsByLabelIgnoreCaseAndDiscipline_IdAndEventDateAndIdNot(
                label, discipline.getId(), request.eventDate(), id)) {
            throw duplicate(label, request.eventDate());
        }
        mapper.updateEntity(entity, request, discipline);
        Epreuve saved = repository.saveAndFlush(entity);
        log.info("Épreuve modifiée : id={}", id);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Epreuve entity = findOrThrow(id);
        repository.delete(entity);
        log.info("Épreuve supprimée : id={}", id);
    }

    private Epreuve findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Épreuve introuvable pour l'identifiant " + id + "."));
    }

    private Discipline requireDiscipline(Long disciplineId) {
        return disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Discipline introuvable pour l'identifiant " + disciplineId + "."));
    }

    private ConflictException duplicate(String label, LocalDate eventDate) {
        return new ConflictException(
                "Une épreuve « " + label + " » existe déjà pour cette discipline à la date du " + eventDate + ".");
    }
}
