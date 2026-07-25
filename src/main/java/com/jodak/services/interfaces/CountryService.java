package com.jodak.services.interfaces;

import com.jodak.dtos.common.PageResponse;
import com.jodak.dtos.country.CountryRequest;
import com.jodak.dtos.country.CountryResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contrat métier de gestion du référentiel des nations.
 */
public interface CountryService {

    CountryResponse create(CountryRequest request);

    CountryResponse getById(Long id);

    PageResponse<CountryResponse> getAll(Pageable pageable);
}
