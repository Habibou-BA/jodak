package com.jodak.services.interfaces;

import com.jodak.dtos.athlete.AthletePatchRequest;
import com.jodak.dtos.athlete.AthleteRequest;
import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.dtos.athlete.AthleteSearchCriteria;
import com.jodak.dtos.common.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contrat métier de gestion des athlètes.
 */
public interface AthleteService {

    AthleteResponse create(AthleteRequest request);

    AthleteResponse getById(Long id);

    PageResponse<AthleteResponse> search(AthleteSearchCriteria criteria, Pageable pageable);

    PageResponse<AthleteResponse> getByDiscipline(Long disciplineId, Pageable pageable);

    AthleteResponse update(Long id, AthleteRequest request);

    AthleteResponse patch(Long id, AthletePatchRequest request);

    void delete(Long id);
}
