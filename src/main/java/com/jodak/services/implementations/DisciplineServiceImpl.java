package com.jodak.services.implementations;

import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.dtos.discipline.DisciplineResponse;
import com.jodak.entities.Discipline;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.DisciplineMapper;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.services.interfaces.DisciplineService;
import com.jodak.utils.PageResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation métier de la gestion des disciplines.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DisciplineServiceImpl implements DisciplineService {

    private final DisciplineRepository repository;
    private final DisciplineMapper mapper;

    @Override
    public DisciplineResponse create(DisciplineRequest request) {
        String name = normalize(request.name());
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Une discipline nommée « " + name + " » existe déjà.");
        }
        Discipline saved = repository.saveAndFlush(mapper.toEntity(request));
        log.info("Discipline créée : id={}, name={}", saved.getId(), saved.getName());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DisciplineResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DisciplineResponse> getAll(Pageable pageable) {
        return PageResponseFactory.from(repository.findAll(pageable), mapper::toResponse);
    }

    @Override
    public DisciplineResponse update(Long id, DisciplineRequest request) {
        Discipline entity = findOrThrow(id);
        String name = normalize(request.name());
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Une discipline nommée « " + name + " » existe déjà.");
        }
        mapper.updateEntity(entity, request);
        Discipline saved = repository.saveAndFlush(entity);
        log.info("Discipline modifiée : id={}", id);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Discipline entity = findOrThrow(id);
        repository.delete(entity);
        log.info("Discipline supprimée : id={}", id);
    }

    private Discipline findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Discipline introuvable pour l'identifiant " + id + "."));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
