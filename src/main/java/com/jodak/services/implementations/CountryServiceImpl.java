package com.jodak.services.implementations;

import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.country.CountryRequest;
import com.jodak.dtos.country.CountryResponse;
import com.jodak.entities.Country;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.CountryMapper;
import com.jodak.repositories.CountryRepository;
import com.jodak.services.interfaces.CountryService;
import com.jodak.utils.PageResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation métier de la gestion des nations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CountryServiceImpl implements CountryService {

    private final CountryRepository repository;
    private final CountryMapper mapper;

    @Override
    public CountryResponse create(CountryRequest request) {
        String code = mapper.normalizeCode(request.code());
        String name = mapper.normalizeName(request.name());
        if (repository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Une nation avec le code « " + code + " » existe déjà.");
        }
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Une nation nommée « " + name + " » existe déjà.");
        }
        Country saved = repository.saveAndFlush(mapper.toEntity(request));
        log.info("Nation créée : id={}, code={}", saved.getId(), saved.getCode());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CountryResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CountryResponse> getAll(Pageable pageable) {
        return PageResponseFactory.from(repository.findAll(pageable), mapper::toResponse);
    }

    private Country findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nation introuvable pour l'identifiant " + id + "."));
    }
}
