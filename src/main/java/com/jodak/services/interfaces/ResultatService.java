package com.jodak.services.interfaces;

import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.resultat.PodiumResponse;
import com.jodak.dtos.resultat.ResultatRequest;
import com.jodak.dtos.resultat.ResultatResponse;
import com.jodak.dtos.resultat.ResultatSearchCriteria;
import org.springframework.data.domain.Pageable;

/**
 * Contrat métier de gestion des résultats.
 */
public interface ResultatService {

    ResultatResponse create(ResultatRequest request);

    ResultatResponse getById(Long id);

    PageResponse<ResultatResponse> search(ResultatSearchCriteria criteria, Pageable pageable);

    ResultatResponse update(Long id, ResultatRequest request);

    void delete(Long id);

    PodiumResponse getPodium(Long epreuveId);
}
