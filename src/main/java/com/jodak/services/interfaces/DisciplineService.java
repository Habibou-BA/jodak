package com.jodak.services.interfaces;

import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.discipline.DisciplineRequest;
import com.jodak.dtos.discipline.DisciplineResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contrat métier de gestion des disciplines.
 */
public interface DisciplineService {

    DisciplineResponse create(DisciplineRequest request);

    DisciplineResponse getById(Long id);

    PageResponse<DisciplineResponse> getAll(Pageable pageable);

    DisciplineResponse update(Long id, DisciplineRequest request);

    void delete(Long id);
}
