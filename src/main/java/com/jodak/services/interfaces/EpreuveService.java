package com.jodak.services.interfaces;

import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.epreuve.EpreuveRequest;
import com.jodak.dtos.epreuve.EpreuveResponse;
import com.jodak.dtos.epreuve.EpreuveSearchCriteria;
import org.springframework.data.domain.Pageable;

/**
 * Contrat métier de gestion des épreuves.
 */
public interface EpreuveService {

    EpreuveResponse create(EpreuveRequest request);

    EpreuveResponse getById(Long id);

    PageResponse<EpreuveResponse> search(EpreuveSearchCriteria criteria, Pageable pageable);

    EpreuveResponse update(Long id, EpreuveRequest request);

    void delete(Long id);
}
